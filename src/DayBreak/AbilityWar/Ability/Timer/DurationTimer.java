package DayBreak.AbilityWar.Ability.Timer;

import java.util.ArrayList;

import org.bukkit.ChatColor;

import DayBreak.AbilityWar.Ability.AbilityBase;
import DayBreak.AbilityWar.Utils.Messager;
import DayBreak.AbilityWar.Utils.Library.SoundLib;
import DayBreak.AbilityWar.Utils.Library.Packet.ActionbarPacket;
import DayBreak.AbilityWar.Utils.Math.NumberUtil;
import DayBreak.AbilityWar.Utils.Thread.TimerBase;

/**
 * Duration Timer (지속시간 타이머)
 * @author DayBreak 새벽
 */
abstract public class DurationTimer extends TimerBase {

	/**
	 * 지속시간 초기화
	 */
	public static void ResetDuration() {
		TimerBase.StopTasks(DurationTimer.class);
	}
	
	private final AbilityBase Ability;
	private final CooldownTimer CooldownTimer;
	private final int Duration;

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
			Messager.sendMessage(Ability.getPlayer(), ChatColor.translateAlternateColorCodes('&', "&6지속 시간 &f" + NumberUtil.parseTimeString(this.getFixedCount())));
		}
		
		return isTimerRunning();
	}
	
	@Override
	public DurationTimer setPeriod(int Period) {
		super.setPeriod(Period);
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
		
		ActionbarPacket actionbar = new ActionbarPacket(ChatColor.translateAlternateColorCodes('&', "&6지속 시간 &f: &e" + NumberUtil.parseTimeString(this.getFixedCount())), 0, 25, 0);
		actionbar.Send(Ability.getPlayer());
		
		if(this.getFixedCount() == (Duration / 2) && !Counted.contains(this.getFixedCount())) {
			Counted.add(this.getFixedCount());
			Messager.sendMessage(Ability.getPlayer(), ChatColor.translateAlternateColorCodes('&', "&6지속 시간 &f" + NumberUtil.parseTimeString(this.getFixedCount())));
			SoundLib.BLOCK_NOTE_BLOCK_HAT.playSound(Ability.getPlayer());
		} else if(this.getFixedCount() <= 5 && this.getFixedCount() >= 1 && !Counted.contains(this.getFixedCount())) {
			Counted.add(this.getFixedCount());
			Messager.sendMessage(Ability.getPlayer(), ChatColor.translateAlternateColorCodes('&', "&6지속 시간 &f" + NumberUtil.parseTimeString(this.getFixedCount())));
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
