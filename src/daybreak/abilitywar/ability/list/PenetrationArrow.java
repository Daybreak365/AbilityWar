package daybreak.abilitywar.ability.list;

import daybreak.abilitywar.ability.AbilityBase;
import daybreak.abilitywar.ability.AbilityManifest;
import daybreak.abilitywar.ability.Scheduled;
import daybreak.abilitywar.ability.SubscribeEvent;
import daybreak.abilitywar.config.ability.AbilitySettings;
import daybreak.abilitywar.game.games.mode.AbstractGame;
import daybreak.abilitywar.utils.base.ProgressBar;
import daybreak.abilitywar.utils.base.concurrent.TimeUnit;
import daybreak.abilitywar.utils.base.minecraft.version.NMSUtil;
import daybreak.abilitywar.utils.base.minecraft.version.NMSUtil.PlayerUtil;
import daybreak.abilitywar.utils.library.ParticleLib;
import daybreak.abilitywar.utils.library.SoundLib;
import daybreak.abilitywar.utils.library.item.ItemLib;
import daybreak.abilitywar.utils.math.FastMath;
import daybreak.abilitywar.utils.math.LocationUtil;
import daybreak.abilitywar.utils.math.VectorUtil.Vectors;
import daybreak.abilitywar.utils.math.geometry.Boundary.CenteredBoundingBox;
import daybreak.abilitywar.utils.math.geometry.Line;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Note;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Damageable;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.util.Vector;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Set;

@AbilityManifest(Name = "관통화살", Rank = AbilityManifest.Rank.S, Species = AbilityManifest.Species.OTHERS)
public class PenetrationArrow extends AbilityBase {

	public static final AbilitySettings.SettingObject<Integer> BulletConfig = new AbilitySettings.SettingObject<Integer>(PenetrationArrow.class, "ArrowCount", 5,
			"# 능력 당 화살 개수") {

		@Override
		public boolean Condition(Integer value) {
			return value >= 1 && value <= 100 && value % 2 != 0;
		}

	};

	public PenetrationArrow(AbstractGame.Participant participant) throws IllegalStateException {
		super(participant,
				ChatColor.translateAlternateColorCodes('&', "&f활을 쏠 때 벽과 생명체를 통과하는 특수한 투사체를 쏩니다."),
				ChatColor.translateAlternateColorCodes('&', "&f투사체에는 특수한 능력이 있으며, 활을 &e" + BulletConfig.getValue() + "번 쏠 때마다 능력이 변경됩니다."),
				ChatColor.translateAlternateColorCodes('&', "&f능력을 변경할 때 3초의 재장전 시간이 소요됩니다."),
				ChatColor.translateAlternateColorCodes('&', "&c절단&f: 투사체를 맞은 대상에게 추가 대미지를 입힙니다."),
				ChatColor.translateAlternateColorCodes('&', "&5중력&f: 투사체를 맞은 대상 주위 4칸의 생명체를 대상에게 끌어갑니다."),
				ChatColor.translateAlternateColorCodes('&', "&e풍월&f: 투사체를 맞은 대상을 나에게서 멀리 날려보냅니다."));
	}

	@Override
	public boolean ActiveSkill(Material material, ClickType clickType) {
		return false;
	}

	private static final ParticleLib.RGB RED = new ParticleLib.RGB(219, 64, 66);
	private static final ParticleLib.RGB PURPLE = new ParticleLib.RGB(138, 9, 173);
	private static final ParticleLib.RGB YELLOW = new ParticleLib.RGB(255, 246, 122);

	private final int bulletCount = BulletConfig.getValue();

	private final Vectors sphere = LocationUtil.getSphere(4, 10);
	private final Random random = new Random();
	private final List<ArrowType> arrowTypes = Arrays.asList(
			new ArrowType(ChatColor.translateAlternateColorCodes('&', "&c절단")) {
				@Override
				protected void launchArrow(Arrow arrow) {
					SoundLib.ENTITY_ARROW_SHOOT.playSound(getPlayer());
					new Parabola(arrow.getLocation(), arrow.getVelocity(), getPlayer().getLocation().getDirection().getY() * 90, RED) {
						@Override
						public void onHit(Damageable damager, Damageable victim) {
							ParticleLib.SWEEP_ATTACK.spawnParticle(victim.getLocation(), 1, 1, 1, 3);
							if (victim instanceof LivingEntity) {
								((LivingEntity) victim).setNoDamageTicks(0);
							}
							victim.damage(5, damager);
						}
					}.start();
				}
			},
			new ArrowType(ChatColor.translateAlternateColorCodes('&', "&5중력")) {
				@Override
				protected void launchArrow(Arrow arrow) {
					SoundLib.ENTITY_ARROW_SHOOT.playSound(getPlayer());
					SoundLib.PIANO.playInstrument(getPlayer(), Note.flat(0, Note.Tone.D));
					new Parabola(arrow.getLocation(), arrow.getVelocity(), getPlayer().getLocation().getDirection().getY() * 90, PURPLE) {
						@Override
						public void onHit(Damageable damager, Damageable victim) {
							for (Location location : sphere.toLocations(victim.getLocation())) {
								ParticleLib.REDSTONE.spawnParticle(location, PURPLE);
							}
							for (Damageable damageable : LocationUtil.getNearbyDamageableEntities(victim.getLocation(), 4, 4)) {
								damageable.setVelocity(victim.getLocation().toVector().subtract(damageable.getLocation().toVector()).multiply(0.75));
							}
						}
					}.start();
				}
			},
			new ArrowType(ChatColor.translateAlternateColorCodes('&', "&e풍월")) {
				@Override
				protected void launchArrow(Arrow arrow) {
					SoundLib.ENTITY_ARROW_SHOOT.playSound(getPlayer());
					SoundLib.XYLOPHONE.playInstrument(getPlayer(), Note.flat(1, Note.Tone.B));
					new Parabola(arrow.getLocation(), arrow.getVelocity(), getPlayer().getLocation().getDirection().getY() * 90, YELLOW) {
						@Override
						public void onHit(Damageable damager, Damageable victim) {
							victim.setVelocity(damager.getLocation().toVector().subtract(victim.getLocation().toVector()).multiply(-0.35).setY(0));
						}
					}.start();
				}
			}
	);

	private ArrowType arrowType = arrowTypes.get(0);
	private int arrowBullet = bulletCount;
	private Timer reload = null;

	@Scheduled
	private final Timer notice = new Timer() {
		@Override
		protected void run(int count) {
			if (reload == null) {
				NMSUtil.PlayerUtil.sendActionbar(getPlayer(), ChatColor.translateAlternateColorCodes('&', "&f능력: " + arrowType.name + "   &f화살: &e" + arrowBullet + "&f개"), 0, 4, 0);
			}
		}
	}.setPeriod(TimeUnit.TICKS, 2);

	@SubscribeEvent
	private void onProjectileLaunch(EntityShootBowEvent e) {
		if (getPlayer().equals(e.getEntity()) && e.getProjectile() instanceof Arrow) {
			e.setCancelled(true);
			if (reload == null) {
				if (!getPlayer().getGameMode().equals(GameMode.CREATIVE) && (!e.getBow().hasItemMeta() || !e.getBow().getItemMeta().hasEnchant(Enchantment.ARROW_INFINITE))) {
					ItemLib.removeItem(getPlayer().getInventory(), Material.ARROW, 1);
				}
				arrowType.launchArrow((Arrow) e.getProjectile());
				arrowBullet--;
				if (arrowBullet <= 0) {
					this.reload = new Timer(15) {
						private final ProgressBar progressBar = new ProgressBar(15, 15);

						@Override
						protected void run(int count) {
							progressBar.step();
							PlayerUtil.sendActionbar(getPlayer(), "재장전: " + progressBar.toString(), 0, 6, 0);
						}

						@Override
						protected void onEnd() {
							arrowType = arrowTypes.get(random.nextInt(arrowTypes.size()));
							arrowBullet = bulletCount;
							PenetrationArrow.this.reload = null;
						}
					}.setPeriod(TimeUnit.TICKS, 4);
					reload.start();
				}
			} else {
				getPlayer().sendMessage(ChatColor.translateAlternateColorCodes('&', "&b재장전 &f중입니다."));
			}
		}
	}

	@Override
	public void TargetSkill(Material material, LivingEntity livingEntity) {
	}

	private abstract static class ArrowType {

		private final String name;

		private ArrowType(String name) {
			this.name = name;
		}

		protected abstract void launchArrow(Arrow arrow);

	}

	private static final double GRAVITATIONAL_CONSTANT = 3;

	public abstract class Parabola extends Timer {

		private final CenteredBoundingBox centeredBoundingBox;
		private final double velocity;
		private final double sin;
		private final Vector forward;

		private final ParticleLib.RGB color;

		private Parabola(Location startLocation, Vector arrowVelocity, double angle, ParticleLib.RGB color) {
			super(300);
			setPeriod(TimeUnit.TICKS, 1);
			this.centeredBoundingBox = new CenteredBoundingBox(startLocation, -0.5, -0.5, -0.5, 0.5, 0.5, 0.5);
			this.velocity = Math.sqrt((arrowVelocity.getX() * arrowVelocity.getX()) + (arrowVelocity.getY() * arrowVelocity.getY()) + (arrowVelocity.getZ() * arrowVelocity.getZ()));
			this.sin = FastMath.sin(Math.toRadians(angle));
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
			time += 0.03;
			double height = -0.5 * GRAVITATIONAL_CONSTANT * (time * time) + (velocity * sin * time) * 0.7;
			Location newLocation = lastLocation.clone().add(forward).add(0, height, 0);
			for (Iterator<Location> iterator = Line.iteratorBetween(lastLocation, newLocation, 8); iterator.hasNext(); ) {
				Location location = iterator.next();
				centeredBoundingBox.setLocation(location);
				for (Damageable damageable : LocationUtil.getConflictingDamageables(centeredBoundingBox)) {
					if (!getPlayer().equals(damageable) && !attacked.contains(damageable)) {
						damageable.damage(Math.round(2.5 * velocity * 10) / 10.0, getPlayer());
						onHit(getPlayer(), damageable);
						attacked.add(damageable);
					}
				}
				ParticleLib.REDSTONE.spawnParticle(location, color);
			}
			lastLocation = newLocation;
		}

		public abstract void onHit(Damageable damager, Damageable victim);

	}

}
