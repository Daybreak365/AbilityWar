package daybreak.abilitywar.utils.base.logging;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;

public class Logger extends java.util.logging.Logger {

	public static Logger getLogger(final String name) {
		return new Logger(name);
	}

	public static Logger getLogger(final Class<?> clazz) {
		return new Logger(clazz.getName());
	}

	public Logger(final String name) {
		super(name, null);
	}

	public void log(final LogType logType, final String msg) {
		if (logType.isLoggable()) {
			Bukkit.getConsoleSender().sendMessage("[" + getName() + "] " + logType.prefix + ChatColor.RESET + " " + msg);
		}
	}

	public void debug(final String msg) {
		log(LogType.DEBUG, msg);
	}

	public void error(final String msg) {
		log(LogType.ERROR, msg);
	}

}
