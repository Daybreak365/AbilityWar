package Marlang.AbilityWar.Ability.Timer;

import java.util.ArrayList;

import org.bukkit.ChatColor;

import Marlang.AbilityWar.Ability.AbilityBase;
import Marlang.AbilityWar.Utils.Messager;
import Marlang.AbilityWar.Utils.Library.SoundLib;
import Marlang.AbilityWar.Utils.Library.Packet.ActionbarPacket;
import Marlang.AbilityWar.Utils.Math.NumberUtil;
import Marlang.AbilityWar.Utils.Thread.TimerBase;

/**
 * Duration Timer
 * @author _Marlang 말랑
 */
abstract public class DurationTimer extends TimerBase {
	
	private final AbilityBase Ability;
	private final CooldownTimer CooldownTimer;
	private final Integer Duration;

	public DurationTimer(AbilityBase Ability, Integer Duration, CooldownTimer CooldownTimer) {
		super(Duration);
		this.Ability = Ability;
		this.Duration = Duration;
		this.CooldownTimer = CooldownTimer;
	}

	public DurationTimer(AbilityBase Ability, Integer Duration) {
		super(Duration);
		this.Ability = Ability;
		this.Duration = Duration;
		this.CooldownTimer = null;
	}
	
	abstract protected void onDurationStart();
	
	abstract protected void DurationProcess(Integer Seconds);
	
	abstract protected void onDurationEnd();
	
	public boolean isDuration() {
		if(isTimerRunning()) {
			Messager.sendMessage(Ability.getPlayer(), ChatColor.translateAlternateColorCodes('&', "&6지속 시간 &f" + NumberUtil.parseTimeString(getFixedTime(this.getTempCount()))));
		}
		
		return isTimerRunning();
	}
	
	@Override
	public DurationTimer setPeriod(Integer Period) {
		super.setPeriod(Period);
		return this;
	}
	
	@Override
	public DurationTimer setProcessDuringGame(boolean bool) {
		super.setProcessDuringGame(bool);
		return this;
	}
	
	@Override
	protected void onStart() {
		//Notify
		this.onDurationStart();
		
		Counted = new ArrayList<Integer>();
	}
	
	private ArrayList<Integer> Counted;
	
	@Override
	protected void TimerProcess(Integer Seconds) {
		//Notify
		this.DurationProcess(Seconds);
		
		ActionbarPacket actionbar = new ActionbarPacket(ChatColor.translateAlternateColorCodes('&', "&6지속 시간 &f: &e" + NumberUtil.parseTimeString(getFixedTime(Seconds))), 0, 25, 0);
		actionbar.Send(Ability.getPlayer());
		
		if(getFixedTime(Seconds) == (Duration / 2) && !Counted.contains(getFixedTime(Seconds))) {
			Counted.add(getFixedTime(Seconds));
			Messager.sendMessage(Ability.getPlayer(), ChatColor.translateAlternateColorCodes('&', "&6지속 시간 &f" + NumberUtil.parseTimeString(getFixedTime(Seconds))));
			SoundLib.BLOCK_NOTE_BLOCK_HAT.playSound(Ability.getPlayer());
		} else if(getFixedTime(Seconds) <= 5 && getFixedTime(Seconds) >= 1 && !Counted.contains(getFixedTime(Seconds))) {
			Counted.add(getFixedTime(Seconds));
			Messager.sendMessage(Ability.getPlayer(), ChatColor.translateAlternateColorCodes('&', "&6지속 시간 &f" + NumberUtil.parseTimeString(getFixedTime(Seconds))));
			SoundLib.BLOCK_NOTE_BLOCK_HAT.playSound(Ability.getPlayer());
		}
	}
	
	@Override
	protected void onEnd() {
		//Notify
		this.onDurationEnd();
		
		if(CooldownTimer != null) {
			CooldownTimer.StartTimer();
		}
		
		Messager.sendMessage(Ability.getPlayer(), ChatColor.translateAlternateColorCodes('&', "&6지속 시간&f이 종료되었습니다."));
	}
	
}
