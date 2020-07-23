package daybreak.abilitywar.ability.list;

import daybreak.abilitywar.ability.AbilityBase;
import daybreak.abilitywar.ability.AbilityManifest;
import daybreak.abilitywar.ability.AbilityManifest.Rank;
import daybreak.abilitywar.ability.AbilityManifest.Species;
import daybreak.abilitywar.ability.SubscribeEvent;
import daybreak.abilitywar.ability.decorator.ActiveHandler;
import daybreak.abilitywar.config.ability.AbilitySettings.SettingObject;
import daybreak.abilitywar.game.AbstractGame.Participant;
import daybreak.abilitywar.utils.base.Formatter;
import daybreak.abilitywar.utils.library.ParticleLib;
import daybreak.abilitywar.utils.library.PotionEffects;
import daybreak.abilitywar.utils.library.SoundLib;
import org.bukkit.Material;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

@AbilityManifest(name = "버서커", rank = Rank.B, species = Species.HUMAN, explain = {
		"철괴를 우클릭한 후 5초 안에 하는 다음 근접 공격이 강화됩니다. $[COOLDOWN_CONFIG]",
		"강화된 공격은 $[StrengthConfig]배의 대미지를 내며, 강화된 공격을 사용한 후",
		"$[DebuffConfig]초간 대미지를 입힐 수 없습니다.",
		"지속시간 내에 공격하지 못한 경우, 쿨타임을 절반만 갖습니다."
})
public class Berserker extends AbilityBase implements ActiveHandler {

	public static final SettingObject<Integer> COOLDOWN_CONFIG = abilitySettings.new SettingObject<Integer>(Berserker.class, "Cooldown", 80,
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

	public static final SettingObject<Double> StrengthConfig = abilitySettings.new SettingObject<Double>(Berserker.class, "Strength", 2.5,
			"# 공격 강화 배수") {

		@Override
		public boolean condition(Double value) {
			return value >= 2;
		}

	};

	public static final SettingObject<Integer> DebuffConfig = abilitySettings.new SettingObject<Integer>(Berserker.class, "Debuff", 5,
			"# 능력 사용 후 디버프를 받는 시간",
			"# 단위 : 초") {

		@Override
		public boolean condition(Integer value) {
			return value >= 1;
		}

	};

	public Berserker(Participant participant) {
		super(participant);
	}

	private final Cooldown cooldownTimer = new Cooldown(COOLDOWN_CONFIG.getValue());

	private class BerserkerTimer extends Duration {

		private BerserkerTimer() {
			super(5);
		}

		@Override
		protected void onDurationProcess(int count) {
		}

		@Override
		protected void onDurationEnd() {
			cooldownTimer.start();
			cooldownTimer.setCount(cooldownTimer.getCooldown() / 2);
		}

		public boolean stop() {
			boolean bool = super.stop(true);
			cooldownTimer.start();
			return bool;
		}

	}

	private final BerserkerTimer berserkerTimer = new BerserkerTimer();

	@Override
	public boolean ActiveSkill(Material material, ClickType clickType) {
		if (material == Material.IRON_INGOT && clickType == ClickType.RIGHT_CLICK) {
			if (!berserkerTimer.isDuration() && !cooldownTimer.isCooldown()) {
				berserkerTimer.start();
				return true;
			}
		}

		return false;
	}

	private final double strength = StrengthConfig.getValue();
	private final int debuff = DebuffConfig.getValue();

	@SubscribeEvent
	public void onEntityDamageByEntity(EntityDamageByEntityEvent e) {
		if (e.getDamager().equals(getPlayer()) && !e.isCancelled()) {
			if (berserkerTimer.isRunning()) {
				berserkerTimer.stop();
				e.setDamage(e.getDamage() * strength);
				SoundLib.ENTITY_PLAYER_ATTACK_SWEEP.playSound(getPlayer());
				ParticleLib.SWEEP_ATTACK.spawnParticle(e.getEntity().getLocation(), 1, 1, 1, 5);
				PotionEffects.WEAKNESS.addPotionEffect(getPlayer(), debuff * 20, 1, true);
			}
		}
	}

}
