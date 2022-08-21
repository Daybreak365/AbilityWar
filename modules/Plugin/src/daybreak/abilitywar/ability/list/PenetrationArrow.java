package daybreak.abilitywar.ability.list;

import com.google.common.base.Strings;
import daybreak.abilitywar.ability.AbilityBase;
import daybreak.abilitywar.ability.AbilityManifest;
import daybreak.abilitywar.ability.AbilityManifest.Rank;
import daybreak.abilitywar.ability.AbilityManifest.Species;
import daybreak.abilitywar.ability.SubscribeEvent;
import daybreak.abilitywar.ability.SubscribeEvent.Priority;
import daybreak.abilitywar.config.ability.AbilitySettings.SettingObject;
import daybreak.abilitywar.game.AbstractGame;
import daybreak.abilitywar.game.AbstractGame.CustomEntity;
import daybreak.abilitywar.game.AbstractGame.Participant;
import daybreak.abilitywar.game.AbstractGame.Participant.ActionbarNotification.ActionbarChannel;
import daybreak.abilitywar.game.GameManager;
import daybreak.abilitywar.game.manager.effect.Stun;
import daybreak.abilitywar.game.module.DeathManager;
import daybreak.abilitywar.game.module.Wreck;
import daybreak.abilitywar.game.team.interfaces.Teamable;
import daybreak.abilitywar.utils.base.ProgressBar;
import daybreak.abilitywar.utils.base.color.RGB;
import daybreak.abilitywar.utils.base.concurrent.TimeUnit;
import daybreak.abilitywar.utils.base.math.LocationUtil;
import daybreak.abilitywar.utils.base.math.geometry.Sphere;
import daybreak.abilitywar.utils.base.minecraft.damage.Damages;
import daybreak.abilitywar.utils.base.minecraft.entity.decorator.Deflectable;
import daybreak.abilitywar.utils.base.random.Random;
import daybreak.abilitywar.utils.library.ParticleLib;
import daybreak.abilitywar.utils.library.SoundLib;
import daybreak.abilitywar.utils.library.item.EnchantLib;
import daybreak.abilitywar.utils.library.item.ItemLib;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Note;
import org.bukkit.World;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Damageable;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.projectiles.ProjectileSource;
import org.bukkit.util.Vector;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.function.Predicate;

@AbilityManifest(name = "관통화살", rank = Rank.S, species = Species.OTHERS, explain = {
		"활을 쏘면 벽과 생명체를 통과하며 특수한 능력이 있는 발사체를 쏩니다.",
		"탄창에는 $[AMMO_SIZE_CONFIG]개의 탄약이 들어있습니다. 탄약을 모두 소진하면 3초간 재장전하며,",
		"임의의 능력을 가진 탄약으로 탄창이 다시 채워집니다.",
		"§c절단§f: 대상에게 추가 근접 대미지를 입힙니다.",
		"§5중력§f: 대상을 0.5초간 기절시키고, 대상 주위 4칸의 생명체를 대상에게 끌어갑니다.",
		"§e풍월§f: 대상을 멀리 밀쳐냅니다."
}, summarize = {
		"화살 대신 지형지물과 생명체를 §3관통§f하는 특수 투사체를 발사합니다.",
		"§4[§c절단§4] §f대상에게 추가 근접 피해를 입힙니다.",
		"§5[§d중력§5] §f대상을 0.5초간 §e기절§f시키고 대상 주변 생명체를 끌어모읍니다.",
		"§6[§e풍월§6] §f대상을 멀리 밀쳐냅니다."
})
public class PenetrationArrow extends AbilityBase {

	public static final SettingObject<Integer> AMMO_SIZE_CONFIG = abilitySettings.new SettingObject<Integer>(PenetrationArrow.class, "ammo-size", 4,
			"# 탄창 크기") {

		@Override
		public boolean condition(Integer value) {
			return value >= 1;
		}

	};

	private static final RGB RED = new RGB(219, 64, 66), PURPLE = new RGB(138, 9, 173), YELLOW = new RGB(255, 246, 122);
	private static final Sphere sphere = Sphere.of(4, 10);
	private static final double GRAVITATIONAL_CONSTANT = 3;

	private final Random random = new Random();
	private final List<ArrowType> arrowTypes = Arrays.asList(
			new ArrowType(ChatColor.RED, "절단") {
				@Override
				protected void launchArrow(Arrow arrow, int powerLevel) {
					SoundLib.ENTITY_ARROW_SHOOT.playSound(getPlayer());
					new Parabola(getPlayer(), OnHitBehavior.CUT, arrow.getLocation(), arrow.getVelocity(), getPlayer().getLocation().getPitch(), powerLevel, RED).start();
				}
			},
			new ArrowType(ChatColor.DARK_PURPLE, "중력") {
				@Override
				protected void launchArrow(Arrow arrow, int powerLevel) {
					SoundLib.ENTITY_ARROW_SHOOT.playSound(getPlayer());
					SoundLib.PIANO.playInstrument(getPlayer(), Note.flat(0, Note.Tone.D));
					new Parabola(getPlayer(), OnHitBehavior.GRAVITY, arrow.getLocation(), arrow.getVelocity(), getPlayer().getLocation().getPitch(), powerLevel, PURPLE).start();
				}
			},
			new ArrowType(ChatColor.YELLOW, "풍월") {
				@Override
				protected void launchArrow(Arrow arrow, int powerLevel) {
					SoundLib.ENTITY_ARROW_SHOOT.playSound(getPlayer());
					SoundLib.PIANO.playInstrument(getPlayer(), Note.flat(1, Note.Tone.B));
					new Parabola(getPlayer(), OnHitBehavior.WIND, arrow.getLocation(), arrow.getVelocity(), getPlayer().getLocation().getPitch(), powerLevel, YELLOW).start();
				}
			}
	);

	private class Ammo {

		private final int ammoSize = AMMO_SIZE_CONFIG.getValue();
		private final LinkedList<ArrowType> ammo = new LinkedList<>();

		private Ammo() {
			reload();
		}

		private void reload() {
			ammo.clear();
			for (int i = 0; i < ammoSize; i++) {
				ammo.add(random.pick(arrowTypes));
			}
		}

		private ArrowType poll() {
			return ammo.poll();
		}

		private boolean hasAmmo() {
			return !ammo.isEmpty();
		}

		@Override
		public String toString() {
			final StringBuilder builder = new StringBuilder();
			for (ArrowType type : ammo) {
				builder.append(type.color).append("▐");
			}
			builder.append(ChatColor.GRAY.toString()).append(Strings.repeat("▐", ammoSize - ammo.size()));
			return builder.toString();
		}
	}

	private final ActionbarChannel actionbarChannel = newActionbarChannel();
	private final Ammo ammo = new Ammo();
	private AbilityTimer reload = null;

	public PenetrationArrow(AbstractGame.Participant participant) {
		super(participant);
	}

	@Override
	protected void onUpdate(Update update) {
		if (update == Update.RESTRICTION_CLEAR) {
			actionbarChannel.update(ammo.toString());
		}
	}

	@SubscribeEvent(ignoreCancelled = true, priority = Priority.HIGHEST)
	private void onProjectileLaunch(EntityShootBowEvent e) {
		if (getPlayer().equals(e.getEntity()) && e.getProjectile() instanceof Arrow) {
			e.setCancelled(true);
			if (reload == null) {
				if (!ammo.hasAmmo()) {
					startReload();
					return;
				}
				if (!getPlayer().getGameMode().equals(GameMode.CREATIVE) && (!e.getBow().hasItemMeta() || !e.getBow().getItemMeta().hasEnchant(Enchantment.ARROW_INFINITE))) {
					ItemLib.removeItem(getPlayer().getInventory(), Material.ARROW, 1);
				}
				ammo.poll().launchArrow((Arrow) e.getProjectile(), e.getBow().getEnchantmentLevel(Enchantment.ARROW_DAMAGE));
				actionbarChannel.update(ammo.toString());
				if (!ammo.hasAmmo()) {
					startReload();
				}
			} else {
				getPlayer().sendMessage("§b재장전 §f중입니다.");
			}
		}
	}

	private void startReload() {
		final int reloadCount = Wreck.isEnabled(GameManager.getGame()) ? (int) (Wreck.calculateDecreasedAmount(70) * 15.0) : 15;
		this.reload = new AbilityTimer(reloadCount) {
			private final ProgressBar progressBar = new ProgressBar(reloadCount, 15);

			@Override
			protected void run(int count) {
				progressBar.step();
				actionbarChannel.update("재장전: " + progressBar.toString());
			}

			@Override
			protected void onEnd() {
				ammo.reload();
				PenetrationArrow.this.reload = null;
				actionbarChannel.update(ammo.toString());
			}
		}.setPeriod(TimeUnit.TICKS, 4).setBehavior(RestrictionBehavior.PAUSE_RESUME);
		reload.start();
	}

	public interface OnHitBehavior {
		OnHitBehavior CUT = new OnHitBehavior() {
			@Override
			public void onHit(PenetrationArrow ability, Damageable damager, Damageable victim) {
				ParticleLib.SWEEP_ATTACK.spawnParticle(victim.getLocation(), 1, 1, 1, 3);
				if (victim instanceof LivingEntity) {
					((LivingEntity) victim).setNoDamageTicks(0);
				}
				victim.damage(5, damager);
			}
		};
		OnHitBehavior GRAVITY = new OnHitBehavior() {
			@Override
			public void onHit(PenetrationArrow ability, Damageable damager, Damageable victim) {
				for (Location location : sphere.toLocations(victim.getLocation())) {
					ParticleLib.REDSTONE.spawnParticle(location, PURPLE);
				}
				for (LivingEntity entity : LocationUtil.getNearbyEntities(LivingEntity.class, victim.getLocation(), 4, 4, new Predicate<Entity>() {
					@Override
					public boolean test(Entity entity) {
						if (entity.equals(damager)) return false;
						if (entity instanceof Player) {
							if (!ability.getGame().isParticipating(entity.getUniqueId())
									|| (ability.getGame() instanceof DeathManager.Handler && ((DeathManager.Handler) ability.getGame()).getDeathManager().isExcluded(entity.getUniqueId()))
									|| !ability.getGame().getParticipant(entity.getUniqueId()).attributes().TARGETABLE.getValue()) {
								return false;
							}
							if (ability.getGame() instanceof Teamable) {
								final Teamable teamGame = (Teamable) ability.getGame();
								final Participant entityParticipant = teamGame.getParticipant(entity.getUniqueId()), participant = teamGame.getParticipant(damager.getUniqueId());
								return participant == null || !teamGame.hasTeam(entityParticipant) || !teamGame.hasTeam(participant) || (!teamGame.getTeam(entityParticipant).equals(teamGame.getTeam(participant)));
							}
						}
						return true;
					}
				})) {
					entity.setVelocity(victim.getLocation().toVector().subtract(entity.getLocation().toVector()).multiply(0.75));
				}
				final Participant participant = ability.getGame().getParticipant(victim.getUniqueId());
				if (participant != null) {
					Stun.apply(participant, TimeUnit.TICKS, 10);
				}
			}
		};
		OnHitBehavior WIND = new OnHitBehavior() {
			@Override
			public void onHit(PenetrationArrow ability, Damageable damager, Damageable victim) {
				Vector vector = damager.getLocation().toVector().subtract(victim.getLocation().toVector()).multiply(-1);
				if (vector.length() > 0.01) {
					vector.normalize().multiply(2);
				}
				victim.setVelocity(vector.setY(0));
			}
		};

		void onHit(PenetrationArrow ability, Damageable damager, Damageable victim);
	}

	private abstract static class ArrowType {

		private final ChatColor color;
		private final String name;

		private ArrowType(ChatColor color, String name) {
			this.color = color;
			this.name = color.toString() + name;
		}

		protected abstract void launchArrow(Arrow arrow, int powerEnchant);

	}

	public class Parabola extends AbilityTimer {

		private final LivingEntity shooter;
		private final OnHitBehavior onHitBehavior;
		private final CustomEntity entity;
		private final double velocity;
		private final int powerEnchant;
		private final Vector forward;
		private final Predicate<Entity> predicate;

		private final RGB color;
		private final Set<Damageable> attacked = new HashSet<>();
		private Location lastLocation;
		private double time;
		private Parabola(LivingEntity shooter, OnHitBehavior onHitBehavior, Location startLocation, Vector arrowVelocity, double angle, int powerEnchant, RGB color) {
			super(300);
			setPeriod(TimeUnit.TICKS, 1);
			this.shooter = shooter;
			this.onHitBehavior = onHitBehavior;
			this.entity = new ArrowEntity(startLocation.getWorld(), startLocation.getX(), startLocation.getY(), startLocation.getZ()).resizeBoundingBox(-0.5, -0.5, -0.5, 0.5, 0.5, 0.5);
			this.velocity = Math.sqrt((arrowVelocity.getX() * arrowVelocity.getX()) + (arrowVelocity.getY() * arrowVelocity.getY()) + (arrowVelocity.getZ() * arrowVelocity.getZ()));
			this.powerEnchant = powerEnchant;
			this.forward = arrowVelocity.setY(arrowVelocity.getY() * 0.7);
			this.color = color;
			this.lastLocation = startLocation;
			this.time = Math.max(0, (angle + 135) / 360);
			this.predicate = new Predicate<Entity>() {
				@Override
				public boolean test(Entity entity) {
					if (entity.equals(shooter)) return false;
					if (entity instanceof Player) {
						if (!getGame().isParticipating(entity.getUniqueId())
								|| (getGame() instanceof DeathManager.Handler && ((DeathManager.Handler) getGame()).getDeathManager().isExcluded(entity.getUniqueId()))
								|| !getGame().getParticipant(entity.getUniqueId()).attributes().TARGETABLE.getValue()) {
							return false;
						}
						if (getGame() instanceof Teamable) {
							final Teamable teamGame = (Teamable) getGame();
							final Participant entityParticipant = teamGame.getParticipant(entity.getUniqueId()), participant = teamGame.getParticipant(shooter.getUniqueId());
							if (participant != null) {
								return !teamGame.hasTeam(entityParticipant) || !teamGame.hasTeam(participant) || (!teamGame.getTeam(entityParticipant).equals(teamGame.getTeam(participant)));
							}
						}
					}
					return true;
				}
			};
		}

		@Override
		protected void run(int i) {
			time += 0.025;
			double height = -0.5 * GRAVITATIONAL_CONSTANT * (time * time);
			Location newLocation = lastLocation.clone().add(forward).add(0, height, 0);
			for (Iterator<Location> iterator = new Iterator<Location>() {
				private final Vector vectorBetween = newLocation.toVector().subtract(lastLocation.toVector()), unit = vectorBetween.clone().normalize().multiply(.2);
				private final int amount = (int) (vectorBetween.length() / 0.2);
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
				Location location = iterator.next();
				if (location.getY() < 0) {
					stop(false);
					return;
				}
				entity.setLocation(location);
				for (Damageable damageable : LocationUtil.getConflictingEntities(Damageable.class, shooter.getWorld(), entity.getBoundingBox(), predicate)) {
					if (damageable.isValid() && !damageable.isDead() && !shooter.equals(damageable) && !attacked.contains(damageable)) {
						Damages.damageArrow(damageable, getPlayer(), EnchantLib.getDamageWithPowerEnchantment(Math.round(2.5f * velocity * 10) / 10.0f, powerEnchant));
						onHitBehavior.onHit(PenetrationArrow.this, shooter, damageable);
						attacked.add(damageable);
					}
				}
				ParticleLib.REDSTONE.spawnParticle(location, color);
			}
			lastLocation = newLocation;
		}

		@Override
		protected void onEnd() {
			entity.remove();
		}

		@Override
		protected void onSilentEnd() {
			entity.remove();
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
				Player deflectedPlayer = deflector.getPlayer();
				new Parabola(deflectedPlayer, onHitBehavior, lastLocation, newDirection, getPlayer().getLocation().getDirection().getY() * 90, powerEnchant, color).start();
			}

			@Override
			public ProjectileSource getShooter() {
				return shooter;
			}

			@Override
			protected void onRemove() {
				Parabola.this.stop(false);
			}

		}

	}

}
