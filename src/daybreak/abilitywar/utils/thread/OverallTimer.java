package daybreak.abilitywar.utils.thread;

import daybreak.abilitywar.AbilityWar;
import org.bukkit.Bukkit;

/**
 * 전역 타이머
 *
 * @author Daybreak 새벽
 */
public abstract class OverallTimer {

	private int task = -1;

	private boolean isInfinite;

	private int maxCount;
	private int count;
	private int period = 20;

	protected void onStart() {
	}

	protected abstract void onProcess(int seconds);

	protected void onEnd() {
	}

	public boolean isRunning() {
		return task != -1;
	}

	/**
	 * 타이머를 시작합니다.
	 */
	public void startTimer() {
		if (!isRunning()) {
			count = maxCount;
			this.task = Bukkit.getScheduler().scheduleSyncRepeatingTask(AbilityWar.getPlugin(), new TimerTask(), 0, period);
			onStart();
		}
	}

	/**
	 * 타이머를 종료합니다.
	 */
	public void stopTimer() {
		if (isRunning()) {
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
	 * maxCount 이후 종료되는 일반 {@link OverallTimer}를 만듭니다.
	 */
	public OverallTimer(int Count) {
		isInfinite = false;
		this.maxCount = Count;
	}

	/**
	 * 종료되지 않는 {@link OverallTimer}를 만듭니다.
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