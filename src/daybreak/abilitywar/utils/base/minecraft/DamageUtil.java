package daybreak.abilitywar.utils.base.minecraft;

import daybreak.abilitywar.utils.annotations.Beta;
import daybreak.abilitywar.utils.library.MaterialX;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.attribute.Attributable;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Entity;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;

import java.util.HashMap;
import java.util.Map;

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

	@Beta
	public static <D extends Entity & Attributable, V extends Entity & Attributable> double getPenetratedDamage(D damager, V victim, double damage) {
		double attackDamage = damager.getAttribute(Attribute.GENERIC_ATTACK_DAMAGE).getValue();
		double defensePoint = victim.getAttribute(Attribute.GENERIC_ARMOR).getValue();
		double base = (attackDamage * (1 - (Math.min(20, Math.max(defensePoint / 5, defensePoint - (attackDamage / (2 + (victim.getAttribute(Attribute.GENERIC_ARMOR_TOUGHNESS).getValue() / 4))))) / 25)));
		return base * (damage / base);
	}

	@Beta
	public static class Damage {

		private static final Map<Material, Double> damageMap = new HashMap<>();

		private static double register(MaterialX material, double value) {
			damageMap.putIfAbsent(material.parseMaterial(), value);
			return value;
		}

		public static final double TRIDENT = register(MaterialX.TRIDENT, 9);

		public static final double WOODEN_SWORD = register(MaterialX.WOODEN_SWORD, 4);
		public static final double STONE_SWORD = register(MaterialX.STONE_SWORD, 5);
		public static final double IRON_SWORD = register(MaterialX.IRON_SWORD, 6);
		public static final double GOLDEN_SWORD = register(MaterialX.GOLDEN_SWORD, 4);
		public static final double DIAMOND_SWORD = register(MaterialX.DIAMOND_SWORD, 7);

		public static final double WOODEN_SHOVEL = register(MaterialX.WOODEN_SHOVEL, 2.5);
		public static final double STONE_SHOVEL = register(MaterialX.STONE_SHOVEL, 3.5);
		public static final double IRON_SHOVEL = register(MaterialX.IRON_SHOVEL, 4.5);
		public static final double GOLDEN_SHOVEL = register(MaterialX.GOLDEN_SHOVEL, 2.5);
		public static final double DIAMOND_SHOVEL = register(MaterialX.DIAMOND_SHOVEL, 5.5);

		public static final double WOODEN_PICKAXE = register(MaterialX.WOODEN_PICKAXE, 2);
		public static final double STONE_PICKAXE = register(MaterialX.STONE_PICKAXE, 3);
		public static final double IRON_PICKAXE = register(MaterialX.IRON_PICKAXE, 4);
		public static final double GOLDEN_PICKAXE = register(MaterialX.GOLDEN_PICKAXE, 2);
		public static final double DIAMOND_PICKAXE = register(MaterialX.DIAMOND_PICKAXE, 5);

		public static final double WOODEN_AXE = register(MaterialX.WOODEN_AXE, 7);
		public static final double STONE_AXE = register(MaterialX.STONE_AXE, 9);
		public static final double IRON_AXE = register(MaterialX.IRON_AXE, 9);
		public static final double GOLDEN_AXE = register(MaterialX.GOLDEN_AXE, 7);
		public static final double DIAMOND_AXE = register(MaterialX.DIAMOND_AXE, 9);

		public static final double DEFAULT = 1;

		public static double of(Material material) {
			return damageMap.getOrDefault(material, 1.0);
		}

	}

	@Beta
	public static class DefensePoint {

		private static final Map<Material, DefensePoint> defenseMap = new HashMap<>();

		private final double defensePoint;
		private final double toughness;

		private DefensePoint(double defensePoint, boolean toughness) {
			this.defensePoint = defensePoint;
			this.toughness = toughness ? 2 : 0;
		}

		private DefensePoint(MaterialX material, double defensePoint, boolean toughness) {
			this(defensePoint, toughness);
			defenseMap.putIfAbsent(material.parseMaterial(), this);
		}

		public static final DefensePoint LEATHER_HELMET = new DefensePoint(MaterialX.LEATHER_HELMET, 1, false);
		public static final DefensePoint LEATHER_CHESTPLATE = new DefensePoint(MaterialX.LEATHER_CHESTPLATE, 3, false);
		public static final DefensePoint LEATHER_LEGGINGS = new DefensePoint(MaterialX.LEATHER_LEGGINGS, 2, false);
		public static final DefensePoint LEATHER_BOOTS = new DefensePoint(MaterialX.LEATHER_BOOTS, 1, false);

		public static final DefensePoint GOLDEN_HELMET = new DefensePoint(MaterialX.GOLDEN_HELMET, 2, false);
		public static final DefensePoint GOLDEN_CHESTPLATE = new DefensePoint(MaterialX.GOLDEN_CHESTPLATE, 5, false);
		public static final DefensePoint GOLDEN_LEGGINGS = new DefensePoint(MaterialX.GOLDEN_LEGGINGS, 3, false);
		public static final DefensePoint GOLDEN_BOOTS = new DefensePoint(MaterialX.GOLDEN_BOOTS, 1, false);

		public static final DefensePoint CHAINMAIL_HELMET = new DefensePoint(MaterialX.CHAINMAIL_HELMET, 2, false);
		public static final DefensePoint CHAINMAIL_CHESTPLATE = new DefensePoint(MaterialX.CHAINMAIL_CHESTPLATE, 5, false);
		public static final DefensePoint CHAINMAIL_LEGGINGS = new DefensePoint(MaterialX.CHAINMAIL_LEGGINGS, 4, false);
		public static final DefensePoint CHAINMAIL_BOOTS = new DefensePoint(MaterialX.CHAINMAIL_BOOTS, 1, false);

		public static final DefensePoint IRON_HELMET = new DefensePoint(MaterialX.IRON_HELMET, 2, false);
		public static final DefensePoint IRON_CHESTPLATE = new DefensePoint(MaterialX.IRON_CHESTPLATE, 6, false);
		public static final DefensePoint IRON_LEGGINGS = new DefensePoint(MaterialX.IRON_LEGGINGS, 5, false);
		public static final DefensePoint IRON_BOOTS = new DefensePoint(MaterialX.IRON_BOOTS, 2, false);

		public static final DefensePoint DIAMOND_HELMET = new DefensePoint(MaterialX.DIAMOND_HELMET, 3, true);
		public static final DefensePoint DIAMOND_CHESTPLATE = new DefensePoint(MaterialX.DIAMOND_CHESTPLATE, 8, true);
		public static final DefensePoint DIAMOND_LEGGINGS = new DefensePoint(MaterialX.DIAMOND_LEGGINGS, 6, true);
		public static final DefensePoint DIAMOND_BOOTS = new DefensePoint(MaterialX.DIAMOND_BOOTS, 3, true);

		public static final DefensePoint TURTLE_HELMET = new DefensePoint(MaterialX.TURTLE_HELMET, 2, false);

		public static DefensePoint of(Material material) {
			return defenseMap.getOrDefault(material, null);
		}

	}

}
