package DayBreak.AbilityWar.Ability.Timer;

import java.util.ArrayList;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import DayBreak.AbilityWar.Ability.AbilityBase;
import DayBreak.AbilityWar.Utils.Library.SoundLib;
import DayBreak.AbilityWar.Utils.Library.TItle.Actionbar;
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
			Ability.getPlayer().sendMessage(ChatColor.translateAlternateColorCodes('&', "&6지속 시간 &f" + NumberUtil.parseTimeString(this.getFixedCount())));
		}
		
		return isTimerRunning();
	}
	
	@Override
	public DurationTimer setPeriod(int Period) {
		super.setPeriod(Period);
		return this;
	}

	@Override
	public DurationTimer setSilentNotice(boolean forcedStopNotice) {
		super.setSilentNotice(forcedStopNotice);
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
		Player target = Ability.getPlayer();
		if(target != null) {
			this.DurationProcess(Seconds);
			
			Actionbar actionbar = new Actionbar(ChatColor.translateAlternateColorCodes('&', "&6지속 시간 &f: &e" + NumberUtil.parseTimeString(this.getFixedCount())), 0, 25, 0);
			actionbar.sendTo(target);
			
			if(this.getFixedCount() == (Duration / 2) && !Counted.contains(this.getFixedCount())) {
				Counted.add(this.getFixedCount());
				target.sendMessage(ChatColor.translateAlternateColorCodes('&', "&6지속 시간 &f" + NumberUtil.parseTimeString(this.getFixedCount())));
				SoundLib.BLOCK_NOTE_BLOCK_HAT.playSound(target);
			} else if(this.getFixedCount() <= 5 && this.getFixedCount() >= 1 && !Counted.contains(this.getFixedCount())) {
				Counted.add(this.getFixedCount());
				target.sendMessage(ChatColor.translateAlternateColorCodes('&', "&6지속 시간 &f" + NumberUtil.parseTimeString(this.getFixedCount())));
				SoundLib.BLOCK_NOTE_BLOCK_HAT.playSound(target);
			}
		}
	}
	
	@Override
	protected void onEnd() {
		Player target = Ability.getPlayer();
		if(target != null) {
			//Notify
			this.onDurationEnd();
			
			if(CooldownTimer != null) {
				CooldownTimer.StartTimer();
			}
			
			target.sendMessage(ChatColor.translateAlternateColorCodes('&', "&6지속 시간&f이 종료되었습니다."));
		}
	}
	
}
