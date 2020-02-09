package daybreak.abilitywar.ability.list;

import daybreak.abilitywar.ability.AbilityBase;
import daybreak.abilitywar.ability.AbilityManifest;
import daybreak.abilitywar.ability.AbilityManifest.Rank;
import daybreak.abilitywar.ability.AbilityManifest.Species;
import daybreak.abilitywar.ability.Scheduled;
import daybreak.abilitywar.ability.SubscribeEvent;
import daybreak.abilitywar.config.ability.AbilitySettings.SettingObject;
import daybreak.abilitywar.game.games.mode.AbstractGame.Participant;
import daybreak.abilitywar.utils.base.concurrent.TimeUnit;
import daybreak.abilitywar.utils.base.minecraft.version.ServerVersion;
import daybreak.abilitywar.utils.library.ParticleLib;
import daybreak.abilitywar.utils.library.ParticleLib.RGB;
import daybreak.abilitywar.utils.library.PotionEffects;
import daybreak.abilitywar.utils.library.item.ItemLib;
import daybreak.abilitywar.utils.math.LocationUtil;
import daybreak.abilitywar.utils.math.geometry.Boundary.CenteredBoundingBox;
import daybreak.abilitywar.utils.math.geometry.Line;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Damageable;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.util.Vector;

import java.util.Iterator;

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
	private static final RGB BULLET_COLOR = new RGB(43, 209, 224);

	@Scheduled
	private final Timer snipeMode = new Timer() {
		@Override
		protected void run(int count) {
			Material main = getPlayer().getInventory().getItemInMainHand().getType();
			Material off = getPlayer().getInventory().getItemInOffHand().getType();
			if (main.equals(Material.BOW) || off.equals(Material.BOW) || (ServerVersion.getVersion() >= 14 && (main.equals(Material.CROSSBOW) || off.equals(Material.CROSSBOW)))) {
				PotionEffects.SLOW.addPotionEffect(getPlayer(), 2, 3, true);
				getPlayer().setVelocity(getPlayer().getVelocity().setX(0).setY(Math.min(0, getPlayer().getVelocity().getY())).setZ(0));
			}
		}
	}.setPeriod(TimeUnit.TICKS, 1);


	@Override
	public boolean ActiveSkill(Material materialType, ClickType clickType) {
		return false;
	}

	@SubscribeEvent
	public void onProjectileLaunch(EntityShootBowEvent e) {
		if (getPlayer().equals(e.getEntity()) && e.getProjectile() instanceof Arrow) {
			e.setCancelled(true);
			Arrow arrow = (Arrow) e.getProjectile();
			new Bullet(arrow.getLocation(), arrow.getVelocity(), BULLET_COLOR).start();
		}
	}

	@Override
	public void TargetSkill(Material materialType, LivingEntity entity) {
	}

	public class Bullet extends Timer {

		private final CenteredBoundingBox centeredBoundingBox;
		private final Vector forward;

		private final RGB color;

		private Bullet(Location startLocation, Vector arrowVelocity, RGB color) {
			super(160);
			setPeriod(TimeUnit.TICKS, 1);
			this.centeredBoundingBox = new CenteredBoundingBox(startLocation, -.75, -.75, -.75, .75, .75, .75);
			this.forward = arrowVelocity.multiply(2.5);
			this.color = color;
			this.lastLocation = startLocation;
		}

		private Location lastLocation;

		@Override
		protected void run(int i) {
			Location newLocation = lastLocation.clone().add(forward);
			for (Iterator<Location> iterator = Line.iteratorBetween(lastLocation, newLocation, 20); iterator.hasNext(); ) {
				Location location = iterator.next();
				centeredBoundingBox.setLocation(location);
				Block block = location.getBlock();
				Material type = block.getType();
				if (type.isSolid()) {
					if (ItemLib.STAINED_GLASS.compareType(type) || Material.GLASS == type || ItemLib.STAINED_GLASS_PANE.compareType(type) || type == GLASS_PANE) {
						block.breakNaturally();
					} else {
						stop(false);
						return;
					}
				}
				for (Damageable damageable : LocationUtil.getConflictingDamageables(centeredBoundingBox)) {
					if (!getPlayer().equals(damageable)) {
						double damage = Math.min((forward.getX() * forward.getX()) + (forward.getY() * forward.getY()) + (forward.getZ() * forward.getZ()) / 10.0, 10);
						damageable.damage(damage, getPlayer());
						stop(false);
						return;
					}
				}
				ParticleLib.REDSTONE.spawnParticle(location, color);
			}
			lastLocation = newLocation;
		}

	}

}