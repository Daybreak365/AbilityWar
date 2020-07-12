package daybreak.abilitywar.game.list.mix

import daybreak.abilitywar.ability.AbilityBase
import daybreak.abilitywar.ability.AbilityBase.Update.RESTRICTION_CLEAR
import daybreak.abilitywar.ability.AbilityBase.Update.RESTRICTION_SET
import daybreak.abilitywar.ability.AbilityManifest
import daybreak.abilitywar.ability.AbilityManifest.Rank.SPECIAL
import daybreak.abilitywar.ability.AbilityManifest.Species.OTHERS
import daybreak.abilitywar.ability.decorator.ActiveHandler
import daybreak.abilitywar.ability.decorator.TargetHandler
import daybreak.abilitywar.game.AbstractGame.Participant
import daybreak.abilitywar.game.list.mix.synergy.Synergy
import daybreak.abilitywar.game.list.mix.synergy.SynergyFactory
import org.bukkit.ChatColor.RESET
import org.bukkit.Material
import org.bukkit.entity.LivingEntity
import java.lang.reflect.InvocationTargetException
import java.util.StringJoiner

@AbilityManifest(name = "믹스", rank = SPECIAL, species = OTHERS, explain = ["$(EXPLAIN)"])
class Mix(participant: Participant) : AbilityBase(participant), ActiveHandler, TargetHandler {
	var synergy: Synergy? = null
		private set
	var first: AbilityBase? = null
		private set
	var second: AbilityBase? = null
		private set
	private val EXPLAIN: Any = object : Any() {
		override fun toString(): String {
			val joiner = StringJoiner("\n")
			joiner.add("§a--------------------------------")
			return if (synergy != null) {
				val base = SynergyFactory.getSynergyBase(synergy!!.registration)
				joiner.add("§f시너지: §a" + base.left.manifest.name + " §f+ §a" + base.right.manifest.name)
				joiner.add("§a--------------------------------")
				joiner.add("§b" + synergy!!.name + " " + synergy!!.rank.rankName + " " + synergy!!.species.speciesName)
				val iterator = synergy!!.explanation
				while (iterator.hasNext()) {
					joiner.add(RESET.toString() + iterator.next())
				}
				joiner.toString()
			} else {
				formatInfo(joiner, first)
				joiner.add("§a--------------------------------")
				formatInfo(joiner, second)
				joiner.toString()
			}
		}

		private fun formatInfo(joiner: StringJoiner, ability: AbilityBase?) {
			if (ability != null) {
				joiner.add("§b" + ability.name + " " + ability.rank.rankName + " " + ability.species.speciesName)
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
	fun setAbility(first: Class<out AbilityBase?>?, second: Class<out AbilityBase?>?) {
		removeAbility()
		val synergyReg = SynergyFactory.getSynergy(first, second)
		if (synergyReg != null) {
			synergy = create(synergyReg.abilityClass, participant) as Synergy
		} else {
			this.first = create(first, participant)
			this.second = create(second, participant)
		}
	}

	fun hasSynergy(): Boolean {
		return synergy != null
	}

	fun hasAbility(): Boolean {
		return synergy != null || first != null && second != null
	}

	fun removeAbility() {
		if (hasAbility()) {
			synergy?.destroy()
			synergy = null
			first?.destroy()
			first = null
			second?.destroy()
			second = null
		}
	}

	override fun ActiveSkill(material: Material, clickType: ClickType): Boolean {
		return if (hasAbility()) {
			if (synergy != null) {
				synergy is ActiveHandler && (synergy as ActiveHandler).ActiveSkill(material, clickType)
			} else {
				var abilityUsed = false
				if (first is ActiveHandler && (first as ActiveHandler).ActiveSkill(material, clickType)) abilityUsed = true
				if (second is ActiveHandler && (second as ActiveHandler).ActiveSkill(material, clickType)) abilityUsed = true
				abilityUsed
			}
		} else {
			false
		}
	}

	override fun TargetSkill(material: Material, entity: LivingEntity) {
		if (hasAbility()) {
			if (synergy != null) {
				if (synergy is TargetHandler) (synergy as TargetHandler).TargetSkill(material, entity)
			} else {
				if (first is TargetHandler) (first as TargetHandler).TargetSkill(material, entity)
				if (second is TargetHandler) (second as TargetHandler).TargetSkill(material, entity)
			}
		}
	}

	override fun onUpdate(update: Update) {
		if (update == RESTRICTION_CLEAR) {
			if (hasAbility()) {
				synergy?.isRestricted = false
				first?.isRestricted = false
				second?.isRestricted = false
			}
		} else if (update == RESTRICTION_SET) {
			if (hasAbility()) {
				synergy?.isRestricted = true
				first?.isRestricted = true
				second?.isRestricted = true
			}
		}
	}
}