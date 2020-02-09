package daybreak.abilitywar.ability.list;

import daybreak.abilitywar.ability.AbilityBase;
import daybreak.abilitywar.ability.AbilityManifest;
import daybreak.abilitywar.ability.AbilityManifest.Rank;
import daybreak.abilitywar.ability.AbilityManifest.Species;
import daybreak.abilitywar.ability.SubscribeEvent;
import daybreak.abilitywar.ability.decorator.ActiveHandler;
import daybreak.abilitywar.config.ability.AbilitySettings.SettingObject;
import daybreak.abilitywar.game.games.mode.AbstractGame.Participant;
import daybreak.abilitywar.utils.Messager;
import daybreak.abilitywar.utils.base.concurrent.TimeUnit;
import daybreak.abilitywar.utils.library.MaterialX;
import daybreak.abilitywar.utils.math.FastMath;
import daybreak.abilitywar.utils.math.LocationUtil;
import daybreak.abilitywar.utils.math.geometry.Line;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.player.PlayerArmorStandManipulateEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.EulerAngle;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

@AbilityManifest(Name = "황제", Rank = Rank.A, Species = Species.HUMAN)
public class TheEmperor extends AbilityBase implements ActiveHandler {

	public static final SettingObject<Integer> CooldownConfig = new SettingObject<Integer>(TheEmperor.class, "Cooldown", 50,
			"# 쿨타임") {

		@Override
		public boolean Condition(Integer value) {
			return value >= 0;
		}

	};

	public TheEmperor(Participant participant) {
		super(participant,
				ChatColor.translateAlternateColorCodes('&', "&f철괴를 우클릭하면 앞으로 돌진하는 방패 부대를 내보내"),
				ChatColor.translateAlternateColorCodes('&', "&f앞에 있는 모든 생명체와 물체를 밀쳐냅니다. " + Messager.formatCooldown(CooldownConfig.getValue())));
	}

	private final Set<ArmorStand> armorStands = new HashSet<>();
	private Vector direction;


	private static final double radians = Math.toRadians(90);
	private final CooldownTimer cooldownTimer = new CooldownTimer(CooldownConfig.getValue());
	private final DurationTimer skill = new DurationTimer(140, cooldownTimer) {

		private ArmorStand center;
		private HashMap<ArmorStand, Vector> diff;

		@Override
		protected void onDurationStart() {
			Location playerLocation = getPlayer().getLocation();
			direction = playerLocation.getDirection();
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
			center.setInvulnerable(true);
			center.setCollidable(false);
			center.setVisible(false);

			EulerAngle eulerAngle = new EulerAngle(Math.toRadians(270), Math.toRadians(270), 0);
			diff = new HashMap<>();
			for (ArmorStand armorStand : armorStands) {
				armorStand.setInvulnerable(true);
				armorStand.setCollidable(false);
				armorStand.setBasePlate(false);
				armorStand.setArms(true);
				armorStand.setVisible(false);
				armorStand.setRightArmPose(eulerAngle);
				armorStand.setGravity(false);
				EntityEquipment equipment = armorStand.getEquipment();
				equipment.setItemInMainHand(new ItemStack(Material.SHIELD));
				equipment.setHelmet(MaterialX.GOLDEN_HELMET.parseItem());
				diff.put(armorStand, armorStand.getLocation().toVector().subtract(centerVector));
			}
			gravityFalse = false;
		}

		private boolean gravityFalse;

		@Override
		protected void onDurationProcess(int count) {
			if (count >= 120) {
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
					if (!armorStands.contains(entity) && !entity.equals(getPlayer()))
						entity.setVelocity(direction.clone().multiply(1.3).setY(0));
				}
			}
		}
		@Override
		protected void onDurationEnd() {
			for (ArmorStand armorStand : armorStands) {
				armorStand.remove();
			}
			center.remove();
			armorStands.clear();
		}

		@Override
		protected void onDurationSilentEnd() {
			for (ArmorStand armorStand : armorStands) {
				armorStand.remove();
			}
			center.remove();
			armorStands.clear();
		}
	}.setPeriod(TimeUnit.TICKS, 1);

	@Override
	public boolean ActiveSkill(Material materialType, ClickType clickType) {
		if (materialType.equals(Material.IRON_INGOT) && clickType.equals(ClickType.RIGHT_CLICK) && !skill.isDuration() && !cooldownTimer.isCooldown()) {
			skill.start();
			return true;
		}
		return false;
	}

	@SubscribeEvent
	private void onPlayerArmorStandManipulate(PlayerArmorStandManipulateEvent e) {
		if (armorStands.contains(e.getRightClicked())) e.setCancelled(true);
	}

	@Override
	public void TargetSkill(Material materialType, LivingEntity entity) {
	}

	private double rotateX(double x, double z, double radians) {
		return (x * FastMath.cos(radians)) + (z * FastMath.sin(radians));
	}

	private double rotateZ(double x, double z, double radians) {
		return (-x * FastMath.sin(radians)) + (z * FastMath.cos(radians));
	}

}
