package daybreak.abilitywar.utils;

import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;

public class DamageUtil {

	private DamageUtil() {
	}

	public static boolean canDamage(Entity damager, Entity victim, EntityDamageEvent.DamageCause damageCause, double damage) {
		EntityDamageByEntityEvent fakeEvent = new EntityDamageByEntityEvent(damager, victim, damageCause, damage);
		Bukkit.getPluginManager().callEvent(fakeEvent);
		return !fakeEvent.isCancelled();
	}

	public static boolean canDamage(Entity victim, EntityDamageEvent.DamageCause damageCause, double damage) {
		EntityDamageEvent fakeEvent = new EntityDamageEvent(victim, damageCause, damage);
		Bukkit.getPluginManager().callEvent(fakeEvent);
		return !fakeEvent.isCancelled();
	}

}
