package daybreak.abilitywar.ability.list;

import daybreak.abilitywar.ability.AbilityBase;
import daybreak.abilitywar.ability.AbilityManifest;
import daybreak.abilitywar.ability.AbilityManifest.Rank;
import daybreak.abilitywar.ability.AbilityManifest.Species;
import daybreak.abilitywar.ability.SubscribeEvent;
import daybreak.abilitywar.ability.timer.CooldownTimer;
import daybreak.abilitywar.ability.timer.DurationTimer;
import daybreak.abilitywar.config.AbilitySettings.SettingObject;
import daybreak.abilitywar.game.games.mode.AbstractGame.Participant;
import daybreak.abilitywar.utils.Messager;
import daybreak.abilitywar.utils.library.EffectLib;
import org.bukkit.ChatColor;
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

	public static final SettingObject<Integer> StrengthConfig = new SettingObject<Integer>(Berserker.class, "Strength", 4,
			"# 공격 강화 배수") {
		
		@Override
		public boolean Condition(Integer value) {
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
				ChatColor.translateAlternateColorCodes('&', "&f철괴를 우클릭한 후 5초 안에 하는 다음 공격이 강화됩니다. " + Messager.formatCooldown(CooldownConfig.getValue())),
				ChatColor.translateAlternateColorCodes('&', "&f강화된 공격은 " + StrengthConfig.getValue() + "배의 데미지를 내며, 강화된 공격을 사용한 후"),
				ChatColor.translateAlternateColorCodes('&', "&f" + DebuffConfig.getValue() + "초간 데미지를 입힐 수 없습니다."));
	}

	private final int Strength = StrengthConfig.getValue();
	
	private final CooldownTimer Cool = new CooldownTimer(this, CooldownConfig.getValue());
	
	private final DurationTimer Duration = new DurationTimer(this, 5, Cool) {
		
		@Override
		public void onDurationStart() {
			Strengthen = true;
		}
		
		@Override
		public void onDurationProcess(int seconds) {}
		
		@Override
		public void onDurationEnd() {
			Strengthen = false;
		}
		
	};
	
	private boolean Strengthen = false;
	
	@Override
	public boolean ActiveSkill(MaterialType mt, ClickType ct) {
		if(mt.equals(MaterialType.IRON_INGOT)) {
			if(ct.equals(ClickType.RIGHT_CLICK)) {
				if(!Duration.isDuration() && !Cool.isCooldown()) {
					Duration.startTimer();
					
					return true;
				}
			}
		}
		
		return false;
	}

	private final int DebuffTime = DebuffConfig.getValue();
	
	@SubscribeEvent
	public void onEntityDamageByEntity(EntityDamageByEntityEvent e) {
		if(e.getDamager().equals(getPlayer()) && !e.isCancelled()) {
			if(Strengthen) {
				if(Duration.isDuration()) Duration.stopTimer(false);
				e.setDamage(e.getDamage() * Strength);
				EffectLib.WEAKNESS.addPotionEffect(getPlayer(), DebuffTime * 20, 1, true);
			}
		}
	}
	
	@Override
	public void onRestrictClear() {}

	@Override
	public void TargetSkill(MaterialType mt, LivingEntity entity) {}
	
}
