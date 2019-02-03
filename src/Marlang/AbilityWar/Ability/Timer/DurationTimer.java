package Marlang.AbilityWar.Ability.Timer;

import java.util.ArrayList;

import org.bukkit.ChatColor;

import Marlang.AbilityWar.Ability.AbilityBase;
import Marlang.AbilityWar.Utils.Messager;
import Marlang.AbilityWar.Utils.NumberUtil;
import Marlang.AbilityWar.Utils.PacketUtil.ActionbarObject;
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
	
	abstract public void DurationSkill(Integer Seconds);
	
	public boolean isDuration() {
		if(isTimerRunning()) {
			Messager.sendMessage(Ability.getPlayer(), ChatColor.translateAlternateColorCodes('&', "&6지속 시간 &f" + NumberUtil.parseTimeString(getFixedTime(this.getTempCount()))));
		}
		
		return isTimerRunning();
	}
	
	@Override
	public void TimerStart(Data<?>... args) {
		Counted = new ArrayList<Integer>();
	}
	
	private ArrayList<Integer> Counted;
	
	@Override
	public void TimerProcess(Integer Seconds) {
		this.DurationSkill(Seconds);
		ActionbarObject actionbar = new ActionbarObject(ChatColor.translateAlternateColorCodes('&', "&6지속 시간 &f: &e" + NumberUtil.parseTimeString(getFixedTime(Seconds))));
		actionbar.Send(Ability.getPlayer());
		
		if(getFixedTime(Seconds) == (Duration / 2) && !Counted.contains(getFixedTime(Seconds))) {
			Counted.add(getFixedTime(Seconds));
			Messager.sendMessage(Ability.getPlayer(), ChatColor.translateAlternateColorCodes('&', "&6지속 시간 &f" + NumberUtil.parseTimeString(getFixedTime(Seconds))));
			SoundLib.BLOCK_NOTE_HAT.playSound(Ability.getPlayer());
		} else if(getFixedTime(Seconds) <= 5 && getFixedTime(Seconds) >= 1 && !Counted.contains(getFixedTime(Seconds))) {
			Counted.add(getFixedTime(Seconds));
			Messager.sendMessage(Ability.getPlayer(), ChatColor.translateAlternateColorCodes('&', "&6지속 시간 &f" + NumberUtil.parseTimeString(getFixedTime(Seconds))));
			SoundLib.BLOCK_NOTE_HAT.playSound(Ability.getPlayer());
		}
	}
	
	@Override
	public void TimerEnd() {
		CooldownTimer.StartTimer();
		Messager.sendMessage(Ability.getPlayer(), ChatColor.translateAlternateColorCodes('&', "&6지속 시간&f이 종료되었습니다."));
	}
	
}
