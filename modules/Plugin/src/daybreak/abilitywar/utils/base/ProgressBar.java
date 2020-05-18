package daybreak.abilitywar.utils.base;

import org.bukkit.ChatColor;

import static com.google.common.base.Strings.repeat;

public class ProgressBar {

	private final int maximum, barCount;
	private int step = 0;

	public ProgressBar(int maximum, int barCount) {
		this.maximum = maximum;
		this.barCount = barCount;
	}

	@Override
	public String toString() {
		int progress = (int) (((double) step / maximum) * barCount);
		return ChatColor.GREEN + repeat("|", progress) + ChatColor.GRAY + repeat("|", barCount - progress);
	}

	public String toString(String character, ChatColor progressed, ChatColor notProgressed) {
		int progress = (int) (((double) step / maximum) * barCount);
		return progressed + repeat(character, progress) + notProgressed + repeat(character, barCount - progress);
	}

	public String toString(String character, String progressed, String notProgressed) {
		int progress = (int) (((double) step / maximum) * barCount);
		return progressed + repeat(character, progress) + notProgressed + repeat(character, barCount - progress);
	}

	public ProgressBar step() {
		return stepBy(1);
	}

	public ProgressBar stepBy(int step) {
		this.step = Math.min(this.step + step, maximum);
		return this;
	}

	public ProgressBar setStep(int step) {
		this.step = Math.min(step, maximum);
		return this;
	}

	public int getProgress() {
		return (int) (((double) step / maximum) * barCount);
	}

	public int getBarCount() {
		return barCount;
	}

	public int getMaximum() {
		return maximum;
	}

	public int getStep() {
		return step;
	}

}
