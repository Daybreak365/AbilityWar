package daybreak.abilitywar.game.list.mix.synergy.list;

import com.google.common.collect.Iterables;
import daybreak.abilitywar.ability.AbilityManifest;
import daybreak.abilitywar.ability.AbilityManifest.Rank;
import daybreak.abilitywar.ability.AbilityManifest.Species;
import daybreak.abilitywar.ability.SubscribeEvent;
import daybreak.abilitywar.ability.SubscribeEvent.Priority;
import daybreak.abilitywar.game.AbstractGame.CustomEntity;
import daybreak.abilitywar.game.AbstractGame.Participant;
import daybreak.abilitywar.game.AbstractGame.Participant.ActionbarNotification.ActionbarChannel;
import daybreak.abilitywar.game.GameManager;
import daybreak.abilitywar.game.list.mix.synergy.Synergy;
import daybreak.abilitywar.game.manager.effect.Stun;
import daybreak.abilitywar.game.module.DeathManager;
import daybreak.abilitywar.game.module.Wreck;
import daybreak.abilitywar.game.team.interfaces.Teamable;
import daybreak.abilitywar.utils.base.ProgressBar;
import daybreak.abilitywar.utils.base.color.RGB;
import daybreak.abilitywar.utils.base.concurrent.TimeUnit;
import daybreak.abilitywar.utils.base.math.LocationUtil;
import daybreak.abilitywar.utils.base.math.VectorUtil;
import daybreak.abilitywar.utils.base.math.geometry.Circle;
import daybreak.abilitywar.utils.base.math.geometry.Sphere;
import daybreak.abilitywar.utils.base.minecraft.damage.Damages;
import daybreak.abilitywar.utils.base.minecraft.entity.decorator.Deflectable;
import daybreak.abilitywar.utils.base.minecraft.version.ServerVersion;
import daybreak.abilitywar.utils.library.ParticleLib;
import daybreak.abilitywar.utils.library.PotionEffects;
import daybreak.abilitywar.utils.library.SoundLib;
import daybreak.abilitywar.utils.library.item.EnchantLib;
import daybreak.abilitywar.utils.library.item.ItemLib;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Note;
import org.bukkit.World;
import org.bukkit.block.Block;
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
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Random;
import java.util.Set;
import java.util.function.Predicate;

@AbilityManifest(name = "관통 스나이퍼", rank = Rank.S, species = Species.HUMAN, explain = {
		"활을 쏠 때 매우 빠른 속도로 나아가고 벽과 생명체를 통과하는 특수한",
		"투사체를 쏩니다. 투사체에는 특수한 능력이 있으며, 재장전할 때마다",
		"능력이 변경됩니다. 투사체를 쏘고 난 후 일정 시간동안 재장전을 하며,",
		"재장전 중에는 활을 쏠 수 없습니다. 활을 들고 있을 경우 빠르게 이동할",
		"수 없으며, 이동이 제한됩니다."
})
public class PenetrationSniper extends Synergy {

	private static final Material GLASS_PANE = ServerVersion.getVersion() > 12 ? Material.valueOf("GLASS_PANE") : Material.valueOf("THIN_GLASS");
	private static final RGB BULLET_COLOR = new RGB(43, 209, 224);
	private static final RGB RED = new RGB(219, 64, 66);
	private static final RGB PURPLE = new RGB(138, 9, 173);
	private static final RGB YELLOW = new RGB(255, 246, 122);
	private static final Sphere sphere = Sphere.of(4, 10);
	private static final Circle CIRCLE = Circle.of(0.5, 15);

	private final AbilityTimer snipeMode = new AbilityTimer() {
		@Override
		protected void run(int count) {
			final Material main = getPlayer().getInventory().getItemInMainHand().getType(), off = getPlayer().getInventory().getItemInOffHand().getType();
			if (main.equals(Material.BOW) || off.equals(Material.BOW) || (ServerVersion.getVersion() >= 14 && (main.equals(Material.CROSSBOW) || off.equals(Material.CROSSBOW)))) {
				PotionEffects.SLOW.addPotionEffect(getPlayer(), 2, 3, true);
				getPlayer().setVelocity(getPlayer().getVelocity().setX(0).setY(Math.min(0, getPlayer().getVelocity().getY())).setZ(0));
			}
		}
	}.setPeriod(TimeUnit.TICKS, 1).register();

	@Override
	protected void onUpdate(Update update) {
		if (update == Update.RESTRICTION_CLEAR) {
			snipeMode.start();
		}
	}

	private final Random random = new Random();
	private final List<ArrowType> arrowTypes = Arrays.asList(
			new ArrowType(ChatColor.RED + "절단") {
				@Override
				protected void launchArrow(Arrow arrow, int powerLevel) {
					SoundLib.ENTITY_ARROW_SHOOT.playSound(getPlayer());
					new Bullet(getPlayer(), arrow.getLocation(), arrow.getVelocity(), powerLevel, OnHitBehavior.CUT, BULLET_COLOR, RED).start();
				}
			},
			new ArrowType(ChatColor.DARK_PURPLE + "중력") {
				@Override
				protected void launchArrow(Arrow arrow, int powerLevel) {
					SoundLib.ENTITY_ARROW_SHOOT.playSound(getPlayer());
					SoundLib.PIANO.playInstrument(getPlayer(), Note.flat(0, Note.Tone.D));
					new Bullet(getPlayer(), arrow.getLocation(), arrow.getVelocity(), powerLevel, OnHitBehavior.GRAVITY, BULLET_COLOR, PURPLE).start();
				}
			},
			new ArrowType(ChatColor.YELLOW + "풍월") {
				@Override
				protected void launchArrow(Arrow arrow, int powerLevel) {
					SoundLib.ENTITY_ARROW_SHOOT.playSound(getPlayer());
					SoundLib.PIANO.playInstrument(getPlayer(), Note.flat(1, Note.Tone.B));
					new Bullet(getPlayer(), arrow.getLocation(), arrow.getVelocity(), powerLevel, OnHitBehavior.WIND, BULLET_COLOR, YELLOW).start();
				}
			}
	);
	private final ActionbarChannel actionbarChannel = newActionbarChannel();
	private ArrowType arrowType = arrowTypes.get(0);
	private AbilityTimer reload = null;

	public PenetrationSniper(Participant participant) {
		super(participant);
	}

	@SubscribeEvent(ignoreCancelled = true, priority = Priority.HIGHEST)
	public void onProjectileLaunch(EntityShootBowEvent e) {
		if (getPlayer().equals(e.getEntity()) && e.getProjectile() instanceof Arrow) {
			e.setCancelled(true);
			if (reload == null) {
				if (!getPlayer().getGameMode().equals(GameMode.CREATIVE) && (!e.getBow().hasItemMeta() || !e.getBow().getItemMeta().hasEnchant(Enchantment.ARROW_INFINITE))) {
					ItemLib.removeItem(getPlayer().getInventory(), Material.ARROW, 1);
				}
				arrowType.launchArrow((Arrow) e.getProjectile(), e.getBow().getEnchantmentLevel(Enchantment.ARROW_DAMAGE));
				SoundLib.ENTITY_GENERIC_EXPLODE.playSound(getPlayer().getLocation(), 7, 1.75f);
				final int reloadCount = Wreck.isEnabled(GameManager.getGame()) ? (int) (Wreck.calculateDecreasedAmount(20) * 25.0) : 25;
				this.reload = new AbilityTimer(reloadCount) {
					private final ProgressBar progressBar = new ProgressBar(reloadCount, 15);

					@Override
					protected void run(int count) {
						progressBar.step();
						actionbarChannel.update("재장전: " + progressBar.toString());
					}

					@Override
					protected void onEnd() {
						arrowType = arrowTypes.get(random.nextInt(arrowTypes.size()));
						PenetrationSniper.this.reload = null;
						actionbarChannel.update("§f능력: " + arrowType.name);
						SoundLib.BLOCK_STONE_PRESSURE_PLATE_CLICK_ON.playSound(getPlayer());
					}
				}.setPeriod(TimeUnit.TICKS, 2).setBehavior(RestrictionBehavior.PAUSE_RESUME);
				reload.start();
			} else {
				getPlayer().sendMessage("§b재장전 §f중입니다.");
			}
		}
	}

	private abstract static class ArrowType {

		private final String name;

		private ArrowType(String name) {
			this.name = name;
		}

		protected abstract void launchArrow(Arrow arrow, int powerEnchant);

	}

	public interface OnHitBehavior {
		OnHitBehavior CUT = new OnHitBehavior() {
			@Override
			public void onHit(PenetrationSniper ability, Damageable damager, Damageable victim) {
				ParticleLib.SWEEP_ATTACK.spawnParticle(victim.getLocation(), 1, 1, 1, 3);
				if (victim instanceof LivingEntity) {
					((LivingEntity) victim).setNoDamageTicks(0);
				}
				victim.damage(10, damager);
			}
		};
		OnHitBehavior GRAVITY = new OnHitBehavior() {
			@Override
			public void onHit(PenetrationSniper ability, Damageable damager, Damageable victim) {
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
					entity.setVelocity(victim.getLocation().toVector().subtract(entity.getLocation().toVector()).multiply(1.25));
				}
				final Participant participant = ability.getGame().getParticipant(victim.getUniqueId());
				if (participant != null) {
					Stun.apply(participant, TimeUnit.TICKS, 10);
				}
			}
		};
		OnHitBehavior WIND = new OnHitBehavior() {
			@Override
			public void onHit(PenetrationSniper ability, Damageable damager, Damageable victim) {
				Vector vector = damager.getLocation().toVector().subtract(victim.getLocation().toVector()).multiply(-1);
				if (vector.length() > 0.01) {
					vector.normalize().multiply(3);
				}
				victim.setVelocity(vector.setY(0));
			}
		};

		void onHit(PenetrationSniper ability, Damageable damager, Damageable victim);
	}

	public class Bullet extends AbilityTimer {

		private final LivingEntity shooter;
		private final CustomEntity entity;
		private final Vector forward;
		private final Iterator<Vector> circle;
		private final OnHitBehavior onHitBehavior;
		private final int powerEnchant;

		private final RGB color, abilityColor;
		private final Set<Damageable> attacked = new HashSet<>();
		private Location lastLocation;
		private final Predicate<Entity> predicate;

		private Bullet(LivingEntity shooter, Location startLocation, Vector arrowVelocity, int powerEnchant, OnHitBehavior onHitBehavior, RGB color, RGB abilityColor) {
			super(80);
			setPeriod(TimeUnit.TICKS, 1);
			this.shooter = shooter;
			this.entity = new ArrowEntity(startLocation.getWorld(), startLocation.getX(), startLocation.getY(), startLocation.getZ()).resizeBoundingBox(-.75, -.75, -.75, .75, .75, .75);
			this.forward = arrowVelocity.multiply(10);
			this.circle = Iterables.cycle(CIRCLE.clone().rotateAroundAxisY(-shooter.getLocation().getYaw()).rotateAroundAxis(VectorUtil.rotateAroundAxisY(shooter.getLocation().getDirection().setY(0).normalize(), 90), shooter.getLocation().getPitch() + 90)).iterator();
			this.onHitBehavior = onHitBehavior;
			this.powerEnchant = powerEnchant;
			this.color = color;
			this.abilityColor = abilityColor;
			this.lastLocation = startLocation;
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
			Location newLocation = lastLocation.clone().add(forward);
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
				Location location = iterator.next();
				entity.setLocation(location);
				Block block = location.getBlock();
				Material type = block.getType();
				if (type.isSolid() && ItemLib.STAINED_GLASS.compareType(type) || Material.GLASS == type || ItemLib.STAINED_GLASS_PANE.compareType(type) || type == GLASS_PANE) {
					block.breakNaturally();
					SoundLib.BLOCK_GLASS_BREAK.playSound(block.getLocation(), 3, 1);
				}
				for (Damageable damageable : LocationUtil.getConflictingEntities(Damageable.class, shooter.getWorld(), entity.getBoundingBox(), predicate)) {
					if (!shooter.equals(damageable) && damageable.isValid() && !damageable.isDead() && !attacked.contains(damageable)) {
						Damages.damageArrow(damageable, shooter, (float) EnchantLib.getDamageWithPowerEnchantment(Math.min((forward.getX() * forward.getX()) + (forward.getY() * forward.getY()) + (forward.getZ() * forward.getZ()) / 10.0, 10), powerEnchant));
						onHitBehavior.onHit(PenetrationSniper.this, shooter, damageable);
						attacked.add(damageable);
					}
				}
				ParticleLib.REDSTONE.spawnParticle(location, color);
				if (i % 2 == 0) {
					ParticleLib.REDSTONE.spawnParticle(location.add(circle.next()), abilityColor);
				}
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
				new Bullet(deflector.getPlayer(), lastLocation, newDirection, powerEnchant, onHitBehavior, color, abilityColor).start();
			}

			@Override
			public ProjectileSource getShooter() {
				return shooter;
			}

			@Override
			protected void onRemove() {
				Bullet.this.stop(false);
			}
		}

	}

}