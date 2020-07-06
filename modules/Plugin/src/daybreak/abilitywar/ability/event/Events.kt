package daybreak.abilitywar.ability.event

import daybreak.abilitywar.ability.AbilityBase
import daybreak.abilitywar.ability.AbilityBase.ClickType
import org.bukkit.Material
import org.bukkit.event.HandlerList

class AbilityActiveSkillEvent(ability: AbilityBase, val materialType: Material, val clickType: ClickType) : AbilityEvent(ability) {
	private companion object Handlers {
		@JvmStatic
		val handlerList = HandlerList()
	}

	override fun getHandlers(): HandlerList {
		return handlerList
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

class AbilityRestrictionEvent(ability: AbilityBase, val newStatus: Boolean) : AbilityEvent(ability) {
	private companion object Handlers {
		@JvmStatic
		val handlerList = HandlerList()
	}

	override fun getHandlers(): HandlerList {
		return handlerList
	}
}

class AbilityPreRestrictionEvent(ability: AbilityBase, var newStatus: Boolean) : AbilityEvent(ability) {
	private companion object Handlers {
		@JvmStatic
		val handlerList = HandlerList()
	}

	override fun getHandlers(): HandlerList {
		return handlerList
	}
}
