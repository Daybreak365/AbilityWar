package daybreak.abilitywar.utils.base.logging

import daybreak.abilitywar.config.Configuration.Settings.DeveloperSettings
import org.bukkit.Bukkit
import org.bukkit.ChatColor.DARK_GRAY
import org.bukkit.ChatColor.DARK_RED
import org.bukkit.ChatColor.GRAY
import org.bukkit.ChatColor.RED
import org.bukkit.ChatColor.RESET

class Logger constructor(name: String) : java.util.logging.Logger(name, null) {
	companion object {
		@JvmStatic
		fun getLogger(name: String): Logger {
			return Logger(name)
		}

		@JvmStatic
		fun getLogger(clazz: Class<*>): Logger {
			return Logger(clazz.name)
		}
	}

	fun log(logType: LogType, msg: String) {
		if (logType.isLoggable) {
			Bukkit.getConsoleSender().sendMessage("[" + name + "] " + logType.prefix + RESET + " " + msg)
		}
	}

	fun debug(msg: String) {
		log(LogType.DEBUG, msg)
	}

	fun error(msg: String) {
		log(LogType.ERROR, msg)
	}
}

abstract class LogType private constructor(val prefix: String) {
	abstract val isLoggable: Boolean

	companion object {
		@JvmField
		val DEBUG: LogType = object : LogType(DARK_GRAY.toString() + "[" + GRAY + "디버그" + DARK_GRAY + "]") {
			override val isLoggable: Boolean
				get() = DeveloperSettings.isEnabled()
		}

		@JvmField
		val ERROR: LogType = object : LogType(DARK_RED.toString() + "[" + RED + "오류" + DARK_RED + "]") {
			override val isLoggable: Boolean
				get() = true
		}
	}

}
