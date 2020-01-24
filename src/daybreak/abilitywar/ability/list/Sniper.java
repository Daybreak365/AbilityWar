package daybreak.abilitywar.ability.list;

import daybreak.abilitywar.ability.AbilityBase;
import daybreak.abilitywar.ability.AbilityManifest;
import daybreak.abilitywar.ability.AbilityManifest.Rank;
import daybreak.abilitywar.ability.AbilityManifest.Species;
import daybreak.abilitywar.ability.SubscribeEvent;
import daybreak.abilitywar.ability.event.AbilityRestrictionClearEvent;
import daybreak.abilitywar.config.AbilitySettings.SettingObject;
import daybreak.abilitywar.game.games.mode.AbstractGame.Participant;
import daybreak.abilitywar.utils.library.ParticleLib;
import daybreak.abilitywar.utils.library.PotionEffects;
import daybreak.abilitywar.utils.library.item.ItemLib;
import daybreak.abilitywar.utils.math.LocationUtil;
import daybreak.abilitywar.utils.math.geometry.Boundary.CenteredBoundingBox;
import daybreak.abilitywar.utils.math.geometry.Line;
import daybreak.abilitywar.utils.versioncompat.ServerVersion;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Damageable;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.util.Vector;

@AbilityManifest(Name = "스나이퍼", Rank = Rank.S, Species = Species.HUMAN)
public class Sniper extends AbilityBase {

	public static final SettingObject<Integer> DurationConfig = new SettingObject<Integer>(Sniper.class, "Duration", 2,
			"# 능력 지속시간") {

		@Override
		public boolean Condition(Integer value) {
			return value >= 1;
		}

	};

	public Sniper(Participant participant) {
		super(participant,
				ChatColor.translateAlternateColorCodes('&', "&f스나이퍼가 쏘는 화살은 " + DurationConfig.getValue() + "초간 빠른 속도로 곧게 뻗어나가다 떨어집니다."));
	}

	private static final Material GLASS_PANE = ServerVersion.getVersion() > 12 ? Material.valueOf("GLASS_PANE") : Material.valueOf("THIN_GLASS");
	private static final ParticleLib.RGB PURPLE = new ParticleLib.RGB(138, 9, 173);

	private final Timer snipe = ServerVersion.getVersion() < 14 ?
			new Timer() {
				@Override
				protected void onProcess(int count) {
					Material main = getPlayer().getInventory().getItemInMainHand().getType();
					Material off = getPlayer().getInventory().getItemInOffHand().getType();
					if (main.equals(Material.BOW) || off.equals(Material.BOW)) {
						PotionEffects.SLOW.addPotionEffect(getPlayer(), 5, 8, true);
						PotionEffects.JUMP.addPotionEffect(getPlayer(), 5, 200, true);
					}
				}
			}.setPeriod(3)
			:
			new Timer() {
				@Override
				protected void onProcess(int count) {
					Material main = getPlayer().getInventory().getItemInMainHand().getType();
					Material off = getPlayer().getInventory().getItemInOffHand().getType();
					if (main.equals(Material.BOW) || off.equals(Material.BOW) || main.equals(Material.CROSSBOW) || off.equals(Material.CROSSBOW)) {
						PotionEffects.SLOW.addPotionEffect(getPlayer(), 5, 8, true);
						PotionEffects.JUMP.addPotionEffect(getPlayer(), 5, 200, true);
					}
				}
			}.setPeriod(3);


	@Override
	public boolean ActiveSkill(Material materialType, ClickType ct) {
		return false;
	}

	@SubscribeEvent
	public void onProjectileLaunch(EntityShootBowEvent e) {
		if (getPlayer().equals(e.getEntity()) && e.getProjectile() instanceof Arrow) {
			e.setCancelled(true);
			Arrow arrow = (Arrow) e.getProjectile();
			new Bullet(arrow.getLocation(), arrow.getVelocity(), PURPLE) {
				@Override
				public void onHit(Damageable damager, Damageable victim) {
					Bukkit.broadcastMessage("명중");
				}
			}.startTimer();
		}
	}

	@Override
	public void TargetSkill(Material materialType, LivingEntity entity) {
	}

	@SubscribeEvent(onlyRelevant = true)
	public void onRestrictionClear(AbilityRestrictionClearEvent e) {
		snipe.startTimer();
	}

	public abstract class Bullet extends Timer {

		private final CenteredBoundingBox centeredBoundingBox;
		private final Vector forward;

		private final ParticleLib.RGB color;

		private Bullet(Location startLocation, Vector arrowVelocity, ParticleLib.RGB color) {
			super(160);
			setPeriod(1);
			this.centeredBoundingBox = new CenteredBoundingBox(startLocation, -.75, -.75, -.75, .75, .75, .75);
			this.forward = arrowVelocity.multiply(2.5);
			this.color = color;
			this.lastLocation = startLocation;
			this.line = new Line(startLocation, startLocation).setLocationAmount(30);
		}

		private Location lastLocation;
		private final Line line;

		@Override
		protected void onProcess(int i) {
			Location newLocation = lastLocation.clone().add(forward);
			for (Location location : line.setVector(lastLocation, newLocation).getLocations(lastLocation)) {
				centeredBoundingBox.setLocation(location);
				Block block = location.getBlock();
				Material type = block.getType();
				if (type.isSolid()) {
					if (ItemLib.STAINED_GLASS.compareType(type) || Material.GLASS == type || ItemLib.STAINED_GLASS_PANE.compareType(type) || type == GLASS_PANE) {
						block.breakNaturally();
					} else {
						stopTimer(false);
						return;
					}
				}
				for (Damageable damageable : LocationUtil.getConflictingDamageables(centeredBoundingBox)) {
					if (!getPlayer().equals(damageable)) {
						double damage = Math.min((forward.getX() * forward.getX()) + (forward.getY() * forward.getY()) + (forward.getZ() * forward.getZ()) / 10.0, 15);
						Bukkit.broadcastMessage(damage + " 대미지");
						damageable.damage(damage, getPlayer());
						onHit(getPlayer(), damageable);
						stopTimer(false);
						return;
					}
				}
				ParticleLib.REDSTONE.spawnParticle(location, color);
			}
			lastLocation = newLocation;
		}

		public abstract void onHit(Damageable damager, Damageable victim);

	}

}