package daybreak.abilitywar.utils.base.logging;

import daybreak.abilitywar.config.Configuration.Settings.DeveloperSettings;
import org.bukkit.ChatColor;

public abstract class LogType {

	public static final LogType DEBUG = new LogType(ChatColor.DARK_GRAY + "[" + ChatColor.GRAY + "디버그" + ChatColor.DARK_GRAY + "]") {
		@Override
		boolean isLoggable() {
			return DeveloperSettings.isEnabled();
		}
	};

	public static final LogType ERROR = new LogType(ChatColor.DARK_RED + "[" + ChatColor.RED + "오류" + ChatColor.DARK_RED + "]") {
		@Override
		boolean isLoggable() {
			return true;
		}
	};

	final String prefix;

	private LogType(String prefix) {
		this.prefix = prefix;
	}

	abstract boolean isLoggable();

}
