package daybreak.abilitywar.game.list.mixability.synergy.list;

import daybreak.abilitywar.AbilityWar;
import daybreak.abilitywar.ability.AbilityManifest;
import daybreak.abilitywar.ability.AbilityManifest.Rank;
import daybreak.abilitywar.ability.AbilityManifest.Species;
import daybreak.abilitywar.ability.SubscribeEvent;
import daybreak.abilitywar.ability.decorator.ActiveHandler;
import daybreak.abilitywar.config.ability.AbilitySettings.SettingObject;
import daybreak.abilitywar.game.AbstractGame.Participant;
import daybreak.abilitywar.game.list.mixability.synergy.Synergy;
import daybreak.abilitywar.utils.base.Formatter;
import daybreak.abilitywar.utils.base.concurrent.TimeUnit;
import daybreak.abilitywar.utils.base.math.FastMath;
import daybreak.abilitywar.utils.base.math.LocationUtil;
import daybreak.abilitywar.utils.base.math.VectorUtil;
import daybreak.abilitywar.utils.base.math.geometry.Line;
import daybreak.abilitywar.utils.base.minecraft.version.ServerVersion;
import daybreak.abilitywar.utils.library.MaterialX;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.event.entity.EntityDamageByBlockEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerArmorStandManipulateEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.util.EulerAngle;
import org.bukkit.util.Vector;

@AbilityManifest(name = "대황제", rank = Rank.A, species = Species.HUMAN, explain = {
		"철괴를 우클릭하면 모든 방향으로 돌진하는 방패 부대를 내보내",
		"주변에 있는 모든 생명체와 물체를 밀쳐냅니다. $[CooldownConfig]"
})
public class GrandEmperor extends Synergy implements ActiveHandler {

	public static final SettingObject<Integer> CooldownConfig = synergySettings.new SettingObject<Integer>(GrandEmperor.class, "Cooldown", 50,
			"# 쿨타임") {

		@Override
		public boolean condition(Integer value) {
			return value >= 0;
		}

		@Override
		public String toString() {
			return Formatter.formatCooldown(getValue());
		}

	};
	private static final double radians = Math.toRadians(90);
	private final CooldownTimer cooldownTimer = new CooldownTimer(CooldownConfig.getValue());

	public GrandEmperor(Participant participant) {
		super(participant);
	}

	@Override
	public boolean ActiveSkill(Material materialType, ClickType clickType) {
		if (materialType.equals(Material.IRON_INGOT) && clickType.equals(ClickType.RIGHT_CLICK) && !cooldownTimer.isCooldown()) {
			Vector direction = getPlayer().getLocation().getDirection();
			for (int i = 0; i < 4; i++) {
				Vector dir = direction.clone();
				if (i != 0) {
					VectorUtil.rotateAroundAxisY(dir, i * 90);
				}
				new Shield(dir).start();
			}
			cooldownTimer.start();
			return true;
		}
		return false;
	}

	@SubscribeEvent
	private void onPlayerArmorStandManipulate(PlayerArmorStandManipulateEvent e) {
		if (e.getRightClicked().hasMetadata("Emperor")) e.setCancelled(true);
	}

	@SubscribeEvent
	private void onEntityDamage(EntityDamageEvent e) {
		if (e.getEntity().hasMetadata("Emperor")) {
			e.setCancelled(true);
		}
	}

	@SubscribeEvent
	private void onEntityDamageByEntity(EntityDamageByEntityEvent e) {
		onEntityDamage(e);
	}

	@SubscribeEvent
	private void onEntityDamageByBlock(EntityDamageByBlockEvent e) {
		onEntityDamage(e);
	}

	private double rotateX(double x, double z, double radians) {
		return (x * FastMath.cos(radians)) + (z * FastMath.sin(radians));
	}

	private double rotateZ(double x, double z, double radians) {
		return (-x * FastMath.sin(radians)) + (z * FastMath.cos(radians));
	}

	private class Shield extends Timer {

		private final Set<ArmorStand> armorStands = new HashSet<>();
		private final Vector direction;
		private ArmorStand center;
		private HashMap<ArmorStand, Vector> diff;
		private Vector push;
		private boolean gravityFalse;

		public Shield(Vector direction) {
			super(TaskType.NORMAL, 140);
			setPeriod(TimeUnit.TICKS, 1);
			this.direction = direction.setY(0);
		}

		@Override
		protected void onStart() {
			Location playerLocation = getPlayer().getLocation().clone().add(direction.clone().normalize().multiply(2));
			Location lineTarget = playerLocation.clone().add(direction.clone().setY(0).normalize().multiply(3));
			for (Vector vector : Line.between(playerLocation, lineTarget, 4)) {
				final double originX = vector.getX();
				final double originZ = vector.getZ();
				armorStands.add(getPlayer().getWorld().spawn(playerLocation.clone().add(vector.clone()
						.setX(rotateX(originX, originZ, radians))
						.setZ(rotateZ(originX, originZ, radians))), ArmorStand.class)
				);
				armorStands.add(getPlayer().getWorld().spawn(playerLocation.clone().add(vector.clone()
						.setX(rotateX(originX, originZ, radians * 3))
						.setZ(rotateZ(originX, originZ, radians * 3))), ArmorStand.class)
				);
			}
			Vector centerVector = lineTarget.toVector();
			this.center = getPlayer().getWorld().spawn(lineTarget, ArmorStand.class);
			center.setMetadata("Emperor", new FixedMetadataValue(AbilityWar.getPlugin(), null));
			if (ServerVersion.getVersionNumber() >= 10) {
				center.setInvulnerable(true);
				center.setCollidable(false);
			}
			center.setVisible(false);

			EulerAngle eulerAngle = new EulerAngle(Math.toRadians(270), Math.toRadians(270), 0);
			diff = new HashMap<>();
			for (ArmorStand armorStand : armorStands) {
				armorStand.setMetadata("Emperor", new FixedMetadataValue(AbilityWar.getPlugin(), null));
				if (ServerVersion.getVersionNumber() >= 10) {
					armorStand.setInvulnerable(true);
					armorStand.setCollidable(false);
				}
				armorStand.setBasePlate(false);
				armorStand.setArms(true);
				armorStand.setVisible(false);
				armorStand.setRightArmPose(eulerAngle);
				armorStand.setGravity(false);
				EntityEquipment equipment = armorStand.getEquipment();
				equipment.setItemInMainHand(new ItemStack(Material.SHIELD));
				equipment.setHelmet(MaterialX.GOLDEN_HELMET.parseItem());
				diff.put(armorStand, armorStand.getLocation().toVector().subtract(centerVector).add(direction.clone()));
			}
			gravityFalse = false;
			push = direction.clone().multiply(2).setY(0);
		}

		@Override
		protected void run(int count) {
			if (count <= 20) {
				center.setVelocity(direction);
				Location centerLocation = center.getLocation();
				for (ArmorStand armorStand : armorStands) {
					if (!armorStand.equals(center) && diff.containsKey(armorStand)) {
						armorStand.teleport(centerLocation.clone().add(diff.get(armorStand)));
					}
				}
			} else if (!gravityFalse) {
				center.setGravity(false);
				gravityFalse = true;
			}
			for (ArmorStand armorStand : armorStands) {
				for (Entity entity : LocationUtil.getNearbyEntities(Entity.class, armorStand.getLocation(), 1.8, 1.8)) {
					if (!armorStands.contains(entity) && !entity.equals(getPlayer())) {
						entity.setVelocity(push);
					}
				}
			}
		}

		@Override
		protected void onEnd() {
			for (ArmorStand armorStand : armorStands) {
				armorStand.remove();
			}
			center.remove();
			armorStands.clear();
		}

		@Override
		protected void onSilentEnd() {
			for (ArmorStand armorStand : armorStands) {
				armorStand.remove();
			}
			center.remove();
			armorStands.clear();
		}
	}

}
