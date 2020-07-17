package daybreak.abilitywar.utils.base.minecraft;

import daybreak.abilitywar.utils.base.minecraft.damage.Damages;
import org.bukkit.entity.Entity;
import org.bukkit.event.entity.EntityDamageEvent;

@Deprecated
public class DamageUtil {

	private DamageUtil() {
	}

	public static boolean canDamage(Entity damager, Entity victim, EntityDamageEvent.DamageCause damageCause, double damage) {
		return Damages.canDamage(victim, damager, damageCause, damage);
	}

	public static boolean canDamage(Entity victim, EntityDamageEvent.DamageCause damageCause, double damage) {
		return Damages.canDamage(victim, damageCause, damage);
	}

}
