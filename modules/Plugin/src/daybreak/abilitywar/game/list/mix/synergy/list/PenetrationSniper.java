package daybreak.abilitywar.game.list.mix.synergy.list;

import com.google.common.collect.Iterables;
import daybreak.abilitywar.ability.AbilityManifest;
import daybreak.abilitywar.ability.AbilityManifest.Rank;
import daybreak.abilitywar.ability.AbilityManifest.Species;
import daybreak.abilitywar.ability.Scheduled;
import daybreak.abilitywar.ability.SubscribeEvent;
import daybreak.abilitywar.config.Configuration.Settings;
import daybreak.abilitywar.game.AbstractGame.CustomEntity;
import daybreak.abilitywar.game.AbstractGame.Participant;
import daybreak.abilitywar.game.AbstractGame.Participant.ActionbarNotification.ActionbarChannel;
import daybreak.abilitywar.game.GameManager;
import daybreak.abilitywar.game.list.mix.synergy.Synergy;
import daybreak.abilitywar.game.manager.object.WRECK;
import daybreak.abilitywar.utils.base.ProgressBar;
import daybreak.abilitywar.utils.base.concurrent.TimeUnit;
import daybreak.abilitywar.utils.base.math.LocationUtil;
import daybreak.abilitywar.utils.base.math.LocationUtil.Predicates;
import daybreak.abilitywar.utils.base.math.VectorUtil;
import daybreak.abilitywar.utils.base.math.geometry.Circle;
import daybreak.abilitywar.utils.base.math.geometry.Line;
import daybreak.abilitywar.utils.base.math.geometry.Sphere;
import daybreak.abilitywar.utils.base.minecraft.entity.decorator.Deflectable;
import daybreak.abilitywar.utils.base.minecraft.version.ServerVersion;
import daybreak.abilitywar.utils.library.ParticleLib;
import daybreak.abilitywar.utils.library.ParticleLib.RGB;
import daybreak.abilitywar.utils.library.PotionEffects;
import daybreak.abilitywar.utils.library.SoundLib;
import daybreak.abilitywar.utils.library.item.EnchantLib;
import daybreak.abilitywar.utils.library.item.ItemLib;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Set;
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

@AbilityManifest(name = "관통 스나이퍼", rank = Rank.S, species = Species.HUMAN, explain = {
		"활을 쏠 때 매우 빠른 속도로 나아가고 벽과 생명체를 통과하는 특수한",
		"투사체를 쏩니다. 투사체에는 특수한 능력이 있으며, 재장전할 때마다",
		"능력이 변경됩니다. 투사체를 쏘고 난 후 일정 시간동안 재장전을 하며,",
		"재장전 중에는 활을 쏠 수 없습니다. 활을 들고 있을 경우 빠르게 이동할",
		"수 없으며, 이동이 제한됩니다."
})
public class PenetrationSniper extends Synergy {

	private static final Material GLASS_PANE = ServerVersion.getVersionNumber() > 12 ? Material.valueOf("GLASS_PANE") : Material.valueOf("THIN_GLASS");
	private static final RGB BULLET_COLOR = new RGB(43, 209, 224);
	private static final RGB RED = new RGB(219, 64, 66);
	private static final RGB PURPLE = new RGB(138, 9, 173);
	private static final RGB YELLOW = new RGB(255, 246, 122);
	private static final Sphere sphere = Sphere.of(4, 10);
	private static final double GRAVITATIONAL_CONSTANT = 3;
	private static final Circle CIRCLE = Circle.of(0.5, 15);
	@Scheduled
	private final Timer snipeMode = new Timer() {
		@Override
		protected void run(int count) {
			Material main = getPlayer().getInventory().getItemInMainHand().getType();
			Material off = getPlayer().getInventory().getItemInOffHand().getType();
			if (main.equals(Material.BOW) || off.equals(Material.BOW) || (ServerVersion.getVersionNumber() >= 14 && (main.equals(Material.CROSSBOW) || off.equals(Material.CROSSBOW)))) {
				PotionEffects.SLOW.addPotionEffect(getPlayer(), 2, 3, true);
				getPlayer().setVelocity(getPlayer().getVelocity().setX(0).setY(Math.min(0, getPlayer().getVelocity().getY())).setZ(0));
			}
		}
	}.setPeriod(TimeUnit.TICKS, 1);
	private final Random random = new Random();
	private final List<ArrowType> arrowTypes = Arrays.asList(
			new ArrowType(ChatColor.RED + "절단") {
				@Override
				protected void launchArrow(Arrow arrow, int powerLevel) {
					SoundLib.ENTITY_ARROW_SHOOT.playSound(getPlayer());
					new Bullet<>(getPlayer(), arrow.getLocation(), arrow.getVelocity(), powerLevel, OnHitBehavior.CUT, BULLET_COLOR, RED).start();
				}
			},
			new ArrowType(ChatColor.DARK_PURPLE + "중력") {
				@Override
				protected void launchArrow(Arrow arrow, int powerLevel) {
					SoundLib.ENTITY_ARROW_SHOOT.playSound(getPlayer());
					SoundLib.PIANO.playInstrument(getPlayer(), Note.flat(0, Note.Tone.D));
					new Bullet<>(getPlayer(), arrow.getLocation(), arrow.getVelocity(), powerLevel, OnHitBehavior.GRAVITY, BULLET_COLOR, PURPLE).start();
				}
			},
			new ArrowType(ChatColor.YELLOW + "풍월") {
				@Override
				protected void launchArrow(Arrow arrow, int powerLevel) {
					SoundLib.ENTITY_ARROW_SHOOT.playSound(getPlayer());
					SoundLib.PIANO.playInstrument(getPlayer(), Note.flat(1, Note.Tone.B));
					new Bullet<>(getPlayer(), arrow.getLocation(), arrow.getVelocity(), powerLevel, OnHitBehavior.WIND, BULLET_COLOR, YELLOW).start();
				}
			}
	);
	private final ActionbarChannel actionbarChannel = newActionbarChannel();
	private ArrowType arrowType = arrowTypes.get(0);
	private Timer reload = null;

	public PenetrationSniper(Participant participant) {
		super(participant);
	}

	@SubscribeEvent
	public void onProjectileLaunch(EntityShootBowEvent e) {
		if (getPlayer().equals(e.getEntity()) && e.getProjectile() instanceof Arrow) {
			e.setCancelled(true);
			if (reload == null) {
				if (!getPlayer().getGameMode().equals(GameMode.CREATIVE) && (!e.getBow().hasItemMeta() || !e.getBow().getItemMeta().hasEnchant(Enchantment.ARROW_INFINITE))) {
					ItemLib.removeItem(getPlayer().getInventory(), Material.ARROW, 1);
				}
				arrowType.launchArrow((Arrow) e.getProjectile(), e.getBow().getEnchantmentLevel(Enchantment.ARROW_DAMAGE));
				SoundLib.ENTITY_GENERIC_EXPLODE.playSound(getPlayer().getLocation(), 7, 1.75f);
				final int reloadCount = WRECK.isEnabled(GameManager.getGame()) ? (int) (((100 - Settings.getCooldownDecrease().getPercentage()) / 100.0) * 25.0) : 25;
				this.reload = new Timer(reloadCount) {
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
				}.setPeriod(TimeUnit.TICKS, 2);
				reload.start();
			} else {
				getPlayer().sendMessage("§b재장전 §f중입니다.");
			}
		}
	}

	public interface OnHitBehavior {
		OnHitBehavior CUT = new OnHitBehavior() {
			@Override
			public void onHit(Damageable damager, Damageable victim) {
				ParticleLib.SWEEP_ATTACK.spawnParticle(victim.getLocation(), 1, 1, 1, 3);
				if (victim instanceof LivingEntity) {
					((LivingEntity) victim).setNoDamageTicks(0);
				}
				victim.damage(5, damager);
			}
		};
		OnHitBehavior GRAVITY = new OnHitBehavior() {
			@Override
			public void onHit(Damageable damager, Damageable victim) {
				for (Location location : sphere.toLocations(victim.getLocation())) {
					ParticleLib.REDSTONE.spawnParticle(location, PURPLE);
				}
				for (LivingEntity entity : LocationUtil.getNearbyEntities(LivingEntity.class, victim.getLocation(), 4, 4, Predicates.STRICT(damager))) {
					entity.setVelocity(victim.getLocation().toVector().subtract(entity.getLocation().toVector()).multiply(0.75));
				}
			}
		};
		OnHitBehavior WIND = new OnHitBehavior() {
			@Override
			public void onHit(Damageable damager, Damageable victim) {
				Vector vector = damager.getLocation().toVector().subtract(victim.getLocation().toVector()).multiply(-1);
				if (vector.length() > 0.01) {
					vector.normalize().multiply(2);
				}
				victim.setVelocity(vector.setY(0));
			}
		};

		void onHit(Damageable damager, Damageable victim);
	}

	private abstract static class ArrowType {

		private final String name;

		private ArrowType(String name) {
			this.name = name;
		}

		protected abstract void launchArrow(Arrow arrow, int powerEnchant);

	}

	public class Bullet<Shooter extends Entity & Damageable & ProjectileSource> extends Timer {

		private final Shooter shooter;
		private final CustomEntity entity;
		private final Vector forward;
		private final Iterator<Vector> circle;
		private final OnHitBehavior onHitBehavior;
		private final int powerEnchant;

		private final RGB color, abilityColor;
		private final Set<Damageable> attacked = new HashSet<>();
		private Location lastLocation;

		private Bullet(Shooter shooter, Location startLocation, Vector arrowVelocity, int powerEnchant, OnHitBehavior onHitBehavior, RGB color, RGB abilityColor) {
			super(80);
			setPeriod(TimeUnit.TICKS, 1);
			this.shooter = shooter;
			this.entity = new ArrowEntity(startLocation.getWorld(), startLocation.getX(), startLocation.getY(), startLocation.getZ()).setBoundingBox(-.75, -.75, -.75, .75, .75, .75);
			this.forward = arrowVelocity.multiply(10);
			this.circle = Iterables.cycle(CIRCLE.clone().rotateAroundAxisY(-getPlayer().getLocation().getYaw()).rotateAroundAxis(VectorUtil.rotateAroundAxisY(getPlayer().getLocation().getDirection().setY(0).normalize(), 90), getPlayer().getLocation().getPitch() + 90)).iterator();
			this.onHitBehavior = onHitBehavior;
			this.powerEnchant = powerEnchant;
			this.color = color;
			this.abilityColor = abilityColor;
			this.lastLocation = startLocation;
		}

		@Override
		protected void run(int i) {
			Location newLocation = lastLocation.clone().add(forward);
			for (Iterator<Location> iterator = Line.iteratorBetween(lastLocation, newLocation, 40); iterator.hasNext(); ) {
				Location location = iterator.next();
				entity.setLocation(location);
				Block block = location.getBlock();
				Material type = block.getType();
				if (type.isSolid() && ItemLib.STAINED_GLASS.compareType(type) || Material.GLASS == type || ItemLib.STAINED_GLASS_PANE.compareType(type) || type == GLASS_PANE) {
					block.breakNaturally();
					SoundLib.BLOCK_GLASS_BREAK.playSound(block.getLocation(), 3, 1);
				}
				for (Damageable damageable : LocationUtil.getConflictingDamageables(entity.getBoundingBox())) {
					if (!shooter.equals(damageable) && damageable.isValid() && !damageable.isDead() && !attacked.contains(damageable)) {
						damageable.damage(EnchantLib.getDamageWithPowerEnchantment(Math.min((forward.getX() * forward.getX()) + (forward.getY() * forward.getY()) + (forward.getZ() * forward.getZ()) / 10.0, 10), powerEnchant), shooter);
						onHitBehavior.onHit(shooter, damageable);
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
				Player deflectedPlayer = deflector.getPlayer();
				new Bullet<>(deflectedPlayer, lastLocation, newDirection, powerEnchant, onHitBehavior, color, abilityColor).start();
			}

			@Override
			public ProjectileSource getShooter() {
				return shooter;
			}

		}

	}

}