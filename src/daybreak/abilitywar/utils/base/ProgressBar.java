package daybreak.abilitywar.utils.base;

import org.bukkit.ChatColor;

import static org.apache.commons.lang.StringUtils.repeat;

public class ProgressBar {

	private final String character;
	private final int maximum;
	private final int maxBar;
	private int current = 0;

	public ProgressBar(String character, int maximum, int maxBar) {
		this.character = character;
		this.maximum = maximum;
		this.maxBar = maxBar;
	}

	public ProgressBar(int maximum, int maxBar) {
		this("|", maximum, maxBar);
	}

	public String getProgress() {
		int progress = (int) (((double) current / maximum) * maxBar);
		return ChatColor.GREEN + repeat(character, progress) + ChatColor.GRAY + repeat(character, maxBar - progress);
	}

	public ProgressBar step() {
		return stepBy(1);
	}

	public ProgressBar stepBy(int step) {
		this.current = Math.min(current + step, maximum);
		return this;
	}

}
