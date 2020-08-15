package daybreak.abilitywar.utils.base.minecraft.health.event

import daybreak.abilitywar.utils.base.minecraft.health.Healths
import org.bukkit.entity.Player
import org.bukkit.event.Cancellable
import org.bukkit.event.HandlerList
import org.bukkit.event.player.PlayerEvent

/**
 * [Healths.setHealth]로 체력을 설정할 경우 호출되는 이벤트입니다.
 */
class PlayerSetHealthEvent(player: Player, val health: Double): PlayerEvent(player), Cancellable {
	private companion object Handlers {
		@JvmStatic
		val handlerList = HandlerList()
	}

	override fun getHandlers(): HandlerList {
		return handlerList
	}

	private var cancelled = false

	override fun setCancelled(cancelled: Boolean) {
		this.cancelled = cancelled
	}

	override fun isCancelled(): Boolean = cancelled

}
