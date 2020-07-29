package daybreak.abilitywar.ability.event

import daybreak.abilitywar.ability.AbilityBase
import daybreak.abilitywar.ability.AbilityBase.ClickType
import org.bukkit.Material
import org.bukkit.event.Cancellable
import org.bukkit.event.HandlerList

class AbilityActiveSkillEvent(ability: AbilityBase, val material: Material, val clickType: ClickType) : AbilityEvent(ability) {
	private companion object Handlers {
		@JvmStatic
		val handlerList = HandlerList()
	}

	override fun getHandlers(): HandlerList {
		return handlerList
	}
}

class AbilityPreActiveSkillEvent(ability: AbilityBase, val material: Material, val clickType: ClickType) : AbilityEvent(ability), Cancellable {
	private companion object Handlers {
		@JvmStatic
		val handlerList = HandlerList()
	}

	override fun getHandlers(): HandlerList {
		return handlerList
	}

	private var cancelled: Boolean = false

	override fun setCancelled(cancelled: Boolean) {
		this.cancelled = cancelled
	}

	override fun isCancelled(): Boolean {
		return cancelled
	}
}

class AbilityDestroyEvent(ability: AbilityBase) : AbilityEvent(ability) {
	private companion object Handlers {
		@JvmStatic
		val handlerList = HandlerList()
	}

	override fun getHandlers(): HandlerList {
		return handlerList
	}
}

class AbilityRestrictionEvent(ability: AbilityBase, val newState: Boolean) : AbilityEvent(ability) {
	private companion object Handlers {
		@JvmStatic
		val handlerList = HandlerList()
	}

	override fun getHandlers(): HandlerList {
		return handlerList
	}
}

class AbilityPreRestrictionEvent(ability: AbilityBase, var newState: Boolean) : AbilityEvent(ability) {
	private companion object Handlers {
		@JvmStatic
		val handlerList = HandlerList()
	}

	override fun getHandlers(): HandlerList {
		return handlerList
	}
}
