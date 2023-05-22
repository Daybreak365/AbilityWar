package daybreak.abilitywar.utils.base.concurrent;

import daybreak.abilitywar.AbilityWar;
import daybreak.abilitywar.utils.base.collect.QueueOnIterateHashSet;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;

public class SimpleTimer {

	private final TaskType taskType;
	private QueueOnIterateHashSet<Observer> observers = null;

	private Task task = null;
	private BukkitTask bukkitTask = null;
	private int maximumCount;
	private int initialDelay = 0;
	private int period = 20;

	public SimpleTimer(final @NotNull TaskType taskType, final int maximumCount) {
		this.taskType = taskType;
		this.maximumCount = taskType == TaskType.INFINITE ? -1 : maximumCount;
	}

	public void attachObserver(final Observer observer) {
		if (observers == null) {
			this.observers = new QueueOnIterateHashSet<>();
		}
		observers.add(observer);
	}

	public void detachObserver(final Observer observer) {
		if (observers != null) {
			observers.remove(observer);
		}
	}

	public SimpleTimer setInitialDelay(final @NotNull TimeUnit timeUnit, final int initialDelay) {
		this.initialDelay = timeUnit.toTicks(initialDelay);
		return this;
	}

	public SimpleTimer setPeriod(final @NotNull TimeUnit timeUnit, final int period) {
		this.period = timeUnit.toTicks(period);
		return this;
	}

	public boolean isRunning() {
		return task != null && bukkitTask != null;
	}

	public boolean isPaused() {
		return task != null && bukkitTask == null;
	}

	public TaskType getTaskType() {
		return taskType;
	}

	public int getCount() {
		return task != null ? task.getCount() : -1;
	}

	public void setCount(final int count) {
		if (task != null) {
			task.setCount(count);
			onCountSet();
		}
	}

	public int getFixedCount() {
		return getCount() / (20 / period);
	}

	public int getMaximumCount() {
		return taskType == TaskType.INFINITE ? -1 : maximumCount;
	}

	public void setMaximumCount(int maximumCount) {
		if (taskType == TaskType.INFINITE) return;
		this.maximumCount = maximumCount;
	}

	public int getInitialDelay() {
		return initialDelay;
	}

	public int getPeriod() {
		return period;
	}

	public boolean start() {
		if (!isRunning()) {
			this.task = taskType.newRunnable(this, maximumCount);
			this.bukkitTask = Bukkit.getScheduler().runTaskTimer(AbilityWar.getPlugin(), task, initialDelay, period);
			if (observers != null) {
				for (final Observer observer : observers) {
					observer.onStart();
				}
			}
			onStart();
			return true;
		}
		return false;
	}

	public boolean stop(final boolean silent) {
		if (isRunning() || isPaused()) {
			this.task = null;
			if (bukkitTask != null) {
				bukkitTask.cancel();
				this.bukkitTask = null;
			}
			if (silent) {
				if (observers != null) {
					for (final Observer observer : observers) {
						observer.onSilentEnd();
					}
				}
				onSilentEnd();
			} else {
				if (observers != null) {
					for (final Observer observer : observers) {
						observer.onEnd();
					}
				}
				onEnd();
			}
			return true;
		}
		return false;
	}

	public boolean pause() {
		if (isRunning()) {
			bukkitTask.cancel();
			this.bukkitTask = null;
			if (observers != null) {
				for (final Observer observer : observers) {
					observer.onPause();
				}
			}
			onPause();
			return true;
		}
		return false;
	}

	public boolean resume() {
		if (isPaused()) {
			this.bukkitTask = Bukkit.getScheduler().runTaskTimer(AbilityWar.getPlugin(), task, 0, period);
			if (observers != null) {
				for (final Observer observer : observers) {
					observer.onResume();
				}
			}
			onResume();
			return true;
		}
		return false;
	}

	protected void onStart() {}
	protected void run(final int count) {}
	protected void onEnd() {}
	protected void onSilentEnd() {}
	protected void onPause() {}
	protected void onResume() {}
	protected void onCountSet() {}

	private void run0(final int count) {
		if (observers != null) {
			for (final Observer observer : observers) {
				observer.run(count);
			}
		}
		run(count);
	}

	public enum TaskType {
		INFINITE {
			@Override
			Task newRunnable(final SimpleTimer simpleTimer, final int maximumCount) {
				return new Task() {
					private int count = 0;

					@Override
					public int getCount() {
						return count;
					}

					@Override
					public void setCount(int count) {
						this.count = count;
					}

					@Override
					public void run() {
						simpleTimer.run0(++count);
					}
				};
			}
		},
		NORMAL {
			@Override
			Task newRunnable(final SimpleTimer simpleTimer, final int maximumCount) {
				return new Task() {
					private int count = 0;

					@Override
					public int getCount() {
						return count;
					}

					@Override
					public void setCount(int count) {
						this.count = count;
					}

					@Override
					public void run() {
						if (count < maximumCount) simpleTimer.run0(++count); else simpleTimer.stop(false);
					}
				};
			}
		},
		REVERSE {
			@Override
			Task newRunnable(final SimpleTimer simpleTimer, final int maximumCount) {
				return new Task() {
					private int count = maximumCount + 1;

					@Override
					public int getCount() {
						return count;
					}

					@Override
					public void setCount(int count) {
						this.count = count;
					}

					@Override
					public void run() {
						if (count > 1) simpleTimer.run0(--count); else simpleTimer.stop(false);
					}
				};
			}
		};

		abstract Task newRunnable(final SimpleTimer simpleTimer, final int maximumCount);
	}

	private interface Task extends Runnable {
		int getCount();
		void setCount(final int count);
	}

	public interface Observer {

		default void onStart() {}
		default void onEnd() {}
		default void onSilentEnd() {}
		default void onPause() {}
		default void onResume() {}

		default void run(final int count) {}

	}

}
