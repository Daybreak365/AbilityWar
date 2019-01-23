package Marlang.AbilityWar.Ability.Timer;

import org.bukkit.ChatColor;

import Marlang.AbilityWar.Ability.AbilityBase;
import Marlang.AbilityWar.Utils.Messager;
import Marlang.AbilityWar.Utils.NumberUtil;
import Marlang.AbilityWar.Utils.TimerBase;
import Marlang.AbilityWar.Utils.Library.SoundLib;

/**
 * Duration Timer
 * @author _Marlang 말랑
 */
abstract public class DurationTimer extends TimerBase {
	
	AbilityBase Ability;
	CooldownTimer CooldownTimer;
	Integer Duration;
	
	public DurationTimer(AbilityBase Ability, Integer Duration, CooldownTimer CooldownTimer) {
		super(Duration);
		this.Ability = Ability;
		this.Duration = Duration;
		this.CooldownTimer = CooldownTimer;
	}
	
	abstract public void DurationSkill();
	
	public boolean isDuration() {
		if(isTimerRunning()) {
			Messager.sendMessage(Ability.getPlayer(), ChatColor.translateAlternateColorCodes('&', "&6지속 시간 &f" + NumberUtil.parseTimeString(this.getTempCount())));
		}
		
		return isTimerRunning();
	}
	
	@Override
	public void TimerStart() {}
	
	@Override
	public void TimerProcess(Integer Seconds) {
		this.DurationSkill();
		
		if(Seconds == (Duration / 2)) {
			Messager.sendMessage(Ability.getPlayer(), ChatColor.translateAlternateColorCodes('&', "&6지속 시간 &f" + NumberUtil.parseTimeString(Seconds)));
			SoundLib.BLOCK_NOTE_HAT.playSound(Ability.getPlayer());
		}
		
		if(Seconds <= 5 && Seconds >= 1) {
			Messager.sendMessage(Ability.getPlayer(), ChatColor.translateAlternateColorCodes('&', "&6지속 시간 &f" + NumberUtil.parseTimeString(Seconds)));
			SoundLib.BLOCK_NOTE_HAT.playSound(Ability.getPlayer());
		}
	}
	
	@Override
	public void TimerEnd() {
		CooldownTimer.StartTimer();
		Messager.sendMessage(Ability.getPlayer(), ChatColor.translateAlternateColorCodes('&', "&6지속 시간&f이 종료되었습니다."));
	}
	
}
