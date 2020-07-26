package daybreak.abilitywar.game.team.event

import daybreak.abilitywar.game.AbstractGame.Participant
import daybreak.abilitywar.game.Game
import daybreak.abilitywar.game.team.interfaces.Members
import daybreak.abilitywar.game.team.interfaces.Teamable
import org.bukkit.event.HandlerList

class TeamCreatedEvent (game: Game, teamable: Teamable, team: Members): TeamEvent(game, teamable, team) {
	private companion object Handlers {
		@JvmStatic
		val handlerList = HandlerList()
	}

	override fun getHandlers(): HandlerList {
		return handlerList
	}
}

class TeamRemovedEvent (game: Game, teamable: Teamable, team: Members): TeamEvent(game, teamable, team) {
	private companion object Handlers {
		@JvmStatic
		val handlerList = HandlerList()
	}

	override fun getHandlers(): HandlerList {
		return handlerList
	}
}

class ParticipantTeamChangedEvent (game: Game, teamable: Teamable, val participant: Participant, val oldTeam: Members, newTeam: Members): TeamEvent(game, teamable, newTeam) {
	private companion object Handlers {
		@JvmStatic
		val handlerList = HandlerList()
	}

	override fun getHandlers(): HandlerList {
		return handlerList
	}
}
