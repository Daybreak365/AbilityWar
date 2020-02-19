package daybreak.abilitywar.utils.base.concurrent;

import com.google.common.base.Preconditions;
import daybreak.abilitywar.AbilityWar;
import org.bukkit.Bukkit;

public abstract class SimpleTimer {

	private final TaskType taskType;
	private final int maximumCount;
	private Task task = null;
	private int taskId = -1;

	public SimpleTimer(TaskType taskType, int maximumCount) {
		this.taskType = taskType;
		this.maximumCount = maximumCount;
	}

	private int initialDelay = 0;
	private int period = 20;

	public SimpleTimer setInitialDelay(TimeUnit timeUnit, int initialDelay) {
		Preconditions.checkNotNull(timeUnit);
		this.initialDelay = timeUnit.toTicks(initialDelay);
		return this;
	}

	public SimpleTimer setPeriod(TimeUnit timeUnit, int period) {
		Preconditions.checkNotNull(timeUnit);
		this.period = timeUnit.toTicks(period);
		return this;
	}

	public boolean isRunning() {
		return task != null && taskId != -1;
	}

	public boolean isPaused() {
		return task != null && taskId == -1;
	}

	public TaskType getTaskType() {
		return taskType;
	}

	public int getPeriod() {
		return period;
	}

	public int getMaximumCount() {
		return maximumCount;
	}

	public int getCount() {
		if (task != null) {
			return task.getCount();
		} else {
			return -1;
		}
	}

	public int getFixedCount() {
		return getCount() / (20 / period);
	}

	public void setCount(int count) {
		if (task != null) {
			task.setCount(count);
		}
	}

	public boolean start() {
		if (!isRunning()) {
			this.task = taskType.newRunnable(this);
			this.taskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(AbilityWar.getPlugin(), task, initialDelay, period);
			onStart();
			return true;
		}
		return false;
	}

	public boolean stop(boolean silent) {
		if (isRunning() || isPaused()) {
			Bukkit.getScheduler().cancelTask(taskId);
			this.task = null;
			this.taskId = -1;
			if (!silent) onEnd();
			else onSilentEnd();
			return true;
		}
		return false;
	}

	public boolean pause() {
		if (isRunning()) {
			Bukkit.getScheduler().cancelTask(taskId);
			this.taskId = -1;
			return true;
		}
		return false;
	}

	public boolean resume() {
		if (isPaused()) {
			this.taskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(AbilityWar.getPlugin(), task, initialDelay, period);
			return true;
		}
		return false;
	}

	protected void onStart() {
	}

	protected abstract void run(int count);

	protected void onEnd() {
	}

	protected void onSilentEnd() {
	}

	public enum TaskType {
		INFINITE {
			Task newRunnable(SimpleTimer simpleTimer) {
				return new Task() {
					int count = 0;

					@Override
					public void run() {
						simpleTimer.run(++count);
					}

					@Override
					public int getCount() {
						return count;
					}

					@Override
					public void setCount(int count) {
						this.count = count;
					}
				};
			}
		},
		NORMAL {
			Task newRunnable(SimpleTimer simpleTimer) {
				return new Task() {
					int count = 0;

					@Override
					public void run() {
						if (count < simpleTimer.maximumCount) simpleTimer.run(++count);
						else simpleTimer.stop(false);
					}

					@Override
					public int getCount() {
						return count;
					}

					@Override
					public void setCount(int count) {
						this.count = count;
					}
				};
			}
		},
		REVERSE {
			Task newRunnable(SimpleTimer simpleTimer) {
				return new Task() {
					int count = simpleTimer.maximumCount + 1;

					@Override
					public void run() {
						if (count > 1) simpleTimer.run(--count);
						else simpleTimer.stop(false);
					}

					@Override
					public int getCount() {
						return count;
					}

					@Override
					public void setCount(int count) {
						this.count = count;
					}
				};
			}
		};

		abstract Task newRunnable(SimpleTimer simpleTimer);
	}

	public interface Task extends Runnable {

		int getCount();

		void setCount(int count);

	}

}
