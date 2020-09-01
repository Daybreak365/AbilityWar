package daybreak.abilitywar.utils.base;

import java.text.NumberFormat;
import java.util.LinkedList;
import java.util.List;

public class Stopwatch {

	private final String identifier;
	private final List<Record> records = new LinkedList<>();
	private long totalNanos = 0L;

	public Stopwatch(String identifier) {
		this.identifier = identifier;
	}

	public Stopwatch() {
		this("");
	}

	private long startTimeNanos = 0L;
	private String currentTaskName = null;

	public void start(String taskName) throws IllegalStateException {
		if (currentTaskName != null) {
			throw new IllegalStateException("Unable to start while the stopwatch is running.");
		}
		this.currentTaskName = taskName;
		System.nanoTime();
		this.startTimeNanos = System.nanoTime();
	}

	public void stop() throws IllegalStateException {
		if (currentTaskName == null) {
			throw new IllegalStateException("Unable to stop while the stopwatch is not running.");
		}
		final long currentNanos = System.nanoTime();
		Record record = new Record(currentTaskName, currentNanos, startTimeNanos);
		records.add(record);
		this.currentTaskName = null;
		totalNanos += record.getTimeNanos();

	}

	public boolean isRunning() {
		return currentTaskName != null;
	}

	public long getTotalNanos() {
		return totalNanos;
	}

	public Record[] getRecords() {
		return records.toArray(new Record[0]);
	}

	@Override
	public String toString() {
		final StringBuilder builder = new StringBuilder();
		final long totalNanos = getTotalNanos();
		builder.append("\nStopwatch '").append(identifier).append("': Running Time = ").append(totalNanos).append(" ns (").append(totalNanos / 1000000000.0).append(" sec)");
		builder.append("\n--------------------------------------------------\n");
		builder.append("nanoseconds    %        Name\n");
		builder.append("--------------------------------------------------\n");
		final NumberFormat ns = NumberFormat.getNumberInstance();
		ns.setMinimumIntegerDigits(10);
		ns.setGroupingUsed(false);
		final NumberFormat percentage = NumberFormat.getNumberInstance();
		percentage.setMinimumIntegerDigits(3);
		percentage.setGroupingUsed(false);
		for (final Record record : records) {
			builder.append(ns.format(record.getTimeNanos()))
					.append("     ")
					.append(percentage.format(Math.round((double) record.timeNanos / totalNanos * 100.0)))
					.append("%     ")
					.append(record.taskName)
					.append("\n");
		}
		builder.append("--------------------------------------------------\n");
		return builder.toString();
	}

	public static final class Record {

		private final String taskName;
		private final long timeNanos;

		private Record(final String taskName, final long currentNanos, final long startTimeNanos) {
			this.taskName = taskName;
			this.timeNanos = currentNanos - startTimeNanos;
		}

		public String getTaskName() {
			return taskName;
		}

		public long getTimeNanos() {
			return timeNanos;
		}

	}

}
