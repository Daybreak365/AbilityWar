package daybreak.abilitywar.utils.thread;

import daybreak.abilitywar.AbilityWar;
import org.bukkit.Bukkit;

/**
 * 전역 타이머
 * @author DayBreak 새벽
 */
abstract public class OverallTimer {

	private int task = -1;

	private boolean isInfinite;
	
	private int maxCount;
	private int count;
	private int period = 20;

	protected abstract void onStart();

	protected abstract void onProcess(Integer Seconds);

	protected abstract void onEnd();

	public boolean isRunning() {
		return task != -1;
	}

	/**
	 * 타이머를 시작합니다.
	 */
	public void startTimer() {
		if(!isRunning()) {
			count = maxCount;
			this.task = Bukkit.getScheduler().scheduleSyncRepeatingTask(AbilityWar.getPlugin(), new TimerTask(), 0, period);
			onStart();
		}
	}

	/**
	 * 타이머를 종료합니다.
	 */
	public void stopTimer() {
		if(isRunning()) {
			Bukkit.getScheduler().cancelTask(task);
			count = maxCount;
			this.task = -1;
			onEnd();
		}
	}
	
	public int getMaxCount() {
		return maxCount;
	}
	
	public int getCount() {
		return count;
	}

	public int getFixedCount() {
		return count / (20 / period);
	}
	
	public OverallTimer setPeriod(int Period) {
		this.period = Period;
		return this;
	}
	
	/**
	 * 일반 타이머
	 */
	public OverallTimer(int Count) {
		isInfinite = false;
		this.maxCount = Count;
	}
	
	/**
	 * 무한 타이머
	 */
	public OverallTimer() {
		isInfinite = true;
		this.maxCount = 0;
	}
	
	private final class TimerTask extends Thread {

		@Override
		public void run() {
			if (isInfinite) {
				onProcess(count);
				count++;
			} else {
				if (count > 0) {
					onProcess(count);
					count--;
				} else {
					stopTimer();
				}
			}
		}

	}

}