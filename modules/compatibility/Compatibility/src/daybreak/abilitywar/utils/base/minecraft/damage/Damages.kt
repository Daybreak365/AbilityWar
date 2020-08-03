package daybreak.abilitywar.utils.base.minecraft.damage

import daybreak.abilitywar.utils.base.minecraft.version.ServerVersion
import daybreak.abilitywar.utils.base.minecraft.version.VersionNotSupportedException
import org.bukkit.Bukkit
import org.bukkit.GameMode.ADVENTURE
import org.bukkit.GameMode.SURVIVAL
import org.bukkit.entity.Entity
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.event.entity.EntityDamageEvent.DamageCause

class Damages private constructor() {
	companion object INSTANCE : IDamages {
		private val INSTANCE: IDamages = try {
			Class.forName("daybreak.abilitywar.utils.base.minecraft.damage." + ServerVersion.name + ".DamageImpl").asSubclass(IDamages::class.java).getConstructor().newInstance()
		} catch (e: Exception) {
			throw VersionNotSupportedException()
		}

		@JvmStatic
		override fun damageArrow(entity: Entity, shooter: LivingEntity, damage: Float): Boolean {
			return INSTANCE.damageArrow(entity, shooter, damage)
		}

		@JvmStatic
		override fun damageFixed(entity: Entity, damager: Player, damage: Float): Boolean {
			return INSTANCE.damageFixed(entity, damager, damage)
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