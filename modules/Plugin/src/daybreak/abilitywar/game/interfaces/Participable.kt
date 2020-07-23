package daybreak.abilitywar.game.interfaces

import org.bukkit.entity.Player

interface Participable {
	val player: Player
	val game: IGame
}