package daybreak.abilitywar.ability.list;

import daybreak.abilitywar.ability.AbilityBase;
import daybreak.abilitywar.ability.AbilityManifest;
import daybreak.abilitywar.ability.AbilityManifest.Rank;
import daybreak.abilitywar.ability.AbilityManifest.Species;
import daybreak.abilitywar.ability.SubscribeEvent;
import daybreak.abilitywar.config.ability.AbilitySettings.SettingObject;
import daybreak.abilitywar.game.games.mode.AbstractGame.Participant;
import daybreak.abilitywar.utils.Messager;
import daybreak.abilitywar.utils.library.PotionEffects;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

@AbilityManifest(Name = "버서커", Rank = Rank.B, Species = Species.HUMAN)
public class Berserker extends AbilityBase {

	public static final SettingObject<Integer> CooldownConfig = new SettingObject<Integer>(Berserker.class, "Cooldown", 80,
			"# 쿨타임") {

		@Override
		public boolean Condition(Integer value) {
			return value >= 0;
		}

	};

	public static final SettingObject<Double> StrengthConfig = new SettingObject<Double>(Berserker.class, "Strength", 5.0,
			"# 공격 강화 배수") {

		@Override
		public boolean Condition(Double value) {
			return value >= 2;
		}

	};

	public static final SettingObject<Integer> DebuffConfig = new SettingObject<Integer>(Berserker.class, "Debuff", 10,
			"# 능력 사용 후 디버프를 받는 시간",
			"# 단위 : 초") {

		@Override
		public boolean Condition(Integer value) {
			return value >= 1;
		}

	};

	public Berserker(Participant participant) {
		super(participant,
				ChatColor.translateAlternateColorCodes('&', "&f철괴를 우클릭한 후 5초 안에 하는 다음 근접 공격이 강화됩니다. " + Messager.formatCooldown(CooldownConfig.getValue())),
				ChatColor.translateAlternateColorCodes('&', "&f강화된 공격은 " + StrengthConfig.getValue() + "배의 대미지를 내며, 강화된 공격을 사용한 후"),
				ChatColor.translateAlternateColorCodes('&', "&f" + DebuffConfig.getValue() + "초간 대미지를 입힐 수 없습니다."));
	}

	private final double strength = StrengthConfig.getValue();
	private final CooldownTimer cooldownTimer = new CooldownTimer(CooldownConfig.getValue());
	private final DurationTimer durationTimer = new DurationTimer(5, cooldownTimer) {
		@Override
		public void onDurationProcess(int seconds) {
		}
	};

	@Override
	public boolean ActiveSkill(Material materialType, ClickType clickType) {
		if (materialType.equals(Material.IRON_INGOT) && clickType == ClickType.RIGHT_CLICK) {
			if (!durationTimer.isDuration() && !cooldownTimer.isCooldown()) {
				durationTimer.startTimer();
				return true;
			}
		}

		return false;
	}

	private final int debuffTime = DebuffConfig.getValue();

	@SubscribeEvent
	public void onEntityDamageByEntity(EntityDamageByEntityEvent e) {
		if (e.getDamager().equals(getPlayer()) && !e.isCancelled()) {
			if (durationTimer.isRunning()) {
				durationTimer.stopTimer(false);
				e.setDamage(e.getFinalDamage() * strength);
				PotionEffects.WEAKNESS.addPotionEffect(getPlayer(), debuffTime * 20, 1, true);
			}
		}
	}

	@Override
	public void TargetSkill(Material materialType, LivingEntity entity) {
	}

}
