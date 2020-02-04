package daybreak.abilitywar.ability.list;

import daybreak.abilitywar.ability.AbilityBase;
import daybreak.abilitywar.ability.AbilityManifest;
import daybreak.abilitywar.ability.AbilityManifest.Rank;
import daybreak.abilitywar.ability.AbilityManifest.Species;
import daybreak.abilitywar.ability.Scheduled;
import daybreak.abilitywar.ability.SubscribeEvent;
import daybreak.abilitywar.game.games.mode.AbstractGame.Participant;
import daybreak.abilitywar.utils.base.concurrent.TimeUnit;
import daybreak.abilitywar.utils.library.ParticleLib;
import daybreak.abilitywar.utils.library.ParticleLib.RGB;
import daybreak.abilitywar.utils.library.PotionEffects;
import daybreak.abilitywar.utils.library.SoundLib;
import daybreak.abilitywar.utils.math.LocationUtil;
import daybreak.abilitywar.utils.math.LocationUtil.Predicates;
import daybreak.abilitywar.utils.math.NumberUtil;
import daybreak.abilitywar.utils.math.geometry.Circle;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.projectiles.ProjectileSource;

import java.util.function.Predicate;

@AbilityManifest(Name = "쇼맨쉽", Rank = Rank.B, Species = Species.HUMAN)
public class ShowmanShip extends AbilityBase {

	public ShowmanShip(Participant participant) {
		super(participant,
				ChatColor.translateAlternateColorCodes('&', "&f주변 7칸 이내에 있는 생명체 수에 따라 효과를 받습니다."),
				ChatColor.translateAlternateColorCodes('&', "&f플레이어는 1명, 나머지 생명체는 0.2명 취급합니다."),
				ChatColor.translateAlternateColorCodes('&', "&a1명 이하 &7: &f나약함  &a2명 이상 &7: &f힘 II"),
				ChatColor.translateAlternateColorCodes('&', "&a4명 이상 &7: &f힘 III"));
	}

	private final RGB WEAK = new RGB(214, 255, 212);
	private final RGB POWER = new RGB(255, 184, 150);
	private final RGB POWERFUL = new RGB(255, 59, 59);
	private final Circle circle = Circle.of(7, 100);

	@Scheduled
	private final Timer passive = new Timer() {

		@Override
		public void run(int count) {
			final double point = getPoint(7, 7);

			final RGB color;
			if (point <= 1) {
				PotionEffects.WEAKNESS.addPotionEffect(getPlayer(), 4, 0, true);
				color = WEAK;
			} else if (point >= 2 && point < 4) {
				PotionEffects.INCREASE_DAMAGE.addPotionEffect(getPlayer(), 4, 1, true);
				color = POWER;
			} else {
				PotionEffects.INCREASE_DAMAGE.addPotionEffect(getPlayer(), 4, 2, true);
				color = POWERFUL;
			}

			for (Location loc : circle.toLocations(getPlayer().getLocation()).floor(getPlayer().getLocation().getY())) {
				ParticleLib.REDSTONE.spawnParticle(getPlayer(), loc, color);
			}
		}

	}.setPeriod(TimeUnit.TICKS, 1);

	@Override
	public boolean ActiveSkill(Material materialType, ClickType clickType) {
		return false;
	}

	@Override
	public void TargetSkill(Material materialType, LivingEntity entity) {
	}

	@SubscribeEvent
	public void onEntityDamageByEntity(EntityDamageByEntityEvent e) {
		Entity damager = e.getDamager();
		if (damager instanceof Projectile) {
			ProjectileSource source = ((Projectile) damager).getShooter();
			if (getPlayer().equals(source)) {
				damager = getPlayer();
			}
		}
		if (damager.equals(getPlayer())) {
			double victimLocationY = e.getEntity().getLocation().getY();
			double damagerLocationY = getPlayer().getLocation().getY();
			if (victimLocationY < damagerLocationY) {
				e.setDamage(Math.floor((e.getDamage() + (e.getDamage() * (1 / (damagerLocationY - victimLocationY + 1) * 0.44))) * 10) / 10);
				SoundLib.ENTITY_EXPERIENCE_ORB_PICKUP.playSound(getPlayer());
				ParticleLib.LAVA.spawnParticle(e.getEntity().getLocation(), 1, 1, 1, 5);
			} else if (victimLocationY != damagerLocationY) {
				e.setCancelled(true);
				SoundLib.BLOCK_ANVIL_BREAK.playSound(getPlayer());
			}
		}
	}

	private final Predicate<Entity> predicate = Predicates.PARTICIPANTS_UNEQUAL(getPlayer());

	private double getPoint(double horizontal, double vertical) {
		Location center = getPlayer().getLocation();
		double centerX = center.getX(), centerZ = center.getZ();
		double point = 0;
		for (Entity entity : LocationUtil.collectEntities(center, horizontal)) {
			Location entityLocation = entity.getLocation();
			if (LocationUtil.distanceSquared2D(centerX, centerZ, entityLocation.getX(), entityLocation.getZ()) <= (horizontal * horizontal) && NumberUtil.subtract(center.getY(), entityLocation.getY()) <= vertical && (predicate == null || predicate.test(entity))) {
				if (entity instanceof Player) {
					point += 1.0;
				} else if (entity instanceof LivingEntity) {
					point += 0.2;
				}
			}
		}
		return point;
	}

}
