package daybreak.abilitywar.utils.base.minecraft.server

import com.google.common.base.Enums
import org.bukkit.Bukkit

enum class ServerType {

	UNKNOWN,
	CRAFTBUKKIT,
	SPIGOT,
	PAPER;

	companion object {

		private val INSTANCE: ServerType = with(Bukkit.getVersion().split("-")) {
			if (this.size >= 2) {
				val name = this[1].toUpperCase()
				Enums.getIfPresent(ServerType::class.java, name).or(if (name == "BUKKIT") CRAFTBUKKIT else UNKNOWN)
			} else UNKNOWN
		}

		@JvmStatic
		fun getServerType(): ServerType {
			return INSTANCE
		}

		@JvmStatic
		fun `is`(other: ServerType): Boolean {
			return INSTANCE == other
		}

	}

}