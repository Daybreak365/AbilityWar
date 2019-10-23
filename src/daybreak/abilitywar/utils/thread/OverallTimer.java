package daybreak.abilitywar.utils.thread;

import org.bukkit.Bukkit;

import daybreak.abilitywar.AbilityWar;

/**
 * 전역 타이머
 * @author DayBreak 새벽
 */
abstract public class OverallTimer {

	private int Task = -1;

	private boolean InfiniteTimer;
	
	private int MaxCount;
	private int Count;
	private int Period = 20;

	protected abstract void onStart();

	protected abstract void onProcess(Integer Seconds);

	protected abstract void onEnd();

	public boolean isRunning() {
		return Task != -1;
	}

	/**
	 * 타이머를 시작합니다.
	 */
	public void StartTimer() {
		if(!isRunning()) {
			Count = MaxCount;
			this.Task = Bukkit.getScheduler().scheduleSyncRepeatingTask(AbilityWar.getPlugin(), new TimerTask(), 0, Period);
			onStart();
		}
	}

	/**
	 * 타이머를 종료합니다.
	 */
	public void StopTimer() {
		if(isRunning()) {
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
		return Count / (20 / Period);
	}
	
	public OverallTimer setPeriod(int Period) {
		this.Period = Period;
		return this;
	}
	
	/**
	 * 일반 타이머
	 */
	public OverallTimer(int Count) {
		InfiniteTimer = false;
		this.MaxCount = Count;
	}
	
	/**
	 * 무한 타이머
	 */
	public OverallTimer() {
		InfiniteTimer = true;
		this.MaxCount = 0;
	}
	
	private final class TimerTask extends Thread {

		@Override
		public void run() {
			if (InfiniteTimer) {
				onProcess(Count);
				Count++;
			} else {
				if (Count > 0) {
					onProcess(Count);

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