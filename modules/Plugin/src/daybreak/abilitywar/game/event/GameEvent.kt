package daybreak.abilitywar.game.event

import daybreak.abilitywar.game.Game
import org.bukkit.event.Event

abstract class GameEvent protected constructor(open val game: Game) : Event()