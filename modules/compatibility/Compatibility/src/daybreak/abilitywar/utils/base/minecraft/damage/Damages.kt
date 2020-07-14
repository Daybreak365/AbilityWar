package daybreak.abilitywar.utils.base.minecraft.damage

import daybreak.abilitywar.utils.base.minecraft.version.ServerVersion
import daybreak.abilitywar.utils.base.minecraft.version.UnsupportedVersionException
import org.bukkit.entity.Entity
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player

class Damages {
	companion object INSTANCE : iDamages {
		private val INSTANCE: iDamages = try {
			Class.forName("daybreak.abilitywar.utils.base.minecraft.damage." + ServerVersion.getVersion().name + ".DamageImpl").asSubclass(iDamages::class.java).getConstructor().newInstance()
		} catch (e: Exception) {
			throw UnsupportedVersionException()
		}

		@JvmStatic
		override fun damageArrow(entity: Entity, shooter: LivingEntity, damage: Float): Boolean {
			return INSTANCE.damageArrow(entity, shooter, damage)
		}

		@JvmStatic
		override fun damageFixed(entity: Entity, damager: Player, damage: Float): Boolean {
			return INSTANCE.damageFixed(entity, damager, damage)
		}
	}
}