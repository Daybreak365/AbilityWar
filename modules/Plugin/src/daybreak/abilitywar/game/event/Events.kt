package daybreak.abilitywar.game.event

import daybreak.abilitywar.game.Game
import org.bukkit.event.HandlerList
import java.util.ArrayList
import java.util.Collections

class GameCreditEvent(game: Game) : GameEvent(game) {
	private companion object Handlers {
		@JvmStatic
		val handlerList = HandlerList()
	}

	override fun getHandlers(): HandlerList {
		return handlerList
	}

	private val credits: MutableList<String> = ArrayList()

	fun getCredits(): List<String> {
		return Collections.unmodifiableList(credits)
	}

	fun addCredit(vararg strings: String) {
		credits.addAll(listOf(*strings))
	}
}

class GameStartEvent(game: Game) : GameEvent(game) {
	private companion object Handlers {
		@JvmStatic
		val handlerList = HandlerList()
	}

	override fun getHandlers(): HandlerList {
		return handlerList
	}
}

class GameEndEvent(game: Game) : GameEvent(game) {
	private companion object Handlers {
		@JvmStatic
		val handlerList = HandlerList()
	}

	override fun getHandlers(): HandlerList {
		return handlerList
	}
}

class GameReadyEvent(game: Game) : GameEvent(game) {
	private companion object Handlers {
		@JvmStatic
		val handlerList = HandlerList()
	}

	override fun getHandlers(): HandlerList {
		return handlerList
	}
}

class InvincibilityStatusChangeEvent(game: Game, val newStatus: Boolean) : GameEvent(game) {
	private companion object Handlers {
		@JvmStatic
		val handlerList = HandlerList()
	}

	override fun getHandlers(): HandlerList {
		return handlerList
	}
}
