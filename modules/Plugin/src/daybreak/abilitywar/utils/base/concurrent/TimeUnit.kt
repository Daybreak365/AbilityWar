package daybreak.abilitywar.utils.base.concurrent

enum class TimeUnit(private val inTicks: Int) {
	TICKS(1),
	SECONDS(20),
	MINUTES(1200);

	fun toTicks(duration: Int): Int {
		return duration * this.inTicks
	}

}