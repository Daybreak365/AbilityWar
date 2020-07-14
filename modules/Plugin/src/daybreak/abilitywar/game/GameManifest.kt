package daybreak.abilitywar.game

import daybreak.abilitywar.utils.library.MaterialX
import org.bukkit.Material
import kotlin.annotation.AnnotationRetention.RUNTIME
import kotlin.annotation.AnnotationTarget.CLASS

@Retention(RUNTIME)
@Target(CLASS)
annotation class GameManifest(val name: String, val description: Array<String>)

@Retention(RUNTIME)
@Target(CLASS)
annotation class Category(val value: GameCategory) {
	enum class GameCategory(val icon: Material, val displayName: String) {
		GAME(Material.DIAMOND_SWORD, "게임"),
		MINIGAME(Material.FEATHER, "미니 게임"),
		DEBUG(MaterialX.COMMAND_BLOCK.parseMaterial(), "디버그");
	}
}

annotation class GameAliases(vararg val value: String)
