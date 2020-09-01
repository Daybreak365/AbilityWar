package daybreak.abilitywar.utils.base.minecraft.entity.health

import daybreak.abilitywar.utils.base.minecraft.entity.health.event.PlayerSetHealthEvent
import org.bukkit.Bukkit
import org.bukkit.attribute.Attribute.GENERIC_MAX_HEALTH
import org.bukkit.entity.Player
import kotlin.math.max
import kotlin.math.min

object Healths {

	@JvmStatic
	fun setHealth(player: Player, health: Double): Double {
		val event = PlayerSetHealthEvent(player, max(min(health, player.getAttribute(GENERIC_MAX_HEALTH)!!.value), 0.0))
		Bukkit.getPluginManager().callEvent(event)
		if (!event.isCancelled) {
			player.health = min(event.health, player.getAttribute(GENERIC_MAX_HEALTH)!!.value)
		}
		return player.health
	}

}
