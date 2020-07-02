package daybreak.abilitywar.utils.base.concurrent

import com.google.common.base.Preconditions
import daybreak.abilitywar.AbilityWar
import org.bukkit.Bukkit

abstract class SimpleTimer(val taskType: TaskType, val maximumCount: Int) {
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

	val isRunning: Boolean
		get() = task != null && taskId != -1

	val isPaused: Boolean
		get() = task != null && taskId == -1

	var count: Int
		get() = if (task != null) {
			task!!.count
		} else {
			-1
		}
		set(count) {
			if (task != null) {
				task!!.count = count
			}
		}

	val fixedCount: Int
		get() = count / (20 / period)

	open fun start(): Boolean {
		if (!isRunning) {
			task = taskType.newRunnable(this)
			taskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(AbilityWar.getPlugin(), task!!, initialDelay.toLong(), period.toLong())
			onStart()
			return true
		}
		return false
	}

	open fun stop(silent: Boolean): Boolean {
		if (isRunning || isPaused) {
			Bukkit.getScheduler().cancelTask(taskId)
			task = null
			taskId = -1
			if (!silent) onEnd() else onSilentEnd()
			return true
		}
		return false
	}

	open fun pause(): Boolean {
		if (isRunning) {
			Bukkit.getScheduler().cancelTask(taskId)
			taskId = -1
			return true
		}
		return false
	}

	open fun resume(): Boolean {
		if (isPaused) {
			taskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(AbilityWar.getPlugin(), task!!, initialDelay.toLong(), period.toLong())
			return true
		}
		return false
	}

	protected open fun onStart() {}
	protected abstract fun run(count: Int)
	protected open fun onEnd() {}
	protected open fun onSilentEnd() {}

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

}