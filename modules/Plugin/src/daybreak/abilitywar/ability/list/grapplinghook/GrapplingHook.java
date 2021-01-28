package daybreak.abilitywar.ability.list.grapplinghook;

import com.google.common.base.Strings;
import daybreak.abilitywar.ability.AbilityBase;
import daybreak.abilitywar.ability.AbilityManifest;
import daybreak.abilitywar.ability.AbilityManifest.Rank;
import daybreak.abilitywar.ability.AbilityManifest.Species;
import daybreak.abilitywar.ability.SubscribeEvent;
import daybreak.abilitywar.ability.decorator.ActiveHandler;
import daybreak.abilitywar.config.ability.AbilitySettings.SettingObject;
import daybreak.abilitywar.game.AbstractGame.CustomEntity;
import daybreak.abilitywar.game.AbstractGame.Participant;
import daybreak.abilitywar.game.AbstractGame.Participant.ActionbarNotification.ActionbarChannel;
import daybreak.abilitywar.game.manager.effect.Stun;
import daybreak.abilitywar.game.module.DeathManager;
import daybreak.abilitywar.game.module.Wreck;
import daybreak.abilitywar.game.team.interfaces.Teamable;
import daybreak.abilitywar.utils.base.concurrent.TimeUnit;
import daybreak.abilitywar.utils.base.math.LocationUtil;
import daybreak.abilitywar.utils.base.math.VectorUtil;
import daybreak.abilitywar.utils.base.minecraft.ability.list.grapplinghook.HookEntity;
import daybreak.abilitywar.utils.base.minecraft.entity.decorator.Deflectable;
import daybreak.abilitywar.utils.base.minecraft.raytrace.RayTrace;
import daybreak.abilitywar.utils.library.ParticleLib;
import daybreak.abilitywar.utils.base.color.RGB;
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

@AbilityManifest(name = "그래플링 훅", rank = Rank.L, species = Species.HUMAN, explain = {
		"후크는 한 번에 최대 4개를 보유할 수 있으며, 첫 번째 후크 사용 후 35초가 지나면",
		"후크 4개가 모두 충전됩니다. 철괴를 우클릭하면 바라보는 방향으로 후크를",
		"발사합니다. 후크가 블록에 고정되면 빠르게 해당 위치로 이동하며, 웅크려서 이동을",
		"취소하고 그 자리에 멈출 수 있습니다. 목적지에 도착했을 때 그 자리에 최대 6초간",
		"고정되며, 웅크려서 후크 고정을 풀고 바라보는 방향으로 짧게 돌진할 수 있습니다.",
		"5칸 이내의 플레이어를 바라본 상태로 돌진했다면, 해당 플레이어를 기절시키고",
		"최대 체력에 비례하여 대미지를 입힙니다. 낙하 대미지를 받지 않습니다."
})
public class GrapplingHook extends AbilityBase implements ActiveHandler {

	public static final SettingObject<Integer> MAX_CHARGE = abilitySettings.new SettingObject<Integer>(GrapplingHook.class, "max-charge", 4,
			"# 후크 최대 충전",
			"# 기본값: 4") {

		@Override
		public boolean condition(Integer value) {
			return value >= 1;
		}

	};

	public GrapplingHook(Participant participant) {
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
			super(TaskType.REVERSE, (int) (35 * Wreck.calculateDecreasedAmount(25)));
			setBehavior(RestrictionBehavior.PAUSE_RESUME);
		}

		private boolean subtractCharge() {
			if (charges > 0) {
				charges = Math.max(0, charges - 1);
				start();
				actionbarChannel.update(toString());
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
									if (GrapplingHook.this.charge.subtractCharge()) {
										GrapplingHook.this.move = new Move(new Location(getPlayer().getWorld(), x, y, z));
										SoundLib.ENTITY_FISHING_BOBBER_RETRIEVE.playSound(getPlayer());
									}
								}
								return true;
							}

							@Override
							public boolean consume(Player player) {
								return false;
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
			if (move.dash) {
				move.stop(false);
				final Player lookingAt = LocationUtil.getEntityLookingAt(Player.class, getPlayer(), 5, .5, predicate);
				getPlayer().setVelocity(getPlayer().getLocation().getDirection().multiply(1.65));
				if (lookingAt != null) {
					SoundLib.ENTITY_PLAYER_ATTACK_KNOCKBACK.playSound(getPlayer());
					SoundLib.ENTITY_PLAYER_HURT.playSound(getPlayer());
					if (attacked.add(lookingAt.getUniqueId())) {
						lookingAt.damage(lookingAt.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue() * .45, getPlayer());
						Stun.apply(getGame().getParticipant(lookingAt), TimeUnit.TICKS, 35);
					} else {
						lookingAt.damage(lookingAt.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue() * .2, getPlayer());
						Stun.apply(getGame().getParticipant(lookingAt), TimeUnit.TICKS, 20);
					}
				}
			} else {
				move.stop(true);
			}
		}
	}

	private class Move extends AbilityTimer {

		private final Location targetLoc;
		private final HookEntity entityHook;
		private boolean dash = false;

		private Move(final Location targetLoc) {
			super(TaskType.NORMAL, 260);
			setPeriod(TimeUnit.TICKS, 1);
			this.targetLoc = targetLoc;
			this.entityHook = Hooks.createHook(getPlayer(), targetLoc);
			start();
		}

		@Override
		protected void run(int count) {
			if (!getPlayer().getWorld().equals(targetLoc.getWorld())) {
				stop(true);
				return;
			}
			getPlayer().setFallDistance(0f);
			if (getPlayer().getLocation().distanceSquared(targetLoc) <= 4) {
				if (!dash) {
					this.dash = true;
					SoundLib.BLOCK_METAL_STEP.playSound(getPlayer());
					if (getCount() < 140) {
						setCount(140);
					}
					return;
				}
			} else {
				this.dash = false;
			}
			getPlayer().setVelocity(VectorUtil.validateVector(targetLoc.toVector().subtract(getPlayer().getLocation().toVector()).normalize()).multiply(dash ? .1 : 1));
		}

		@Override
		protected void onEnd() {
			onSilentEnd();
		}

		@Override
		protected void onSilentEnd() {
			entityHook.die();
			GrapplingHook.this.move = null;
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
			super(8);
			setPeriod(TimeUnit.TICKS, 1);
			this.entity = new ArrowEntity(startLocation.getWorld(), startLocation.getX(), startLocation.getY(), startLocation.getZ()).resizeBoundingBox(-.75, -.75, -.75, .75, .75, .75);
			this.forward = hookVelocity.multiply(6);
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
			GrapplingHook.this.hook = null;
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
