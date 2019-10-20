package daybreak.abilitywar.utils.thread;

import org.bukkit.Bukkit;

import daybreak.abilitywar.AbilityWar;

/**
 * 일반 타이머
 * @author DayBreak 새벽
 */
abstract public class Timer {

	private int Task = -1;

	private boolean InfiniteTimer;
	
	private int MaxCount;
	private int Count;
	private int Period = 20;

	protected abstract void onStart();

	protected abstract void TimerProcess(Integer Seconds);

	protected abstract void onEnd();

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
			onStart();
		}
	}

	/**
	 * 타이머를 종료합니다.
	 */
	public void StopTimer() {
		if(this.isTimerRunning()) {
			Bukkit.getScheduler().cancelTask(Task);
			Count = MaxCount;
			this.Task = -1;
			onEnd();
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
	
	public Timer setPeriod(int Period) {
		this.Period = Period;
		return this;
	}
	
	/**
	 * 일반 타이머
	 */
	public Timer(int Count) {
		InfiniteTimer = false;
		this.MaxCount = Count;
	}
	
	/**
	 * 무한 타이머
	 */
	public Timer() {
		InfiniteTimer = true;
		this.MaxCount = 0;
	}
	
	private final class TimerTask extends Thread {

		@Override
		public void run() {
			if (InfiniteTimer) {
				TimerProcess(Count);
				Count++;
			} else {
				if (Count > 0) {
					TimerProcess(Count);

					if (Count <= 0) {
						StopTimer();
					}

					Count--;
				} else {
					StopTimer();
				}
			}
		}

	}

}