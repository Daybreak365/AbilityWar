package daybreak.abilitywar.utils.base.concurrent

import com.google.common.base.Preconditions
import daybreak.abilitywar.AbilityWar
import daybreak.abilitywar.utils.base.collect.QueueOnIterateHashSet
import org.bukkit.Bukkit

abstract class SimpleTimer(val taskType: TaskType, val maximumCount: Int) {
	private lateinit var observers: QueueOnIterateHashSet<Observer>

	fun attachObserver(observer: Observer) {
		if (!this::observers.isInitialized) {
			this.observers = QueueOnIterateHashSet()
		}
		observers.add(observer)
	}

	fun detachObserver(observer: Observer) {
		if (this::observers.isInitialized) {
			observers.remove(observer)
		}
	}

	private var task: Task? = null
	private var taskId = -1
	private var initialDelay = 0
	var period = 20
		private set

	open fun setInitialDelay(timeUnit: TimeUnit, initialDelay: Int): SimpleTimer {
		this.initialDelay = Preconditions.checkNotNull(timeUnit).toTicks(initialDelay)
		return this
	}

	open fun setPeriod(timeUnit: TimeUnit, period: Int): SimpleTimer {
		this.period = Preconditions.checkNotNull(timeUnit).toTicks(period)
		return this
	}

	fun isRunning(): Boolean {
		return task != null && taskId != -1
	}

	fun isPaused(): Boolean {
		return task != null && taskId == -1
	}

	var count: Int
		get() = if (task != null) {
			task!!.count
		} else {
			-1
		}
		set(count) {
			if (task != null) {
				task!!.count = count
				onCountSet()
			}
		}

	val fixedCount: Int
		get() = count / (20 / period)

	open fun start(): Boolean {
		if (!isRunning()) {
			task = taskType.newRunnable(this)
			taskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(AbilityWar.getPlugin(), task!!, initialDelay.toLong(), period.toLong())
			if (this::observers.isInitialized) {
				for (observer in observers) {
					observer.onStart()
				}
			}
			onStart()
			return true
		}
		return false
	}

	open fun stop(silent: Boolean): Boolean {
		if (isRunning() || isPaused()) {
			Bukkit.getScheduler().cancelTask(taskId)
			task = null
			taskId = -1
			if (!silent) {
				if (this::observers.isInitialized) {
					for (observer in observers) {
						observer.onEnd()
					}
				}
				onEnd()
			} else {
				if (this::observers.isInitialized) {
					for (observer in observers) {
						observer.onSilentEnd()
					}
				}
				onSilentEnd()
			}
			return true
		}
		return false
	}

	open fun pause(): Boolean {
		if (isRunning()) {
			Bukkit.getScheduler().cancelTask(taskId)
			taskId = -1
			if (this::observers.isInitialized) {
				for (observer in observers) {
					observer.onPause()
				}
			}
			onPause()
			return true
		}
		return false
	}

	open fun resume(): Boolean {
		if (isPaused()) {
			taskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(AbilityWar.getPlugin(), task!!, 0, period.toLong())
			if (this::observers.isInitialized) {
				for (observer in observers) {
					observer.onResume()
				}
			}
			onResume()
			return true
		}
		return false
	}

	protected open fun onStart() {}
	protected abstract fun run(count: Int)
	protected open fun onEnd() {}
	protected open fun onSilentEnd() {}
	protected open fun onPause() {}
	protected open fun onResume() {}
	protected open fun onCountSet() {}

	enum class TaskType {
		INFINITE {
			override fun newRunnable(simpleTimer: SimpleTimer): Task {
				return object : Task {
					override var count = 0
					override fun run() {
						simpleTimer.run(++count)
					}

				}
			}
		},
		NORMAL {
			override fun newRunnable(simpleTimer: SimpleTimer): Task {
				return object : Task {
					override var count = 0
					override fun run() {
						if (count < simpleTimer.maximumCount) simpleTimer.run(++count) else simpleTimer.stop(false)
					}

				}
			}
		},
		REVERSE {
			override fun newRunnable(simpleTimer: SimpleTimer): Task {
				return object : Task {
					override var count = simpleTimer.maximumCount + 1
					override fun run() {
						if (count > 1) simpleTimer.run(--count) else simpleTimer.stop(false)
					}

				}
			}
		};

		abstract fun newRunnable(simpleTimer: SimpleTimer): Task
	}

	interface Task : Runnable {
		var count: Int
	}

	interface Observer {

		fun onStart()
		fun onEnd()
		fun onSilentEnd()
		fun onPause()
		fun onResume()

	}

}