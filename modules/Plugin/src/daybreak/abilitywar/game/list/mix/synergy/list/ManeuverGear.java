package daybreak.abilitywar.game.list.mix.synergy.list;

import com.google.common.base.Strings;
import daybreak.abilitywar.ability.AbilityManifest;
import daybreak.abilitywar.ability.AbilityManifest.Rank;
import daybreak.abilitywar.ability.AbilityManifest.Species;
import daybreak.abilitywar.ability.SubscribeEvent;
import daybreak.abilitywar.ability.decorator.ActiveHandler;
import daybreak.abilitywar.ability.list.grapplinghook.Hooks;
import daybreak.abilitywar.config.ability.AbilitySettings.SettingObject;
import daybreak.abilitywar.game.AbstractGame.CustomEntity;
import daybreak.abilitywar.game.AbstractGame.Participant;
import daybreak.abilitywar.game.AbstractGame.Participant.ActionbarNotification.ActionbarChannel;
import daybreak.abilitywar.game.list.mix.synergy.Synergy;
import daybreak.abilitywar.game.manager.effect.Stun;
import daybreak.abilitywar.game.module.DeathManager;
import daybreak.abilitywar.game.module.Wreck;
import daybreak.abilitywar.game.team.interfaces.Teamable;
import daybreak.abilitywar.utils.base.color.RGB;
import daybreak.abilitywar.utils.base.concurrent.TimeUnit;
import daybreak.abilitywar.utils.base.math.LocationUtil;
import daybreak.abilitywar.utils.base.math.VectorUtil;
import daybreak.abilitywar.utils.base.minecraft.ability.list.grapplinghook.HookEntity;
import daybreak.abilitywar.utils.base.minecraft.entity.decorator.Deflectable;
import daybreak.abilitywar.utils.base.minecraft.raytrace.RayTrace;
import daybreak.abilitywar.utils.library.ParticleLib;
import daybreak.abilitywar.utils.library.SoundLib;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.attribute.Attribute;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.projectiles.ProjectileSource;
import org.bukkit.util.Vector;

import java.util.HashSet;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.UUID;
import java.util.function.Predicate;
import java.util.function.Supplier;

@AbilityManifest(name = "입체 기동 장치", rank = Rank.L, species = Species.HUMAN, explain = {
		"§7충전 §8- §3후크§f: 최대 $[MAX_CHARGE]개까지 보유할 수 있으며, 후크를 모두 사용한 후 15초가 지나면",
		" 후크가 모두 충전됩니다.",
		"§7철괴 우클릭 §8- §3후크 발사§f: 바라보는 방향으로 후크를 발사합니다. 블록 또는 적에게",
		" 고정되면, 빠르게 목표 지점으로 이동합니다. 적에게 고정됐다면 목표 지점",
		" 도달 후 §3급습 §f효과가 즉시 적용됩니다. 단, 너무 가까이에서는 적에게 후크를",
		" 고정할 수 없습니다.",
		"§7웅크리기 §8- §3절단§f/§3급습§f: 사용 중인 후크를 끊고 그 자리에 멈춥니다. 주위에 벽이나",
		" 지면이 있다면 바라보는 방향으로 짧게 도약합니다. §3/§f 5칸 이내의 적을 바라본",
		" 상태로 도약한 경우, 해당 적을 기절시키고 최대 체력 비례 피해를 입힙니다.",
		"§7패시브 §8- §3가벼운 착지§f: 낙하 피해를 입지 않습니다."
})
public class ManeuverGear extends Synergy implements ActiveHandler {

	public static final SettingObject<Integer> MAX_CHARGE = synergySettings.new SettingObject<Integer>(ManeuverGear.class, "max-charge", 6,
			"# 후크 최대 충전",
			"# 기본값: 6") {

		@Override
		public boolean condition(Integer value) {
			return value >= 1;
		}

	};

	public ManeuverGear(Participant participant) {
		super(participant);
	}

	@SubscribeEvent(onlyRelevant = true)
	private void onEntityDamage(final EntityDamageEvent e) {
		if (e.getCause() == DamageCause.FALL) {
			e.setCancelled(true);
		}
	}

	private final Predicate<Entity> predicate = new Predicate<Entity>() {
		@Override
		public boolean test(Entity entity) {
			if (entity.equals(getPlayer())) return false;
			if (entity instanceof Player) {
				if (!getGame().isParticipating(entity.getUniqueId())
						|| (getGame() instanceof DeathManager.Handler && ((DeathManager.Handler) getGame()).getDeathManager().isExcluded(entity.getUniqueId()))
						|| !getGame().getParticipant(entity.getUniqueId()).attributes().TARGETABLE.getValue()) {
					return false;
				}
				if (getGame() instanceof Teamable) {
					final Teamable teamGame = (Teamable) getGame();
					final Participant entityParticipant = teamGame.getParticipant(entity.getUniqueId()), participant = teamGame.getParticipant(getPlayer().getUniqueId());
					if (participant != null) {
						return !teamGame.hasTeam(entityParticipant) || !teamGame.hasTeam(participant) || (!teamGame.getTeam(entityParticipant).equals(teamGame.getTeam(participant)));
					}
				}
			}
			return true;
		}
	};

	private final int maxCharge = MAX_CHARGE.getValue();

	private class Charge extends AbilityTimer {

		private final ActionbarChannel actionbarChannel = newActionbarChannel();
		private int charges = maxCharge;

		private Charge() {
			super(TaskType.REVERSE, (int) (15 * Wreck.calculateDecreasedAmount(25)));
			setBehavior(RestrictionBehavior.PAUSE_RESUME);
		}

		private boolean subtractCharge(int amount) {
			if (!isRunning() && charges > 0) {
				charges = Math.max(0, charges - amount);
				actionbarChannel.update(toString());
				if (charges == 0) {
					start();
				}
				return true;
			} else return false;
		}

		@Override
		protected void run(int count) {
			actionbarChannel.update(Strings.repeat("§3●", charges).concat(Strings.repeat("§3○", maxCharge - charges)) + " §7| §3충전§f: " + count + "초");
		}

		@Override
		protected void onEnd() {
			this.charges = maxCharge;
			attacked.clear();
			actionbarChannel.update(toString());
		}

		@Override
		protected void onSilentEnd() {
			actionbarChannel.update(null);
		}

		@Override
		public String toString() {
			if (!isRunning()) {
				return Strings.repeat("§3●", charges).concat(Strings.repeat("§3○", maxCharge - charges));
			} else {
				return Strings.repeat("§3●", charges).concat(Strings.repeat("§3○", maxCharge - charges)) + " §7| §3충전§f: " + getCount() + "초";
			}
		}
	}

	private final Charge charge = new Charge();
	private final Set<UUID> attacked = new HashSet<>();

	protected Hook hook = null;
	private Move move = null;

	@Override
	public boolean ActiveSkill(Material material, ClickType clickType) {
		if (clickType == ClickType.RIGHT_CLICK) {
			if (material == Material.IRON_INGOT) {
				if (hook == null && move == null) {
					if (charge.charges > 0) {
						this.hook = new Hook(getPlayer().getEyeLocation(), getPlayer().getLocation().getDirection()) {
							@Override
							public boolean consume(double x, double y, double z) {
								if (move == null) {
									if (ManeuverGear.this.charge.subtractCharge(1)) {
										ManeuverGear.this.move = new Move(new Location(getPlayer().getWorld(), x, y, z));
										SoundLib.ENTITY_FISHING_BOBBER_RETRIEVE.playSound(getPlayer());
									}
								}
								return true;
							}

							@Override
							public boolean consume(Player player) {
								if (move == null) {
									if (ManeuverGear.this.charge.subtractCharge(1)) {
										ManeuverGear.this.move = new Move(player);
										SoundLib.ENTITY_FISHING_BOBBER_RETRIEVE.playSound(getPlayer());
									}
								}
								return true;
							}
						};
						return true;
					}
				}
			}
		}
		return false;
	}

	@SubscribeEvent(ignoreCancelled = true, onlyRelevant = true)
	private void onToggleSneak(final PlayerToggleSneakEvent e) {
		if (move != null) {
			move.cut();
		}
	}

	private class Move extends AbilityTimer {

		private final Supplier<Location> target;
		private final Runnable afterMove;
		private final HookEntity entityHook;
		private boolean arrived = false;

		private Move(final Location targetLoc) {
			super(TaskType.NORMAL, 260);
			setPeriod(TimeUnit.TICKS, 1);
			this.target = new Supplier<Location>() {
				@Override
				public Location get() {
					return targetLoc;
				}
			};
			this.afterMove = null;
			this.entityHook = Hooks.createHook(getPlayer(), targetLoc);
			start();
		}

		private Move(final Player player) {
			super(TaskType.NORMAL, 260);
			setPeriod(TimeUnit.TICKS, 1);
			this.target = new Supplier<Location>() {
				@Override
				public Location get() {
					return player.getLocation();
				}
			};
			this.afterMove = new Runnable() {
				@Override
				public void run() {
					stop(false);
					attack(player, true);
				}
			};
			this.entityHook = Hooks.createHook(getPlayer(), player);
			start();
		}

		private boolean canDash() {
			if (arrived) return true;
			final Block block = getPlayer().getLocation().getBlock();
			for (int x = -1; x <= 1; x++)
				for (int y = -1; y <= 0; y++)
					for (int z = -1; z <= 1; z++) {
						if (!(x == 0 && y == 0 && z == 0) && block.getRelative(x, y, z).getType().isSolid())
							return true;
					}
			return false;
		}

		public void cut() {
			if (canDash()) {
				stop(false);
				final Player lookingAt = LocationUtil.getEntityLookingAt(Player.class, getPlayer(), 5, .5, predicate);
				if (lookingAt != null) {
					attack(lookingAt, false);
				} else {
					getPlayer().setVelocity(getPlayer().getLocation().getDirection().multiply(1.65));
				}
			} else {
				stop(true);
			}
		}

		public void attack(Player player, boolean weaken) {
			getPlayer().setVelocity(VectorUtil.validateVector(player.getLocation().toVector().subtract(getPlayer().getLocation().toVector()).normalize()).multiply(1.25));
			SoundLib.ENTITY_PLAYER_ATTACK_KNOCKBACK.playSound(getPlayer());
			SoundLib.ENTITY_PLAYER_HURT.playSound(getPlayer());
			if (attacked.add(player.getUniqueId())) {
				player.damage(player.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue() * .65, getPlayer());
				Stun.apply(getGame().getParticipant(player), TimeUnit.TICKS, weaken ? 25 : 45);
			} else {
				player.damage(player.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue() * .45, getPlayer());
				Stun.apply(getGame().getParticipant(player), TimeUnit.TICKS, weaken ? 15 : 35);
			}
			new AbilityTimer(40) {
				@Override
				protected void run(int count) {
					if (getPlayer().getLocation().distanceSquared(player.getLocation()) <= 4) {
						getPlayer().setVelocity(new Vector());
						stop(false);
					}
				}
			}.setPeriod(TimeUnit.TICKS, 1).start();
		}

		@Override
		protected void run(int count) {
			final Location target = this.target.get();
			if (!getPlayer().getWorld().equals(target.getWorld())) {
				stop(true);
				return;
			}
			getPlayer().setFallDistance(0f);
			if (getPlayer().getLocation().distanceSquared(target) <= 6) {
				if (!arrived) {
					this.arrived = true;
					if (afterMove != null) afterMove.run();
					SoundLib.BLOCK_METAL_STEP.playSound(getPlayer());
					if (getCount() < 140) {
						setCount(140);
					}
					return;
				}
			} else {
				this.arrived = false;
			}
			getPlayer().setVelocity(VectorUtil.validateVector(target.toVector().subtract(getPlayer().getLocation().toVector()).normalize()).multiply(arrived ? .1 : 2));
		}

		@Override
		protected void onEnd() {
			onSilentEnd();
		}

		@Override
		protected void onSilentEnd() {
			entityHook.die();
			ManeuverGear.this.move = null;
		}
	}

	@Override
	protected void onUpdate(Update update) {
		if (update == Update.RESTRICTION_CLEAR) {
			charge.actionbarChannel.update(charge.toString());
		}
	}

	protected abstract class Hook extends AbilityTimer {

		private final CustomEntity entity;
		private final Vector forward;

		public Hook(Location startLocation, Vector hookVelocity) {
			super(5);
			setPeriod(TimeUnit.TICKS, 1);
			this.entity = new Hook.ArrowEntity(startLocation.getWorld(), startLocation.getX(), startLocation.getY(), startLocation.getZ()).resizeBoundingBox(-.75, -.75, -.75, .75, .75, .75);
			this.forward = hookVelocity.multiply(10);
			this.lastLocation = startLocation;
			start();
		}

		protected abstract boolean consume(double x, double y, double z);
		protected abstract boolean consume(Player player);

		private Location lastLocation;

		@Override
		protected void run(int i) {
			final Location newLocation = lastLocation.clone().add(forward);
			for (Iterator<Location> iterator = new Iterator<Location>() {
				private final Vector vectorBetween = newLocation.toVector().subtract(lastLocation.toVector()), unit = vectorBetween.clone().normalize().multiply(.1);
				private final int amount = (int) (vectorBetween.length() / 0.1);
				private int cursor = 0;

				@Override
				public boolean hasNext() {
					return cursor < amount;
				}

				@Override
				public Location next() {
					if (cursor >= amount) throw new NoSuchElementException();
					cursor++;
					return lastLocation.clone().add(unit.clone().multiply(cursor));
				}
			}; iterator.hasNext(); ) {
				final Location location = iterator.next();
				entity.setLocation(location);
				final Block block = location.getBlock();
				final Material type = block.getType();
				if (type.isSolid()) {
					if (RayTrace.hitsBlock(location.getWorld(), lastLocation.getX(), lastLocation.getY(), lastLocation.getZ(), location.getX(), location.getY(), location.getZ())) {
						if (consume(location.getX(), location.getY(), location.getZ())) {
							stop(false);
						}
						return;
					}
				}
				for (Player player : LocationUtil.getConflictingEntities(Player.class, getPlayer().getWorld(), entity.getBoundingBox(), predicate)) {
					if (player.getLocation().distanceSquared(getPlayer().getLocation()) <= 9) continue;
					if (consume(player)) {
						stop(false);
						return;
					}
					break;
				}
				ParticleLib.REDSTONE.spawnParticle(getPlayer(), location, RGB.BLACK);
			}
			this.lastLocation = newLocation;
		}

		@Override
		protected void onEnd() {
			onSilentEnd();
		}

		@Override
		protected void onSilentEnd() {
			entity.remove();
			ManeuverGear.this.hook = null;
		}

		public class ArrowEntity extends CustomEntity implements Deflectable {

			public ArrowEntity(World world, double x, double y, double z) {
				getGame().super(world, x, y, z);
			}

			@Override
			public Vector getDirection() {
				return forward.clone();
			}

			@Override
			public void onDeflect(Participant deflector, Vector newDirection) {
				stop(false);
				new Hook(lastLocation, newDirection) {
					@Override
					protected boolean consume(double x, double y, double z) {
						return Hook.this.consume(x, y, z);
					}

					@Override
					protected boolean consume(Player player) {
						return Hook.this.consume(player);
					}
				}.start();
			}

			@Override
			public ProjectileSource getShooter() {
				return getPlayer();
			}

			@Override
			protected void onRemove() {
				Hook.this.stop(false);
			}

		}

	}

}
