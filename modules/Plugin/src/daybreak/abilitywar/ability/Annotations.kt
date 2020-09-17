package daybreak.abilitywar.ability

import daybreak.abilitywar.game.AbstractGame
import daybreak.abilitywar.utils.library.MaterialX
import daybreak.abilitywar.utils.library.MaterialX.IRON_BLOCK
import org.bukkit.ChatColor
import org.bukkit.Material
import java.lang.annotation.Inherited
import kotlin.annotation.AnnotationRetention.RUNTIME
import kotlin.annotation.AnnotationTarget.CLASS
import kotlin.annotation.AnnotationTarget.FUNCTION
import kotlin.reflect.KClass

@Retention(RUNTIME)
@Target(CLASS)
@Inherited
annotation class AbilityManifest(val name: String, val rank: Rank, val species: Species, val explain: Array<String> = []) {
	enum class Rank(val rankName: String) {
		SPECIAL("§cSPECIAL 등급"),
		S("§dS 등급"),
		A("§aA 등급"),
		B("§bB 등급"),
		C("§eC 등급");
	}

	enum class Species(val speciesName: String) {
		SPECIAL("§e특별 능력"),
		HUMAN("§f인간"),
		GOD("§c신"),
		DEMIGOD("§c반신§7반인"),
		ANIMAL("§2동물"),
		UNDEAD("§c언데드"),
		OTHERS("§8기타");
	}
}

@Retention(RUNTIME)
@Target(CLASS)
@Inherited
annotation class Tips(val difficulty: Difficulty, val stats: Stats, val tip: Array<String> = [], val strong: Array<Description> = [], val weak: Array<Description> = []) {
	annotation class Description(val subject: String, val explain: Array<String>, val icon: MaterialX = IRON_BLOCK)
	annotation class Stats(val offense: Level, val survival: Level, val crowdControl: Level, val mobility: Level, val utility: Level)
	enum class Level(val display: String) {
		ZERO("${ChatColor.DARK_GRAY}□□□□□□□□□□"),
		ONE("${ChatColor.DARK_RED}■${ChatColor.DARK_GRAY}□□□□□□□□□"),
		TWO("${ChatColor.DARK_RED}■■${ChatColor.DARK_GRAY}□□□□□□□□"),
		THREE("${ChatColor.RED}■■■${ChatColor.DARK_GRAY}□□□□□□□"),
		FOUR("${ChatColor.RED}■■■■${ChatColor.DARK_GRAY}□□□□□□"),
		FIVE("${ChatColor.YELLOW}■■■■■${ChatColor.DARK_GRAY}□□□□□"),
		SIX("${ChatColor.YELLOW}■■■■■■${ChatColor.DARK_GRAY}□□□□"),
		SEVEN("${ChatColor.GREEN}■■■■■■■${ChatColor.DARK_GRAY}□□□"),
		EIGHT("${ChatColor.GREEN}■■■■■■■■${ChatColor.DARK_GRAY}□□"),
		NINE("${ChatColor.DARK_GREEN}■■■■■■■■■${ChatColor.DARK_GRAY}□"),
		TEN("${ChatColor.DARK_GREEN}■■■■■■■■■■")
	}
	enum class Difficulty(val display: String) {
		VERY_EASY("${ChatColor.DARK_GREEN}■${ChatColor.DARK_GRAY}□□□□"),
		EASY("${ChatColor.GREEN}■■${ChatColor.DARK_GRAY}□□□"),
		NORMAL("${ChatColor.YELLOW}■■■${ChatColor.DARK_GRAY}□□"),
		HARD("${ChatColor.RED}■■■■${ChatColor.DARK_GRAY}□"),
		VERY_HARD("${ChatColor.DARK_RED}■■■■■")
	}
}

@Retention(RUNTIME)
@Target(CLASS)
annotation class Materials(val materials: Array<Material>)

@Retention(RUNTIME)
@Target(FUNCTION)
annotation class SubscribeEvent(val onlyRelevant: Boolean = false, val ignoreCancelled: Boolean = false, val priority: Int = Priority.NORMAL) {
	object Priority {
		const val LOWEST = 1
		const val LOW = 2
		const val NORMAL = 3
		const val HIGH = 4
		const val HIGHEST = 5
	}
}

@Retention(RUNTIME)
@Target(CLASS)
annotation class NotAvailable(val value: Array<KClass<out AbstractGame>>)