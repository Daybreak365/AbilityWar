package daybreak.abilitywar.ability.list;

import daybreak.abilitywar.ability.AbilityBase;
import daybreak.abilitywar.ability.AbilityManifest;
import daybreak.abilitywar.ability.SubscribeEvent;
import daybreak.abilitywar.config.Configuration.Settings;
import daybreak.abilitywar.config.ability.AbilitySettings.SettingObject;
import daybreak.abilitywar.game.AbstractGame;
import daybreak.abilitywar.game.AbstractGame.CustomEntity;
import daybreak.abilitywar.game.AbstractGame.Participant;
import daybreak.abilitywar.game.AbstractGame.Participant.ActionbarNotification.ActionbarChannel;
import daybreak.abilitywar.game.GameManager;
import daybreak.abilitywar.game.manager.object.WRECK;
import daybreak.abilitywar.utils.base.ProgressBar;
import daybreak.abilitywar.utils.base.concurrent.TimeUnit;
import daybreak.abilitywar.utils.base.math.LocationUtil;
import daybreak.abilitywar.utils.base.math.LocationUtil.Predicates;
import daybreak.abilitywar.utils.base.math.geometry.Line;
import daybreak.abilitywar.utils.base.math.geometry.Sphere;
import daybreak.abilitywar.utils.base.minecraft.entity.decorator.Deflectable;
import daybreak.abilitywar.utils.library.ParticleLib;
import daybreak.abilitywar.utils.library.ParticleLib.RGB;
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
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Damageable;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.projectiles.ProjectileSource;
import org.bukkit.util.Vector;

@AbilityManifest(name = "관통화살", rank = AbilityManifest.Rank.S, species = AbilityManifest.Species.OTHERS, explain = {
		"활을 쏠 때 벽과 생명체를 통과하는 특수한 투사체를 쏩니다.",
		"투사체에는 특수한 능력이 있으며, 활을 §e$[BulletConfig]번 §f쏠 때마다 능력이 변경됩니다.",
		"능력을 변경할 때 3초의 재장전 시간이 소요됩니다.",
		"§c절단§f: 투사체를 맞은 대상에게 추가 대미지를 입힙니다.",
		"§5중력§f: 투사체를 맞은 대상 주위 4칸의 생명체를 대상에게 끌어갑니다.",
		"§e풍월§f: 투사체를 맞은 대상을 멀리 날려보냅니다."
})
public class PenetrationArrow extends AbilityBase {

	public static final SettingObject<Integer> BulletConfig = abilitySettings.new SettingObject<Integer>(PenetrationArrow.class, "ArrowCount", 3,
			"# 능력 당 화살 개수") {

		@Override
		public boolean condition(Integer value) {
			return value >= 1 && value <= 100 && value % 2 != 0;
		}

	};

	public PenetrationArrow(AbstractGame.Participant participant) {
		super(participant);
	}

	private static final RGB RED = new RGB(219, 64, 66);
	private static final RGB PURPLE = new RGB(138, 9, 173);
	private static final RGB YELLOW = new RGB(255, 246, 122);

	private final int bulletCount = BulletConfig.getValue();

	private static final Sphere sphere = Sphere.of(4, 10);
	private final Random random = new Random();
	private final List<ArrowType> arrowTypes = Arrays.asList(
			new ArrowType(ChatColor.RED + "절단") {
				@Override
				protected void launchArrow(Arrow arrow, int powerLevel) {
					SoundLib.ENTITY_ARROW_SHOOT.playSound(getPlayer());
					new Parabola<>(getPlayer(), OnHitBehavior.CUT, arrow.getLocation(), arrow.getVelocity(), getPlayer().getLocation().getPitch(), powerLevel, RED).start();
				}
			},
			new ArrowType(ChatColor.DARK_PURPLE + "중력") {
				@Override
				protected void launchArrow(Arrow arrow, int powerLevel) {
					SoundLib.ENTITY_ARROW_SHOOT.playSound(getPlayer());
					SoundLib.PIANO.playInstrument(getPlayer(), Note.flat(0, Note.Tone.D));
					new Parabola<>(getPlayer(), OnHitBehavior.GRAVITY, arrow.getLocation(), arrow.getVelocity(), getPlayer().getLocation().getPitch(), powerLevel, PURPLE).start();
				}
			},
			new ArrowType(ChatColor.YELLOW + "풍월") {
				@Override
				protected void launchArrow(Arrow arrow, int powerLevel) {
					SoundLib.ENTITY_ARROW_SHOOT.playSound(getPlayer());
					SoundLib.PIANO.playInstrument(getPlayer(), Note.flat(1, Note.Tone.B));
					new Parabola<>(getPlayer(), OnHitBehavior.WIND, arrow.getLocation(), arrow.getVelocity(), getPlayer().getLocation().getPitch(), powerLevel, YELLOW).start();
				}
			}
	);

	private ArrowType arrowType = arrowTypes.get(0);
	private int arrowBullet = bulletCount;
	private Timer reload = null;

	private final ActionbarChannel actionbarChannel = newActionbarChannel();

	@SubscribeEvent
	private void onProjectileLaunch(EntityShootBowEvent e) {
		if (getPlayer().equals(e.getEntity()) && e.getProjectile() instanceof Arrow) {
			e.setCancelled(true);
			if (reload == null) {
				if (!getPlayer().getGameMode().equals(GameMode.CREATIVE) && (!e.getBow().hasItemMeta() || !e.getBow().getItemMeta().hasEnchant(Enchantment.ARROW_INFINITE))) {
					ItemLib.removeItem(getPlayer().getInventory(), Material.ARROW, 1);
				}
				arrowType.launchArrow((Arrow) e.getProjectile(), e.getBow().getEnchantmentLevel(Enchantment.ARROW_DAMAGE));
				arrowBullet--;
				actionbarChannel.update("§f능력: " + arrowType.name + "   §f화살: §e" + arrowBullet + "§f개");
				if (arrowBullet <= 0) {
					final int reloadCount = WRECK.isEnabled(GameManager.getGame()) ? (int) (((100 - Settings.getCooldownDecrease().getPercentage()) / 100.0) * 15.0) : 15;
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
							arrowBullet = bulletCount;
							PenetrationArrow.this.reload = null;
							actionbarChannel.update("§f능력: " + arrowType.name + "   §f화살: §e" + arrowBullet + "§f개");
						}
					}.setPeriod(TimeUnit.TICKS, 4);
					reload.start();
				}
			} else {
				getPlayer().sendMessage("§b재장전 §f중입니다.");
			}
		}
	}

	@Override
	protected void onUpdate(Update update) {
		if (update == Update.RESTRICTION_CLEAR) {
			actionbarChannel.update("§f능력: " + arrowType.name + "   §f화살: §e" + arrowBullet + "§f개");
		}
	}

	private abstract static class ArrowType {

		private final String name;

		private ArrowType(String name) {
			this.name = name;
		}

		protected abstract void launchArrow(Arrow arrow, int powerEnchant);

	}

	private static final double GRAVITATIONAL_CONSTANT = 3;

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

	public class Parabola<Shooter extends Entity & Damageable & ProjectileSource> extends Timer {

		private final Shooter shooter;
		private final OnHitBehavior onHitBehavior;
		private final CustomEntity entity;
		private final double velocity;
		private final int powerEnchant;
		private final Vector forward;

		private final ParticleLib.RGB color;

		private Parabola(Shooter shooter, OnHitBehavior onHitBehavior, Location startLocation, Vector arrowVelocity, double angle, int powerEnchant, ParticleLib.RGB color) {
			super(300);
			setPeriod(TimeUnit.TICKS, 1);
			this.shooter = shooter;
			this.onHitBehavior = onHitBehavior;
			this.entity = new ArrowEntity(startLocation.getWorld(), startLocation.getX(), startLocation.getY(), startLocation.getZ()).setBoundingBox(-0.5, -0.5, -0.5, 0.5, 0.5, 0.5);
			this.velocity = Math.sqrt((arrowVelocity.getX() * arrowVelocity.getX()) + (arrowVelocity.getY() * arrowVelocity.getY()) + (arrowVelocity.getZ() * arrowVelocity.getZ()));
			this.powerEnchant = powerEnchant;
			this.forward = arrowVelocity.setY(arrowVelocity.getY() * 0.7);
			this.color = color;
			this.lastLocation = startLocation;
			this.time = Math.max(0, (angle + 135) / 360);
		}

		private Location lastLocation;
		private double time;
		private final Set<Damageable> attacked = new HashSet<>();

		@Override
		protected void run(int i) {
			time += 0.025;
			double height = -0.5 * GRAVITATIONAL_CONSTANT * (time * time);
			Location newLocation = lastLocation.clone().add(forward).add(0, height, 0);
			for (Iterator<Location> iterator = Line.iteratorBetween(lastLocation, newLocation, 8); iterator.hasNext(); ) {
				Location location = iterator.next();
				if (location.getY() < 0) {
					stop(false);
					return;
				}
				entity.setLocation(location);
				for (Damageable damageable : LocationUtil.getConflictingDamageables(entity.getBoundingBox())) {
					if (damageable.isValid() && !damageable.isDead() && !shooter.equals(damageable) && !attacked.contains(damageable)) {
						damageable.damage(EnchantLib.getDamageWithPowerEnchantment(Math.round(2.5 * velocity * 10) / 10.0, powerEnchant), getPlayer());
						onHitBehavior.onHit(shooter, damageable);
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
				new Parabola<>(deflectedPlayer, onHitBehavior, lastLocation, newDirection, getPlayer().getLocation().getDirection().getY() * 90, powerEnchant, color).start();
			}

			@Override
			public ProjectileSource getShooter() {
				return shooter;
			}

		}

	}

}
