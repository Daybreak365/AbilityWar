package daybreak.abilitywar.ability.list;

import daybreak.abilitywar.ability.AbilityBase;
import daybreak.abilitywar.ability.AbilityManifest;
import daybreak.abilitywar.ability.AbilityManifest.Rank;
import daybreak.abilitywar.ability.AbilityManifest.Species;
import daybreak.abilitywar.ability.SubscribeEvent;
import daybreak.abilitywar.ability.decorator.ActiveHandler;
import daybreak.abilitywar.config.ability.AbilitySettings.SettingObject;
import daybreak.abilitywar.game.games.mode.AbstractGame.Participant;
import daybreak.abilitywar.utils.base.Messager;
import daybreak.abilitywar.utils.library.ParticleLib;
import daybreak.abilitywar.utils.library.PotionEffects;
import daybreak.abilitywar.utils.library.SoundLib;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

@AbilityManifest(Name = "버서커", Rank = Rank.B, Species = Species.HUMAN)
public class Berserker extends AbilityBase implements ActiveHandler {

	public static final SettingObject<Integer> CooldownConfig = new SettingObject<Integer>(Berserker.class, "Cooldown", 80,
			"# 쿨타임") {

		@Override
		public boolean Condition(Integer value) {
			return value >= 0;
		}

	};

	public static final SettingObject<Double> StrengthConfig = new SettingObject<Double>(Berserker.class, "Strength", 2.5,
			"# 공격 강화 배수") {

		@Override
		public boolean Condition(Double value) {
			return value >= 2;
		}

	};

	public static final SettingObject<Integer> DebuffConfig = new SettingObject<Integer>(Berserker.class, "Debuff", 5,
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
				ChatColor.translateAlternateColorCodes('&', "&f" + DebuffConfig.getValue() + "초간 대미지를 입힐 수 없습니다."),
				ChatColor.translateAlternateColorCodes('&', "&f지속시간 내에 공격하지 못한 경우, 쿨타임을 절반만 갖습니다."));
	}

	private final double strength = StrengthConfig.getValue();
	private final CooldownTimer cooldownTimer = new CooldownTimer(CooldownConfig.getValue());

	private class BerserkerTimer extends DurationTimer {

		private BerserkerTimer() {
			super(5);
		}

		@Override
		protected void onDurationProcess(int count) {
		}

		@Override
		protected void onDurationEnd() {
			cooldownTimer.start();
			cooldownTimer.setCount(cooldownTimer.getMaximumCount() / 2);
		}

		public boolean stop() {
			boolean bool = super.stop(true);
			cooldownTimer.start();
			return bool;
		}

	}

	private final BerserkerTimer berserkerTimer = new BerserkerTimer();

	@Override
	public boolean ActiveSkill(Material materialType, ClickType clickType) {
		if (materialType.equals(Material.IRON_INGOT) && clickType == ClickType.RIGHT_CLICK) {
			if (!berserkerTimer.isDuration() && !cooldownTimer.isCooldown()) {
				berserkerTimer.start();
				return true;
			}
		}

		return false;
	}

	private final int debuffTime = DebuffConfig.getValue();

	@SubscribeEvent
	public void onEntityDamageByEntity(EntityDamageByEntityEvent e) {
		if (e.getDamager().equals(getPlayer()) && !e.isCancelled()) {
			if (berserkerTimer.isRunning()) {
				berserkerTimer.stop();
				e.setDamage(e.getDamage() * strength);
				SoundLib.ENTITY_PLAYER_ATTACK_SWEEP.playSound(getPlayer());
				ParticleLib.SWEEP_ATTACK.spawnParticle(e.getEntity().getLocation(), 1, 1, 1, 5);
				PotionEffects.WEAKNESS.addPotionEffect(getPlayer(), debuffTime * 20, 1, true);
			}
		}
	}

	@Override
	public void TargetSkill(Material materialType, LivingEntity entity) {
	}

}
