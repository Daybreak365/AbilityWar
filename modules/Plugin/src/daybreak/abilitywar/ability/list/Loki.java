package daybreak.abilitywar.ability.list;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableMap;
import daybreak.abilitywar.ability.AbilityBase;
import daybreak.abilitywar.ability.AbilityManifest;
import daybreak.abilitywar.ability.AbilityManifest.Rank;
import daybreak.abilitywar.ability.AbilityManifest.Species;
import daybreak.abilitywar.ability.SubscribeEvent;
import daybreak.abilitywar.ability.decorator.ActiveHandler;
import daybreak.abilitywar.config.ability.AbilitySettings.SettingObject;
import daybreak.abilitywar.config.enums.CooldownDecrease;
import daybreak.abilitywar.game.AbstractGame.Participant;
import daybreak.abilitywar.game.module.DeathManager;
import daybreak.abilitywar.game.team.interfaces.Teamable;
import daybreak.abilitywar.utils.base.Formatter;
import daybreak.abilitywar.utils.base.concurrent.TimeUnit;
import daybreak.abilitywar.utils.base.math.LocationUtil;
import daybreak.abilitywar.utils.base.math.VectorUtil;
import daybreak.abilitywar.utils.base.minecraft.nms.IHologram;
import daybreak.abilitywar.utils.base.minecraft.nms.NMS;
import daybreak.abilitywar.utils.library.SoundLib;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.function.Predicate;

@AbilityManifest(name = "로키", rank = Rank.S, species = Species.GOD, explain = {
		"다른 플레이어를 근접 공격해 스택을 쌓을 수 있습니다. 스택이 쌓일 때마다 대상을",
		"중심으로 하여 마지막으로 이동한 방향으로 짧게 순간이동합니다. 스택이 다섯 개",
		"쌓이면 스택이 초기화되고 반대 방향으로 순간이동합니다. 다른 플레이어를 바라본",
		"상태로 철괴를 우클릭하면 대상의 등 뒤로 순간이동합니다. $[COOLDOWN_CONFIG]"
})
public class Loki extends AbilityBase implements ActiveHandler {

	public static final SettingObject<Integer> COOLDOWN_CONFIG = abilitySettings.new SettingObject<Integer>(Loki.class, "cooldown", 15,
			"# 쿨타임") {

		@Override
		public boolean condition(Integer value) {
			return value >= 0;
		}

		@Override
		public String toString() {
			return Formatter.formatCooldown(getValue());
		}

	};
	private static final Predicate<Block> IS_LIQUID = Block::isLiquid;

	private enum Direction {
		LEFT {
			@Override
			public int apply(int angle) {
				return -angle;
			}
		}, RIGHT {
			@Override
			public int apply(int angle) {
				return angle;
			}
		}, UNKNOWN {
			@Override
			public int apply(int angle) {
				return angle;
			}
		};

		public abstract int apply(int angle);
	}

	private static Direction choose(double d, Direction plus, Direction minus) {
		return d == 0 ? Direction.UNKNOWN : (d > 0 ? plus : minus);
	}

	private static final Map<BlockFace, Function<PlayerMoveEvent, Direction>> directionCalculators = ImmutableMap.<BlockFace, Function<PlayerMoveEvent, Direction>>builder()
			.put(BlockFace.NORTH, new Function<PlayerMoveEvent, Direction>() {
				@Override
				public Direction apply(PlayerMoveEvent e) {
					final Location to = e.getTo();
					if (to == null) return Direction.UNKNOWN;
					return choose(to.getX() - e.getFrom().getX(), Direction.RIGHT, Direction.LEFT);
				}
			})
			.put(BlockFace.SOUTH, new Function<PlayerMoveEvent, Direction>() {
				@Override
				public Direction apply(PlayerMoveEvent e) {
					final Location to = e.getTo();
					if (to == null) return Direction.UNKNOWN;
					return choose(to.getX() - e.getFrom().getX(), Direction.LEFT, Direction.RIGHT);
				}
			})
			.put(BlockFace.WEST, new Function<PlayerMoveEvent, Direction>() {
				@Override
				public Direction apply(PlayerMoveEvent e) {
					final Location to = e.getTo();
					if (to == null) return Direction.UNKNOWN;
					return choose(to.getZ() - e.getFrom().getZ(), Direction.LEFT, Direction.RIGHT);
				}
			})
			.put(BlockFace.EAST, new Function<PlayerMoveEvent, Direction>() {
				@Override
				public Direction apply(PlayerMoveEvent e) {
					final Location to = e.getTo();
					if (to == null) return Direction.UNKNOWN;
					return choose(to.getZ() - e.getFrom().getZ(), Direction.RIGHT, Direction.LEFT);
				}
			})
			.build();

	private final Map<UUID, Stack> stacks = new HashMap<>();
	private final Predicate<Entity> predicate = new Predicate<Entity>() {
		@Override
		public boolean test(Entity entity) {
			if (entity.equals(getPlayer())) return false;
			if (!getGame().isParticipating(entity.getUniqueId())
					|| (getGame() instanceof DeathManager.Handler && ((DeathManager.Handler) getGame()).getDeathManager().isExcluded(entity.getUniqueId()))
					|| !getGame().getParticipant(entity.getUniqueId()).attributes().TARGETABLE.getValue()) {
				return false;
			}
			if (getGame() instanceof Teamable) {
				final Teamable teamGame = (Teamable) getGame();
				final Participant entityParticipant = teamGame.getParticipant(entity.getUniqueId()), participant = getParticipant();
				return !teamGame.hasTeam(entityParticipant) || !teamGame.hasTeam(participant) || (!teamGame.getTeam(entityParticipant).equals(teamGame.getTeam(participant)));
			}
			return true;
		}
	};

	private @NotNull Direction lastDirection = Direction.UNKNOWN;
	private final Cooldown cooldown = new Cooldown(COOLDOWN_CONFIG.getValue(), CooldownDecrease._25);

	public Loki(Participant participant) {
		super(participant);
	}

	@Override
	public boolean ActiveSkill(Material material, ClickType clickType) {
		if (material == Material.IRON_INGOT && clickType == ClickType.RIGHT_CLICK && !cooldown.isCooldown()) {
			final Player target = LocationUtil.getEntityLookingAt(Player.class, getPlayer(), 20, predicate);
			if (target != null) {
				cooldown.start();
				final Location targetLoc = target.getLocation();
				final Vector targetDir = targetLoc.getDirection();
				getPlayer().teleport(LocationUtil.floorY(targetLoc.clone().add(targetDir.multiply(-.75))));
				SoundLib.ITEM_CHORUS_FRUIT_TELEPORT.playSound(getPlayer());
				return true;
			}
		}
		return false;
	}

	@SubscribeEvent
	private void onPlayerMove(PlayerMoveEvent e) {
		final Function<PlayerMoveEvent, Direction> function = directionCalculators.get(LocationUtil.getFacing(getPlayer().getLocation().getYaw()));
		if (function != null) {
			final Direction direction = function.apply(e);
			if (direction != Direction.UNKNOWN) {
				this.lastDirection = direction;
			}
		}
	}

	private void teleport(LivingEntity entity, double angle) {
		final Location playerLoc = getPlayer().getLocation();
		final Vector between = playerLoc.toVector().subtract(entity.getLocation().toVector());
		if (between.length() > 3.5) between.normalize().multiply(3.5);
		Location location = entity.getLocation().clone().add(VectorUtil.rotateAroundAxisY(between, angle));
		location.setYaw(playerLoc.getYaw());
		location.setPitch(playerLoc.getPitch());
		location = LocationUtil.floorY(location, location.getY(), IS_LIQUID);
		final Vector direction = entity.getLocation().toVector().subtract(location.toVector());
		getPlayer().teleport(location.setDirection(direction));
	}

	@SubscribeEvent(onlyRelevant = true)
	private void onPlayerJoin(PlayerJoinEvent e) {
		for (Stack stack : stacks.values()) {
			stack.hologram.display(getPlayer());
		}
	}

	@SubscribeEvent
	private void onAttack(EntityDamageByEntityEvent e) {
		final Entity entity = e.getEntity();
		if (getPlayer().equals(e.getDamager()) && entity instanceof Player && !e.isCancelled() && predicate.test(entity)) {
			final Stack stack = stacks.get(e.getEntity().getUniqueId());
			if (stack != null) {
				if (stack.addStack()) {
					e.setDamage(e.getDamage() * 1.4);
				}
			} else new Stack((Player) e.getEntity()).start();
		}
	}

	@SubscribeEvent
	private void onDeath(EntityDeathEvent e) {
		final Stack stack = stacks.get(e.getEntity().getUniqueId());
		if (stack != null) stack.stop(true);
	}

	@SubscribeEvent
	private void onDeath(PlayerDeathEvent e) {
		final Stack stack = stacks.get(e.getEntity().getUniqueId());
		if (stack != null) stack.stop(true);
	}

	private class Stack extends AbilityTimer {

		private final Player entity;
		private final IHologram hologram;
		private int stack = 0;

		private Stack(Player entity) {
			super(30);
			setPeriod(TimeUnit.TICKS, 4);
			this.entity = entity;
			this.hologram = NMS.newHologram(entity.getWorld(), entity.getLocation().getX(), entity.getLocation().getY() + entity.getEyeHeight() + 0.6, entity.getLocation().getZ(), Strings.repeat("§3●", stack).concat(Strings.repeat("§3◎", 5 - stack)));
			hologram.display(getPlayer());
			stacks.put(entity.getUniqueId(), this);
			addStack();
		}

		@Override
		protected void run(int count) {
			hologram.teleport(entity.getWorld(), entity.getLocation().getX(), entity.getLocation().getY() + entity.getEyeHeight() + 0.6, entity.getLocation().getZ(), entity.getLocation().getYaw(), 0);
		}

		private boolean addStack() {
			setCount(30);
			stack++;
			hologram.setText(Strings.repeat("§3●", stack).concat(Strings.repeat("§3◎", 5 - stack)));
			if (stack >= 5) {
				stop(false);
				teleport(entity, 180);
				SoundLib.ENTITY_FIREWORK_ROCKET_TWINKLE.playSound(getPlayer());
				SoundLib.ENTITY_FIREWORK_ROCKET_TWINKLE.playSound(entity);
				return true;
			} else {
				teleport(entity, lastDirection.apply(40));
				return false;
			}
		}

		@Override
		protected void onEnd() {
			onSilentEnd();
		}

		@Override
		protected void onSilentEnd() {
			hologram.unregister();
			stacks.remove(entity.getUniqueId());
		}
	}

}
