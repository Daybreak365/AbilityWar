package daybreak.abilitywar.utils.base.concurrent;

import com.google.common.base.Preconditions;
import daybreak.abilitywar.AbilityWar;
import daybreak.abilitywar.utils.annotations.Beta;
import org.bukkit.Bukkit;

@Beta
public abstract class SimpleTimer {

	private final TaskType taskType;
	private final int maximum;
	private Runnable runnable = null;
	private int taskId = -1;

	public SimpleTimer(TaskType taskType, int maximum) {
		this.taskType = taskType;
		this.maximum = maximum;
	}

	private long delay = 0L;
	private long period = 20L;

	public SimpleTimer setDelay(TimeUnit timeUnit, long delay) {
		Preconditions.checkNotNull(timeUnit);
		this.delay = timeUnit.toTicks(delay);
		return this;
	}

	public SimpleTimer setPeriod(TimeUnit timeUnit, long period) {
		Preconditions.checkNotNull(timeUnit);
		this.period = timeUnit.toTicks(period);
		return this;
	}

	public boolean isRunning() {
		return runnable != null && taskId != -1;
	}

	public boolean isPaused() {
		return runnable != null && taskId == -1;
	}

	public boolean start() {
		if (!isRunning()) {
			this.runnable = taskType.newRunnable(this);
			this.taskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(AbilityWar.getPlugin(), runnable, 0, period);
			onStart();
			return true;
		}
		return false;
	}

	public boolean stop(boolean silent) {
		if (isRunning()) {
			Bukkit.getScheduler().cancelTask(taskId);
			this.runnable = null;
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
			this.taskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(AbilityWar.getPlugin(), runnable, 0, period);
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
			Runnable newRunnable(SimpleTimer simpleTimer) {
				return new Runnable() {
					int count = 0;

					@Override
					public void run() {
						simpleTimer.run(++count);
					}
				};
			}
		},
		NORMAL {
			Runnable newRunnable(SimpleTimer simpleTimer) {
				return new Runnable() {
					int count = 0;

					@Override
					public void run() {
						if (count <= simpleTimer.maximum) simpleTimer.run(++count);
						else simpleTimer.stop(false);
					}
				};
			}
		},
		REVERSE {
			Runnable newRunnable(SimpleTimer simpleTimer) {
				return new Runnable() {
					int count = simpleTimer.maximum;

					@Override
					public void run() {
						if (count > 0) simpleTimer.run(count--);
						else simpleTimer.stop(false);
					}
				};
			}
		};

		abstract Runnable newRunnable(SimpleTimer simpleTimer);
	}

}
