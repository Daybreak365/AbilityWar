package daybreak.abilitywar.utils.base.concurrent;

public enum TimeUnit {

	TICKS {
		public long toMillis(long duration) {
			return duration * 50;
		}

		public long toTicks(long duration) {
			return duration;
		}

		public long toSeconds(long duration) {
			return duration / 20;
		}

		public long toMinutes(long duration) {
			return duration / 1200;
		}
	},
	SECONDS {
		public long toMillis(long duration) {
			return duration * 1000;
		}

		public long toTicks(long duration) {
			return duration * 20;
		}

		public long toSeconds(long duration) {
			return duration;
		}

		public long toMinutes(long duration) {
			return duration / 60;
		}
	},
	MINUTES {
		public long toMillis(long duration) {
			return duration * 60000;
		}

		public long toTicks(long duration) {
			return duration * 1200;
		}

		public long toSeconds(long duration) {
			return duration * 60;
		}

		public long toMinutes(long duration) {
			return duration;
		}
	};

	public abstract long toMillis(long duration);

	public abstract long toTicks(long duration);

	public abstract long toSeconds(long duration);

	public abstract long toMinutes(long duration);

}
