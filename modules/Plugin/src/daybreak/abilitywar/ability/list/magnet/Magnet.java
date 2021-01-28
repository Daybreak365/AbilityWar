package daybreak.abilitywar.ability.list.magnet;

import daybreak.abilitywar.AbilityWar;
import daybreak.abilitywar.ability.AbilityBase;
import daybreak.abilitywar.ability.AbilityManifest;
import daybreak.abilitywar.ability.AbilityManifest.Rank;
import daybreak.abilitywar.ability.AbilityManifest.Species;
import daybreak.abilitywar.ability.decorator.ActiveHandler;
import daybreak.abilitywar.game.AbstractGame.CustomEntity;
import daybreak.abilitywar.game.AbstractGame.Participant;
import daybreak.abilitywar.game.event.customentity.CustomEntitySetLocationEvent;
import daybreak.abilitywar.utils.annotations.Beta;
import daybreak.abilitywar.utils.base.concurrent.TimeUnit;
import daybreak.abilitywar.utils.base.math.LocationUtil;
import daybreak.abilitywar.utils.base.math.VectorUtil;
import daybreak.abilitywar.utils.base.math.geometry.ImageVector;
import daybreak.abilitywar.utils.base.math.geometry.ImageVector.Point2D;
import daybreak.abilitywar.utils.base.minecraft.item.Skulls;
import daybreak.abilitywar.utils.base.minecraft.nms.NMS;
import daybreak.abilitywar.utils.library.ParticleLib;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import java.util.HashSet;
import java.util.Set;

@AbilityManifest(name = "마그넷", rank = Rank.S, species = Species.HUMAN, explain = {

})
@Beta
public class Magnet extends AbilityBase implements ActiveHandler {

	private static final Vector ZERO = new Vector();
	private static final ImageVector ARROW = ImageVector.parse(Magnet.class.getResourceAsStream("/daybreak/abilitywar/ability/list/magnet/arrow.png"))
			.addTransparent(0);
	private static final ItemStack HEAD = Skulls.createCustomSkull("30a35957fce2344aa8557ba6b36187ab415249934607bbbed2a053c3a86cafc0");

	public Magnet(Participant participant) {
		super(participant);
	}

	@Override
	public boolean ActiveSkill(Material material, ClickType clickType) {
		if (material == Material.IRON_INGOT && clickType == ClickType.RIGHT_CLICK) {
			new Gadget().start();
		}
		return false;
	}

	public class Gadget extends AbilityTimer implements Listener {

		private final Set<Entity> projectiles = new HashSet<>();
		private final Set<Entity> atMiddle = new HashSet<>();
		private final ArmorStand armorStand;

		private Gadget() {
			super(TaskType.NORMAL, 100);
			setPeriod(TimeUnit.TICKS, 1);
			this.armorStand = getPlayer().getWorld().spawn(getPlayer().getLocation(), ArmorStand.class);
			NMS.removeBoundingBox(armorStand);
			armorStand.setVisible(false);
			armorStand.setGravity(false);
			armorStand.getEquipment().setHelmet(HEAD);
		}

		@Override
		protected void onStart() {
			Bukkit.getPluginManager().registerEvents(this, AbilityWar.getPlugin());
		}

		public Location getCenter() {
			return armorStand.getLocation().clone().add(0, 1.75, 0);
		}

		@EventHandler
		private void onCustomEntitySetLocation(final CustomEntitySetLocationEvent e) {
			final Location center = getCenter();
			final CustomEntity customEntity = e.getCustomEntity();
			if (customEntity.isValid() && center.getWorld() == e.getTo().getWorld() && center.distanceSquared(e.getTo()) <= 64) {
				customEntity.getWorld().createExplosion(customEntity.getLocation(), 1f);
				customEntity.remove();
			}
		}

		@EventHandler
		private void onEntityShootBow(final EntityShootBowEvent e) {
			final Entity entity = e.getProjectile();
			final Location center = getCenter();
			if (center.getWorld() == entity.getWorld() && center.distanceSquared(entity.getLocation()) <= 64) {
				projectiles.add(entity);
			}
		}

		@Override
		protected void run(int count) {
			final Location center = getCenter();
			projectiles.addAll(LocationUtil.getNearbyEntities(Projectile.class, center, 8, 8, null));
			for (Entity projectile : projectiles) {
				if (atMiddle.contains(projectile)) continue;
				if (center.getWorld() == projectile.getWorld() && (center.distanceSquared(projectile.getLocation()) <= 1 || projectile.isOnGround())) {
					atMiddle.add(projectile);
					projectile.setVelocity(ZERO);
					projectile.teleport(center);
				} else {
					projectile.setGravity(false);
					projectile.setVelocity(center.toVector().subtract(projectile.getLocation().toVector()).normalize().multiply(0.5));
				}
			}
			if (count % 3 == 0) {
				final Location base = getCenter().add(0, 1, 0);
				for (Point2D point2D : ARROW) {
					ParticleLib.REDSTONE.spawnParticle(base.clone().add(VectorUtil.rotateAroundAxisY(point2D, -armorStand.getLocation().getYaw())), point2D.getColor());
				}
			}
		}

		@Override
		protected void onEnd() {
			onSilentEnd();
			for (Entity projectile : projectiles) {
				if (!projectile.isValid()) continue;
				projectile.remove();
				projectile.getWorld().createExplosion(projectile.getLocation(), 1f);
			}
		}

		@Override
		protected void onSilentEnd() {
			armorStand.remove();
			HandlerList.unregisterAll(this);
		}
	}

}
