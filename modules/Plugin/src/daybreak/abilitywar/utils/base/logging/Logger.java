package daybreak.abilitywar.utils.base.logging;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;

public class Logger extends java.util.logging.Logger {

	public static Logger getLogger(String name) {
		return new Logger(name);
	}

	public static Logger getLogger(Class<?> clazz) {
		return new Logger(clazz.getName());
	}

	Logger(String name) {
		super(name, null);
	}

	public void log(LogType logType, String msg) {
		if (logType.isLoggable()) {
			Bukkit.getConsoleSender().sendMessage("[" + getName() + "] " + logType.prefix + ChatColor.RESET + " " + msg);
		}
	}

	public void debug(String msg) {
		log(LogType.DEBUG, msg);
	}

	public void error(String msg) {
		log(LogType.ERROR, msg);
	}

}
