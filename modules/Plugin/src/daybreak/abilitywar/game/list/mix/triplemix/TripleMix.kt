package daybreak.abilitywar.game.list.mix.triplemix

import daybreak.abilitywar.ability.AbilityBase
import daybreak.abilitywar.ability.AbilityBase.Update.ABILITY_DESTROY
import daybreak.abilitywar.ability.AbilityBase.Update.RESTRICTION_CLEAR
import daybreak.abilitywar.ability.AbilityBase.Update.RESTRICTION_SET
import daybreak.abilitywar.ability.AbilityManifest
import daybreak.abilitywar.ability.AbilityManifest.Rank.SPECIAL
import daybreak.abilitywar.ability.AbilityManifest.Species.OTHERS
import daybreak.abilitywar.ability.decorator.ActiveHandler
import daybreak.abilitywar.ability.decorator.TargetHandler
import daybreak.abilitywar.game.AbstractGame.Participant
import org.bukkit.ChatColor.RESET
import org.bukkit.Material
import org.bukkit.entity.LivingEntity
import java.lang.reflect.InvocationTargetException
import java.util.StringJoiner

@AbilityManifest(name = "트리플 믹스", rank = SPECIAL, species = OTHERS, explain = ["$(EXPLAIN)"])
class TripleMix(participant: Participant) : AbilityBase(participant), ActiveHandler, TargetHandler {
	var first: AbilityBase? = null
		private set
	var second: AbilityBase? = null
		private set
	var third: AbilityBase? = null
		private set
	private val EXPLAIN: Any = object : Any() {
		override fun toString(): String {
			val joiner = StringJoiner("\n")
			joiner.add("§a--------------------------------")
			formatInfo(joiner, first)
			joiner.add("§a--------------------------------")
			formatInfo(joiner, second)
			joiner.add("§a--------------------------------")
			formatInfo(joiner, third)
			return joiner.toString()
		}

		private fun formatInfo(joiner: StringJoiner, ability: AbilityBase?) {
			if (ability != null) {
				joiner.add("§b${ability.name} §f[${if (ability.isRestricted) "§7능력 비활성화됨" else "§a능력 활성화됨"}§f] ${ability.rank.rankName} ${ability.species.speciesName}")
				val iterator = ability.explanation
				while (iterator.hasNext()) {
					joiner.add(RESET.toString() + iterator.next())
				}
			} else {
				joiner.add("§f능력이 없습니다.")
			}
		}
	}

	@Throws(SecurityException::class, InstantiationException::class, IllegalAccessException::class, IllegalArgumentException::class, InvocationTargetException::class)
	fun setAbility(first: Class<out AbilityBase>, second: Class<out AbilityBase>, third: Class<out AbilityBase>) {
		removeAbility()
		this.first = create(first, participant)?.apply {
			this.isRestricted = false
		}
		this.second = create(second, participant)?.apply {
			this.isRestricted = false
		}
		this.third = create(third, participant)?.apply {
			this.isRestricted = false
		}
	}

	override fun usesMaterial(material: Material): Boolean {
		return first?.usesMaterial(material) ?: false || second?.usesMaterial(material) ?: false || third?.usesMaterial(material) ?: false
	}

	fun hasAbility(): Boolean {
		return first != null && second != null && third != null
	}

	fun removeAbility() {
		if (hasAbility()) {
			first?.destroy()
			first = null
			second?.destroy()
			second = null
			third?.destroy()
			third = null
		}
	}

	override fun ActiveSkill(material: Material, clickType: ClickType): Boolean {
		return if (hasAbility()) {
			var abilityUsed = false
			if (first is ActiveHandler && (first as ActiveHandler).ActiveSkill(material, clickType)) abilityUsed = true
			if (second is ActiveHandler && (second as ActiveHandler).ActiveSkill(material, clickType)) abilityUsed = true
			if (third is ActiveHandler && (third as ActiveHandler).ActiveSkill(material, clickType)) abilityUsed = true
			abilityUsed
		} else {
			false
		}
	}

	override fun TargetSkill(material: Material, entity: LivingEntity) {
		if (hasAbility()) {
			if (first is TargetHandler) (first as TargetHandler).TargetSkill(material, entity)
			if (second is TargetHandler) (second as TargetHandler).TargetSkill(material, entity)
			if (third is TargetHandler) (third as TargetHandler).TargetSkill(material, entity)
		}
	}

	override fun onUpdate(update: Update) {
		if (update == RESTRICTION_CLEAR) {
			if (hasAbility()) {
				first?.isRestricted = false
				second?.isRestricted = false
				third?.isRestricted = false
			}
		} else if (update == RESTRICTION_SET) {
			if (hasAbility()) {
				first?.isRestricted = true
				second?.isRestricted = true
				third?.isRestricted = true
			}
		} else if (update == ABILITY_DESTROY) {
			if (hasAbility()) {
				first?.destroy()
				first = null
				second?.destroy()
				second = null
				third?.destroy()
				third = null
			}
		}
	}
}