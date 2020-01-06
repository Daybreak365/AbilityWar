package daybreak.abilitywar.ability.list;

import daybreak.abilitywar.ability.AbilityBase;
import daybreak.abilitywar.ability.AbilityManifest;
import daybreak.abilitywar.ability.AbilityManifest.Rank;
import daybreak.abilitywar.ability.AbilityManifest.Species;
import daybreak.abilitywar.ability.SubscribeEvent;
import daybreak.abilitywar.config.AbilitySettings;
import daybreak.abilitywar.game.games.mode.AbstractGame.Participant;
import daybreak.abilitywar.utils.Messager;
import daybreak.abilitywar.utils.library.item.MaterialLib;
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
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.EulerAngle;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.HashMap;

@AbilityManifest(Name = "황제", Rank = Rank.A, Species = Species.HUMAN)
public class TheEmperor extends AbilityBase {

	public static final AbilitySettings.SettingObject<Integer> CooldownConfig = new AbilitySettings.SettingObject<Integer>(TheEmperor.class, "Cooldown", 50,
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

	private final ArrayList<ArmorStand> armorStands = new ArrayList<>();
	private Vector direction;


	private final CooldownTimer cooldownTimer = new CooldownTimer(CooldownConfig.getValue());
	private final DurationTimer skill = new DurationTimer(140, cooldownTimer) {

		private ArmorStand center;
		private HashMap<ArmorStand, Vector> diff;

		@Override
		protected void onDurationStart() {
			Location playerLocation = getPlayer().getLocation();
			direction = getPlayer().getLocation().getDirection();
			Line line = new Line(playerLocation, playerLocation.clone().add(direction.clone().setY(0).normalize().multiply(3))).setLocationAmount(4);
			final double radians = Math.toRadians(90);
			for (Vector vector : line.getVectors()) {
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
			EulerAngle eulerAngle = new EulerAngle(Math.toRadians(270), Math.toRadians(270), 0);
			center = null;
			Vector centerVector = null;
			diff = new HashMap<>();
			for (int i = 0; i < armorStands.size(); i++) {
				ArmorStand armorStand = armorStands.get(i);
				armorStand.setInvulnerable(true);
				armorStand.setCollidable(false);
				armorStand.setBasePlate(false);
				armorStand.setArms(true);
				armorStand.setVisible(false);
				armorStand.setRightArmPose(eulerAngle);
				switch (i) {
					case 0:
						center = armorStand;
						centerVector = center.getLocation().toVector();
						break;
					default:
						armorStand.setGravity(false);
						armorStand.setItemInHand(new ItemStack(Material.SHIELD));
						armorStand.setHelmet(MaterialLib.GOLDEN_HELMET.getItem());
						diff.put(armorStand, armorStand.getLocation().toVector().subtract(centerVector));
						break;
				}
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
			armorStands.clear();
		}

		@Override
		protected void onSilentEnd() {
			for (ArmorStand armorStand : armorStands) {
				armorStand.remove();
			}
			armorStands.clear();
		}
	}.setPeriod(1);

	@Override
	public boolean ActiveSkill(Material materialType, ClickType ct) {
		if (materialType.equals(Material.IRON_INGOT) && ct.equals(ClickType.RIGHT_CLICK) && !skill.isDuration() && !cooldownTimer.isCooldown()) {
			skill.startTimer();
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
