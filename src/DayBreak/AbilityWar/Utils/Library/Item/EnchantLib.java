package DayBreak.AbilityWar.Utils.Library.Item;

import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;

import DayBreak.AbilityWar.Utils.VersionCompat.ServerVersion;

/**
 * 인첸트 라이브러리
 * @author DayBreak 새벽
 * @version 1.2 (Minecraft 1.14)
 */
public class EnchantLib {
	
	private EnchantLib() {}
	
	public static Enchants ARROW_DAMAGE = new Enchants("ARROW_DAMAGE", "power", 5);
	public static Enchants ARROW_FIRE = new Enchants("ARROW_FIRE", "flame", 5);
	public static Enchants ARROW_INFINITE = new Enchants("ARROW_INFINITE", "infinity", 5);
	public static Enchants ARROW_KNOCKBACK = new Enchants("ARROW_KNOCKBACK", "punch", 5);
	public static Enchants BINDING_CURSE = new Enchants("BINDING_CURSE", "binding_curse", 11);
	public static Enchants CHANNELING = new Enchants("CHANNELING", "channeling", 13);
	public static Enchants DAMAGE_ALL = new Enchants("DAMAGE_ALL", "sharpness", 5);
	public static Enchants DAMAGE_ARTHROPODS = new Enchants("DAMAGE_ARTHROPODS", "bane_of_arthropods", 5);
	public static Enchants DAMAGE_UNDEAD = new Enchants("DAMAGE_UNDEAD", "smite", 5);
	public static Enchants DEPTH_STRIDER = new Enchants("DEPTH_STRIDER", "depth_strider", 8);
	public static Enchants DIG_SPEED = new Enchants("DIG_SPEED", "efficiency", 5);
	public static Enchants DURABILITY = new Enchants("DURABILITY", "unbreaking", 5);
	public static Enchants FIRE_ASPECT = new Enchants("FIRE_ASPECT", "fire_aspect", 5);
	public static Enchants FROST_WALKER = new Enchants("FROST_WALKER", "frost_walker", 9);
	public static Enchants IMPALING = new Enchants("IMPALING", "impaling", 13);
	public static Enchants KNOCKBACK = new Enchants("KNOCKBACK", "knockback", 5);
	public static Enchants LOOT_BONUS_BLOCKS = new Enchants("LOOT_BONUS_BLOCKS", "fortune", 5);
	public static Enchants LOOT_BONUS_MOBS = new Enchants("LOOT_BONUS_MOBS", "looting", 5);
	public static Enchants LOYALTY = new Enchants("LOYALTY", "loyalty", 13);
	public static Enchants LUCK = new Enchants("LUCK", "luck_of_the_sea", 7);
	public static Enchants LURE = new Enchants("LURE", "lure", 7);
	public static Enchants MENDING = new Enchants("MENDING", "mending", 9);
	public static Enchants MULTSHOT = new Enchants("MULTSHOT", "multishot", 14);
	public static Enchants OXYGEN = new Enchants("OXYGEN", "respiration", 5);
	public static Enchants PIERCING = new Enchants("PIERCING", "piercing", 14);
	public static Enchants PROTECTION_ENVIRONMENTAL = new Enchants("PROTECTION_ENVIRONMENTAL", "protection", 5);
	public static Enchants PROTECTION_EXPLOSIONS = new Enchants("PROTECTION_EXPLOSIONS", "blast_protection", 5);
	public static Enchants PROTECTION_FALL = new Enchants("PROTECTION_FALL", "feather_falling", 5);
	public static Enchants PROTECTION_FIRE = new Enchants("PROTECTION_FIRE", "fire_protection", 5);
	public static Enchants PROTECTION_PROJECTILE = new Enchants("PROTECTION_PROJECTILE", "projectile_protection", 5);
	public static Enchants QUICK_CHARGE = new Enchants("QUICK_CHARGE", "quick_charge", 14);
	public static Enchants RIPTIDE = new Enchants("RIPTIDE", "riptide", 13);
	public static Enchants SILK_TOUCH = new Enchants("SILK_TOUCH", "silk_touch", 5);
	public static Enchants SWEEPING_EDGE = new Enchants("SWEEPING_EDGE", "sweeping", 12);
	public static Enchants THORNS = new Enchants("THORNS", "thorns", 5);
	public static Enchants VANISHING_CURSE = new Enchants("VANISHING_CURSE", "vanishing_curse", 11);
	public static Enchants WATER_WORKER = new Enchants("WATER_WORKER", "aqua_affinity", 5);
	
	public static class Enchants {

		private Enchantment enchantment = null;
		
		@SuppressWarnings("deprecation")
		private Enchants(String enchantName, String key, Integer version) {
			if(ServerVersion.getVersion() >= version) {
				if (ServerVersion.getVersion() >= 13) {
					enchantment = Enchantment.getByKey(NamespacedKey.minecraft(key));
				} else {
					enchantment = Enchantment.getByName(enchantName);
				}
			}
		}
		
		/**
		 * 아이템에 인첸트를 추가합니다.
		 * @param item		인첸트를 추가할 아이템
		 * @param level		인첸트 레벨
		 */
		public ItemStack addEnchantment(ItemStack item, int level) {
			if(enchantment != null) {
				item.addEnchantment(enchantment, level);
			}
			
			return item;
		}
		
		/**
		 * 아이템에 인첸트를 추가합니다.
		 * @param item		인첸트를 추가할 아이템
		 * @param level		인첸트 레벨
		 */
		public ItemStack addUnsafeEnchantment(ItemStack item, int level) {
			if(enchantment != null) {
				item.addUnsafeEnchantment(enchantment, level);
			}
			
			return item;
		}

		/**
		 * 아이템에서 인첸트를 제거합니다.
		 * @param item		인첸트를 제거할 아이템
		 */
		public ItemStack removeEnchantment(ItemStack item) {
			if(enchantment != null) {
				item.removeEnchantment(enchantment);
			}
			
			return item;
		}

		/**
		 * 아이템에 부여된 인첸트의 레벨을 확인합니다.
		 * 현재 서버 버전에서 사용할 수 없는 인첸트일 경우 -1을 반환합니다.
		 * @param item		인첸트 레벨을 확인할 아이템
		 */
		public int getEnchantmentLevel(ItemStack item) {
			if(enchantment != null) {
				return item.getEnchantmentLevel(enchantment);
			} else {
				return -1;
			}
		}
		
	}

}
