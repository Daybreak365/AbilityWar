package daybreak.abilitywar.utils.base.minecraft.nms

import daybreak.abilitywar.utils.base.minecraft.version.ServerVersion
import org.bukkit.entity.Player

class Sounds private constructor() {
	companion object INSTANCE : ISounds {
		private val INSTANCE: ISounds? = try {
			Class.forName("daybreak.abilitywar.utils.base.minecraft.nms." + ServerVersion.name + ".SoundsImpl").asSubclass(ISounds::class.java).getConstructor().newInstance()
		} catch (e: Exception) {
			null
		}

		@JvmStatic
		fun isHandled(): Boolean {
			return INSTANCE != null
		}

		@JvmStatic
		override fun playSound(player: Player, sound: String, x: Double, y: Double, z: Double, volume: Float, pitch: Float) {
			INSTANCE?.playSound(player, sound, x, y, z, volume, pitch)
		}

		@JvmStatic
		override fun playSound(sound: String, x: Double, y: Double, z: Double, volume: Float, pitch: Float) {
			INSTANCE?.playSound(sound, x, y, z, volume, pitch)
		}
	}
}