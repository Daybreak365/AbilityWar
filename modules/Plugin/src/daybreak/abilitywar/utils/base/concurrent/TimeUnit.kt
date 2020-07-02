package daybreak.abilitywar.utils.base.concurrent

enum class TimeUnit {
	TICKS {
		override fun toTicks(duration: Int): Int {
			return duration
		}

		override fun toSeconds(duration: Int): Int {
			return duration / 20
		}

		override fun toMinutes(duration: Int): Int {
			return duration / 1200
		}
	},
	SECONDS {
		override fun toTicks(duration: Int): Int {
			return duration * 20
		}

		override fun toSeconds(duration: Int): Int {
			return duration
		}

		override fun toMinutes(duration: Int): Int {
			return duration / 60
		}
	},
	MINUTES {
		override fun toTicks(duration: Int): Int {
			return duration * 1200
		}

		override fun toSeconds(duration: Int): Int {
			return duration * 60
		}

		override fun toMinutes(duration: Int): Int {
			return duration
		}
	};

	abstract fun toTicks(duration: Int): Int
	abstract fun toSeconds(duration: Int): Int
	abstract fun toMinutes(duration: Int): Int
}