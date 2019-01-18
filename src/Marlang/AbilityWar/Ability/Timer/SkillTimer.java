package Marlang.AbilityWar.Ability.Timer;

import org.bukkit.ChatColor;

import Marlang.AbilityWar.Ability.AbilityBase;
import Marlang.AbilityWar.Utils.Messager;
import Marlang.AbilityWar.Utils.TimerBase;

abstract public class SkillTimer extends TimerBase {
	
	AbilityBase Ability;
	SkillType SkillType;
	
	DurationTimer Duration;
	CooldownTimer Cool;
	
	/**
	 * 쿨타임이 없는 스킬
	 */
	public SkillTimer(AbilityBase Ability, Integer Count, SkillType SkillType) {
		super(Count);
		this.Ability = Ability;
		this.SkillType = SkillType;
	}
	
	/**
	 * 쿨타임이 있는 즉발 스킬
	 */
	public SkillTimer(AbilityBase Ability, Integer Count, SkillType SkillType, CooldownTimer Cool) {
		super(Count);
		this.Ability = Ability;
		this.SkillType = SkillType;
		this.Cool = Cool;
	}
	
	@Override
	public void TimerEnd() {
		if(Duration != null) {
			Duration.StartTimer();
		} else {
			if(Cool != null) {
				Cool.StartTimer();
			}
		}
	}
	
	public void Execute() {
		if(!this.isTimerRunning()) {
			Messager.sendMessage(Ability.getPlayer(), ChatColor.translateAlternateColorCodes('&', "&d능력을 사용하였습니다!"));
			this.StartTimer();
		}
	}
	
	public enum SkillType {
		Active(ChatColor.translateAlternateColorCodes('&', "&d능력을 사용하였습니다.")),
		Passive(ChatColor.translateAlternateColorCodes('&', "&d패시브가 발동되었습니다."));
		
		String Message;
		
		private SkillType(String msg) {
			this.Message = msg;
		}
		
		public String getMessage() {
			return Message;
		}
		
	}
	
}
