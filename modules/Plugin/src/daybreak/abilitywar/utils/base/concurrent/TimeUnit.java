package daybreak.abilitywar.utils.base.concurrent;

public enum TimeUnit {
	TICKS(1),
	SECONDS(20),
	MINUTES(1200);

	private final int inTicks;

	TimeUnit(final int inTicks) {
		this.inTicks = inTicks;
	}

	public int toTicks(final int duration) {
		return duration * this.inTicks;
	}

}
