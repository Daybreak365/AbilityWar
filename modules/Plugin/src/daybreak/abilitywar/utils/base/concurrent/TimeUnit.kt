package daybreak.abilitywar.utils.base.concurrent

enum class TimeUnit(inTicks: Int) {
	TICKS(1),
	SECONDS(20),
	MINUTES(1200);

	private val inTicks: Double = inTicks.toDouble()

	fun to(timeUnit: TimeUnit, duration: Int): Double {
		return duration / (timeUnit.inTicks / this.inTicks)
	}

	fun toTicks(duration: Int): Int {
		return to(TICKS, duration).toInt()
	}

	fun toSeconds(duration: Int): Double {
		return to(SECONDS, duration)
	}

	fun toMinutes(duration: Int): Double {
		return to(MINUTES, duration)
	}
}