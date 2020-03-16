package daybreak.abilitywar.utils.base.concurrent;

public enum TimeUnit {

	TICKS {
		public int toTicks(int duration) {
			return duration;
		}

		public int toSeconds(int duration) {
			return duration / 20;
		}

		public int toMinutes(int duration) {
			return duration / 1200;
		}
	},
	SECONDS {
		public int toTicks(int duration) {
			return duration * 20;
		}

		public int toSeconds(int duration) {
			return duration;
		}

		public int toMinutes(int duration) {
			return duration / 60;
		}
	},
	MINUTES {
		public int toTicks(int duration) {
			return duration * 1200;
		}

		public int toSeconds(int duration) {
			return duration * 60;
		}

		public int toMinutes(int duration) {
			return duration;
		}
	};

	public abstract int toTicks(int duration);

	public abstract int toSeconds(int duration);

	public abstract int toMinutes(int duration);

}
