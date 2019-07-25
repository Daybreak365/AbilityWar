package DayBreak.AbilityWar.Utils.Thread;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;

import DayBreak.AbilityWar.AbilityWar;

/**
 * 게임 중 실행되는 타이머
 * @author DayBreak 새벽
 */
abstract public class TimerBase {

	private static List<TimerBase> Tasks = new ArrayList<TimerBase>();
	
	public static List<TimerBase> getTasks() {
		return new ArrayList<TimerBase>(Tasks);
	}

	public static void StopTasks(Class<? extends TimerBase> timerClass) {
		for(TimerBase timer : getTasks()) {
			if(timerClass.isAssignableFrom(timer.getClass())) {
				timer.StopTimer(false);
			}
		}
	}
	
	public static void ResetTasks() {
		for(TimerBase timer : getTasks()) {
			timer.StopTimer(true);
		}
		
		Tasks = new ArrayList<TimerBase>();
	}

	/**
	 * Register TimerBase
	 * @param timer
	 */
	private static void Register(TimerBase timer) {
		Tasks.add(timer);
	}

	/**
	 * Unregister TimerBase
	 * @param timer
	 */
	private static void Unregister(TimerBase timer) {
		Tasks.remove(timer);
	}

	private int Task = -1;

	private boolean InfiniteTimer;
	
	private int MaxCount;
	private int Count;
	private int Period = 20;
	private boolean ForcedStopNotice = false;

	abstract protected void onStart();

	abstract protected void TimerProcess(Integer Seconds);

	abstract protected void onEnd();

	public boolean isTimerRunning() {
		return Task != -1;
	}

	/**
	 * 타이머를 시작합니다.
	 */
	public void StartTimer() {
		if(!this.isTimerRunning()) {
			Count = MaxCount;
			this.Task = Bukkit.getScheduler().scheduleSyncRepeatingTask(AbilityWar.getPlugin(), new TimerTask(), 0, Period);
			Register(this);
			onStart();
		}
	}

	/**
	 * 타이머를 종료합니다.
	 */
	public void StopTimer(boolean Silent) {
		if(this.isTimerRunning()) {
			Bukkit.getScheduler().cancelTask(Task);
			Unregister(this);
			Count = MaxCount;
			this.Task = -1;
			if(!Silent || ForcedStopNotice) {
				onEnd();
			}
		}
	}
	
	public int getMaxCount() {
		return MaxCount;
	}
	
	public int getCount() {
		return Count;
	}

	public int getFixedCount() {
		return (int) (Count / (20 / Period));
	}
	
	public TimerBase setPeriod(int Period) {
		this.Period = Period;
		return this;
	}
	
	protected boolean isForcedStopNotice() {
		return ForcedStopNotice;
	}

	public TimerBase setForcedStopNotice(boolean forcedStopNotice) {
		ForcedStopNotice = forcedStopNotice;
		return this;
	}

	/**
	 * 일반 타이머
	 */
	public TimerBase(int Count) {
		InfiniteTimer = false;
		this.MaxCount = Count;
	}
	
	/**
	 * 무한 타이머
	 */
	public TimerBase() {
		InfiniteTimer = true;
		this.MaxCount = -1;
	}
	
	private final class TimerTask extends Thread {

		@Override
		public void run() {
			if (AbilityWarThread.isGameTaskRunning()) {
				if (InfiniteTimer) {
					TimerProcess(-1);
				} else {
					if (Count > 0) {
						TimerProcess(Count);

						if (Count <= 0) {
							StopTimer(false);
						}

						Count--;
					} else {
						StopTimer(false);
					}
				}
			} else {
				StopTimer(true);
			}
		}

	}

}