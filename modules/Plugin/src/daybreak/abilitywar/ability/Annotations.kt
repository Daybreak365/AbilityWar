package daybreak.abilitywar.ability

import org.bukkit.Material
import kotlin.annotation.AnnotationRetention.RUNTIME
import kotlin.annotation.AnnotationTarget.ANNOTATION_CLASS
import kotlin.annotation.AnnotationTarget.CLASS
import kotlin.annotation.AnnotationTarget.FIELD
import kotlin.annotation.AnnotationTarget.FUNCTION

@Retention(RUNTIME)
@Target(ANNOTATION_CLASS, CLASS)
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
@Target(FIELD)
annotation class Scheduled

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