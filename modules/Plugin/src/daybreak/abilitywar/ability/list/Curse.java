package daybreak.abilitywar.ability.list;

import daybreak.abilitywar.ability.AbilityBase;
import daybreak.abilitywar.ability.AbilityManifest;
import daybreak.abilitywar.ability.AbilityManifest.Rank;
import daybreak.abilitywar.ability.AbilityManifest.Species;
import daybreak.abilitywar.ability.SubscribeEvent;
import daybreak.abilitywar.ability.decorator.TargetHandler;
import daybreak.abilitywar.config.ability.AbilitySettings.SettingObject;
import daybreak.abilitywar.game.games.mode.AbstractGame.Participant;
import daybreak.abilitywar.utils.base.concurrent.TimeUnit;
import daybreak.abilitywar.utils.library.ParticleLib;
import daybreak.abilitywar.utils.library.ParticleLib.RGB;
import daybreak.abilitywar.utils.library.item.ItemLib;
import daybreak.abilitywar.utils.math.geometry.Circle;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByBlockEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerArmorStandManipulateEvent;
import org.bukkit.inventory.EntityEquipment;

@AbilityManifest(Name = "컬스", Rank = Rank.A, Species = Species.OTHERS)
public class Curse extends AbilityBase implements TargetHandler {

	public static final SettingObject<Integer> CooldownConfig = new SettingObject<Integer>(Curse.class, "Cooldown", 100,
			"# 쿨타임") {

		@Override
		public boolean Condition(Integer value) {
			return value >= 0;
		}

	};

	public static final SettingObject<Integer> DurationConfig = new SettingObject<Integer>(Curse.class, "Duration", 7,
			"# 지속시간") {

		@Override
		public boolean Condition(Integer value) {
			return value >= 0;
		}

	};

	public Curse(Participant participant) {
		super(participant,
				ChatColor.translateAlternateColorCodes('&', "&f상대를 우클릭하면 상대의 현재 위치에 7초간 지속되는"),
				ChatColor.translateAlternateColorCodes('&', "&f저주 인형을 만들어내며, 저주 인형이 대미지를 입을 경우"),
				ChatColor.translateAlternateColorCodes('&', "&f대미지의 1/5 만큼이 상대에게 전이됩니다."));
	}

	@Override
	public boolean ActiveSkill(Material materialType, ClickType clickType) {
		return false;
	}

	private static final Circle circle = Circle.of(0.3, 10);
	private static final RGB BLACK = RGB.of(1, 1, 1);

	private Player target = null;
	private ArmorStand armorStand = null;

	private final CooldownTimer cooldownTimer = new CooldownTimer(CooldownConfig.getValue());
	private final DurationTimer skill = new DurationTimer(DurationConfig.getValue() * 10, cooldownTimer) {
		@Override
		protected void onDurationStart() {
			armorStand = target.getWorld().spawn(target.getEyeLocation(), ArmorStand.class);
			armorStand.setInvulnerable(true);
			armorStand.setCustomName(target.getName());
			armorStand.setCustomNameVisible(true);
			armorStand.setBasePlate(false);
			armorStand.setArms(true);
			armorStand.setSmall(true);
			armorStand.setGravity(false);
			EntityEquipment equipment = armorStand.getEquipment();
			equipment.setArmorContents(target.getInventory().getArmorContents());
			equipment.setHelmet(ItemLib.getSkull(target.getName()));
		}

		@Override
		protected void onDurationProcess(int seconds) {
			Location location = armorStand.getLocation();
			location.setYaw(location.getYaw() + 5);
			armorStand.teleport(location);
			for (Location loc : circle.toLocations(armorStand.getEyeLocation().clone().add(0, 0.75, 0))) {
				ParticleLib.REDSTONE.spawnParticle(loc, BLACK);
			}
		}

		@Override
		protected void onDurationEnd() {
			target = null;
			armorStand.remove();
			armorStand = null;
		}

		@Override
		protected void onDurationSilentEnd() {
			target = null;
			armorStand.remove();
			armorStand = null;
		}
	}.setPeriod(TimeUnit.TICKS, 2);

	@Override
	public void TargetSkill(Material materialType, LivingEntity entity) {
		if (materialType.equals(Material.IRON_INGOT) && entity instanceof Player && !skill.isDuration() && !cooldownTimer.isCooldown()) {
			target = (Player) entity;
			skill.start();
		}
	}

	@SubscribeEvent
	private void onEntityDamageByEntity(EntityDamageByEntityEvent e) {
		if (e.getEntity().equals(armorStand)) {
			e.setCancelled(true);
			target.damage(e.getDamage() / 3, e.getDamager());
		}
	}

	@SubscribeEvent
	private void onEntityDamageByBlock(EntityDamageByBlockEvent e) {
		if (e.getEntity().equals(armorStand)) {
			e.setCancelled(true);
			target.damage(e.getDamage() / 3, getPlayer());
		}
	}

	@SubscribeEvent
	private void onEntityDamage(EntityDamageEvent e) {
		if (e.getEntity().equals(armorStand)) {
			e.setCancelled(true);
			target.damage(e.getDamage() / 3, getPlayer());
		}
	}

	@SubscribeEvent
	private void onPlayerArmorStandManipulate(PlayerArmorStandManipulateEvent e) {
		if (e.getRightClicked().equals(armorStand)) e.setCancelled(true);
	}

}
