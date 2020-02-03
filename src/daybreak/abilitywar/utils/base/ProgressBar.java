package daybreak.abilitywar.utils.base;

import org.bukkit.ChatColor;

import static org.apache.commons.lang.StringUtils.repeat;

public class ProgressBar {

	private final String character;
	private final int maximum;
	private final int barCount;
	private int step = 0;

	public ProgressBar(String character, int maximum, int barCount) {
		this.character = character;
		this.maximum = maximum;
		this.barCount = barCount;
	}

	public ProgressBar(int maximum, int barCount) {
		this("|", maximum, barCount);
	}

	@Override
	public String toString() {
		int progress = (int) (((double) step / maximum) * barCount);
		return ChatColor.GREEN + repeat(character, progress) + ChatColor.GRAY + repeat(character, barCount - progress);
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

}
