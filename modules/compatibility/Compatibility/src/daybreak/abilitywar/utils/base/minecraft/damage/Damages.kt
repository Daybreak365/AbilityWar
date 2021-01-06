package daybreak.abilitywar.utils.base.minecraft.damage

import daybreak.abilitywar.utils.base.minecraft.nms.NMS
import daybreak.abilitywar.utils.base.minecraft.version.ServerVersion
import daybreak.abilitywar.utils.base.minecraft.version.VersionNotSupportedException
import org.bukkit.Bukkit
import org.bukkit.GameMode.ADVENTURE
import org.bukkit.GameMode.SURVIVAL
import org.bukkit.attribute.Attribute
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Entity
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.event.entity.EntityDamageEvent.DamageCause
import org.bukkit.potion.PotionEffectType
import kotlin.math.max

class Damages private constructor() {
	companion object INSTANCE : IDamages {
		private val INSTANCE: IDamages = try {
			Class.forName("daybreak.abilitywar.utils.base.minecraft.damage." + ServerVersion.name + ".DamageImpl").asSubclass(IDamages::class.java).getConstructor().newInstance()
		} catch (e: Exception) {
			throw VersionNotSupportedException()
		}

		object Flag {
			const val ARMOR = 0x00000001
			const val RESISTANCE = 0x00000002
			const val ENCHANTMENT = 0x0000004
			const val ABSORPTION = 0x00000008
			const val ALL = ARMOR or RESISTANCE or ENCHANTMENT or ABSORPTION
			@JvmStatic
			fun hasFlag(flags: Int, flag: Int): Boolean {
				return flags and flag == flag
			}
		}

		@JvmStatic
		fun getFinalDamage(victim: LivingEntity, damage: Double, flags: Int): Double {
			var damage = damage
			if (Flag.hasFlag(flags, Flag.ARMOR)) {
				val armorStrength = victim.getAttribute(Attribute.GENERIC_ARMOR)?.value ?: .0
				val armorToughness = victim.getAttribute(Attribute.GENERIC_ARMOR_TOUGHNESS)?.value ?: .0
				damage -= damage - (damage * (1.0 - (armorStrength - damage / (2.0 + armorToughness / 4.0)).coerceIn(armorStrength * .2, 20.0) / 25.0))
			}
			if (Flag.hasFlag(flags, Flag.RESISTANCE)) {
				val resistance = victim.getPotionEffect(PotionEffectType.DAMAGE_RESISTANCE)
				if (resistance != null) {
					damage -= damage - damage * (25 - (resistance.amplifier + 1) * 5) / 25.0
				}
			}
			if (Flag.hasFlag(flags, Flag.ENCHANTMENT)) {
				var protection = 0
				victim.equipment?.armorContents?.forEach {
					if (it != null) {
						protection += it.getEnchantmentLevel(Enchantment.PROTECTION_ENVIRONMENTAL)
					}
				}
				if (protection > 0) {
					damage -= damage - (damage * (1.0 - protection.coerceIn(0, 20) / 25.0))
				}
			}
			if (Flag.hasFlag(flags, Flag.ABSORPTION)) {
				damage -= (damage - max(damage - NMS.getAbsorptionHearts(victim), 0.0)).coerceAtLeast(0.0)
			}
			return damage
		}

		@JvmStatic
		override fun damageArrow(entity: Entity, shooter: LivingEntity, damage: Float): Boolean {
			return INSTANCE.damageArrow(entity, shooter, damage)
		}

		@JvmStatic
		override fun damageFixed(entity: Entity, damager: LivingEntity, damage: Float): Boolean {
			return INSTANCE.damageFixed(entity, damager, damage)
		}

		@JvmStatic
		override fun damageMagic(entity: Entity, damager: Player?, ignoreArmor: Boolean, damage: Float): Boolean {
			return INSTANCE.damageMagic(entity, damager, ignoreArmor, damage)
		}

		private fun <T : EntityDamageEvent> canDamage(event: T, victim: Entity): Boolean {
			Bukkit.getPluginManager().callEvent(event)
			return if (event.isCancelled) false else {
				if (victim is Player) {
					val gameMode = victim.gameMode
					gameMode == SURVIVAL || gameMode == ADVENTURE
				} else true
			}
		}

		@JvmStatic
		fun canDamage(victim: Entity, damager: Entity, damageCause: DamageCause, damage: Double): Boolean {
			return canDamage(EntityDamageByEntityEvent(damager, victim, damageCause, damage), victim)
		}

		@JvmStatic
		fun canDamage(victim: Entity, damageCause: DamageCause, damage: Double): Boolean {
			return canDamage(EntityDamageEvent(victim, damageCause, damage), victim)
		}
	}
}