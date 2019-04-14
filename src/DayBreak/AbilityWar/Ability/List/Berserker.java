package DayBreak.AbilityWar.Ability.List;

import org.bukkit.ChatColor;
import org.bukkit.entity.Entity;
import org.bukkit.event.Event;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

import DayBreak.AbilityWar.Ability.AbilityBase;
import DayBreak.AbilityWar.Ability.AbilityManifest;
import DayBreak.AbilityWar.Ability.AbilityManifest.Rank;
import DayBreak.AbilityWar.Ability.Timer.CooldownTimer;
import DayBreak.AbilityWar.Ability.Timer.DurationTimer;
import DayBreak.AbilityWar.Config.AbilitySettings.SettingObject;
import DayBreak.AbilityWar.Game.Games.AbstractGame.Participant;
import DayBreak.AbilityWar.Utils.Messager;
import DayBreak.AbilityWar.Utils.Library.EffectLib;

@AbilityManifest(Name = "버서커", Rank = Rank.B)
public class Berserker extends AbilityBase {

	public static SettingObject<Integer> CooldownConfig = new SettingObject<Integer>(Berserker.class, "Cooldown", 80,
			"# 쿨타임") {
		
		@Override
		public boolean Condition(Integer value) {
			return value >= 0;
		}
		
	};

	public static SettingObject<Integer> StrengthConfig = new SettingObject<Integer>(Berserker.class, "Strength", 3,
			"# 공격 강화 배수") {
		
		@Override
		public boolean Condition(Integer value) {
			return value >= 2;
		}
		
	};

	public static SettingObject<Integer> DebuffConfig = new SettingObject<Integer>(Berserker.class, "Debuff", 10,
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

	Integer Strength = StrengthConfig.getValue();
	
	CooldownTimer Cool = new CooldownTimer(this, CooldownConfig.getValue());
	
	DurationTimer Duration = new DurationTimer(this, 5, Cool) {
		
		@Override
		public void onDurationStart() {
			Strengthen = true;
		}
		
		@Override
		public void DurationProcess(Integer Seconds) {}
		
		@Override
		public void onDurationEnd() {
			Strengthen = false;
		}
		
	};
	
	boolean Strengthen = false;
	
	@Override
	public boolean ActiveSkill(MaterialType mt, ClickType ct) {
		if(mt.equals(MaterialType.Iron_Ingot)) {
			if(ct.equals(ClickType.RightClick)) {
				if(!Duration.isDuration() && !Cool.isCooldown()) {
					Duration.StartTimer();
					
					return true;
				}
			}
		}
		
		return false;
	}

	Integer DebuffTime = DebuffConfig.getValue();
	
	@Override
	public void PassiveSkill(Event event) {
		if(event instanceof EntityDamageByEntityEvent) {
			EntityDamageByEntityEvent e = (EntityDamageByEntityEvent) event;
			if(e.getDamager().equals(getPlayer())) {
				if(Strengthen) {
					if(Duration.isDuration()) Duration.StopTimer(false);
					e.setDamage(e.getDamage() * Strength);
					EffectLib.WEAKNESS.addPotionEffect(getPlayer(), DebuffTime * 20, 1, true);
				}
			}
		}
	}

	@Override
	public void onRestrictClear() {}

	@Override
	public void TargetSkill(MaterialType mt, Entity entity) {}
	
}
