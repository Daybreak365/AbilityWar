package daybreak.abilitywar.utils.library.item;

import daybreak.abilitywar.utils.base.minecraft.version.ServerVersion;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;

/**
 * 인첸트 라이브러리
 *
 * @author Daybreak 새벽
 * @version 1.2 (Minecraft 1.14)
 */
public class EnchantLib {

	private EnchantLib() {
	}

	public static SimpleEnchantment ARROW_DAMAGE = new SimpleEnchantment("ARROW_DAMAGE", "power", 5);
	public static SimpleEnchantment ARROW_FIRE = new SimpleEnchantment("ARROW_FIRE", "flame", 5);
	public static SimpleEnchantment ARROW_INFINITE = new SimpleEnchantment("ARROW_INFINITE", "infinity", 5);
	public static SimpleEnchantment ARROW_KNOCKBACK = new SimpleEnchantment("ARROW_KNOCKBACK", "punch", 5);
	public static SimpleEnchantment BINDING_CURSE = new SimpleEnchantment("BINDING_CURSE", "binding_curse", 11);
	public static SimpleEnchantment CHANNELING = new SimpleEnchantment("CHANNELING", "channeling", 13);
	public static SimpleEnchantment DAMAGE_ALL = new SimpleEnchantment("DAMAGE_ALL", "sharpness", 5);
	public static SimpleEnchantment DAMAGE_ARTHROPODS = new SimpleEnchantment("DAMAGE_ARTHROPODS", "bane_of_arthropods", 5);
	public static SimpleEnchantment DAMAGE_UNDEAD = new SimpleEnchantment("DAMAGE_UNDEAD", "smite", 5);
	public static SimpleEnchantment DEPTH_STRIDER = new SimpleEnchantment("DEPTH_STRIDER", "depth_strider", 8);
	public static SimpleEnchantment DIG_SPEED = new SimpleEnchantment("DIG_SPEED", "efficiency", 5);
	public static SimpleEnchantment DURABILITY = new SimpleEnchantment("DURABILITY", "unbreaking", 5);
	public static SimpleEnchantment FIRE_ASPECT = new SimpleEnchantment("FIRE_ASPECT", "fire_aspect", 5);
	public static SimpleEnchantment FROST_WALKER = new SimpleEnchantment("FROST_WALKER", "frost_walker", 9);
	public static SimpleEnchantment IMPALING = new SimpleEnchantment("IMPALING", "impaling", 13);
	public static SimpleEnchantment KNOCKBACK = new SimpleEnchantment("KNOCKBACK", "knockback", 5);
	public static SimpleEnchantment LOOT_BONUS_BLOCKS = new SimpleEnchantment("LOOT_BONUS_BLOCKS", "fortune", 5);
	public static SimpleEnchantment LOOT_BONUS_MOBS = new SimpleEnchantment("LOOT_BONUS_MOBS", "looting", 5);
	public static SimpleEnchantment LOYALTY = new SimpleEnchantment("LOYALTY", "loyalty", 13);
	public static SimpleEnchantment LUCK = new SimpleEnchantment("LUCK", "luck_of_the_sea", 7);
	public static SimpleEnchantment LURE = new SimpleEnchantment("LURE", "lure", 7);
	public static SimpleEnchantment MENDING = new SimpleEnchantment("MENDING", "mending", 9);
	public static SimpleEnchantment MULTSHOT = new SimpleEnchantment("MULTSHOT", "multishot", 14);
	public static SimpleEnchantment OXYGEN = new SimpleEnchantment("OXYGEN", "respiration", 5);
	public static SimpleEnchantment PIERCING = new SimpleEnchantment("PIERCING", "piercing", 14);
	public static SimpleEnchantment PROTECTION_ENVIRONMENTAL = new SimpleEnchantment("PROTECTION_ENVIRONMENTAL", "protection", 5);
	public static SimpleEnchantment PROTECTION_EXPLOSIONS = new SimpleEnchantment("PROTECTION_EXPLOSIONS", "blast_protection", 5);
	public static SimpleEnchantment PROTECTION_FALL = new SimpleEnchantment("PROTECTION_FALL", "feather_falling", 5);
	public static SimpleEnchantment PROTECTION_FIRE = new SimpleEnchantment("PROTECTION_FIRE", "fire_protection", 5);
	public static SimpleEnchantment PROTECTION_PROJECTILE = new SimpleEnchantment("PROTECTION_PROJECTILE", "projectile_protection", 5);
	public static SimpleEnchantment QUICK_CHARGE = new SimpleEnchantment("QUICK_CHARGE", "quick_charge", 14);
	public static SimpleEnchantment RIPTIDE = new SimpleEnchantment("RIPTIDE", "riptide", 13);
	public static SimpleEnchantment SILK_TOUCH = new SimpleEnchantment("SILK_TOUCH", "silk_touch", 5);
	public static SimpleEnchantment SWEEPING_EDGE = new SimpleEnchantment("SWEEPING_EDGE", "sweeping", 12);
	public static SimpleEnchantment THORNS = new SimpleEnchantment("THORNS", "thorns", 5);
	public static SimpleEnchantment VANISHING_CURSE = new SimpleEnchantment("VANISHING_CURSE", "vanishing_curse", 11);
	public static SimpleEnchantment WATER_WORKER = new SimpleEnchantment("WATER_WORKER", "aqua_affinity", 5);

	public static class SimpleEnchantment {

		private final Enchantment enchantment;

		@SuppressWarnings("deprecation")
		private SimpleEnchantment(String enchantName, String key, int version) {
			if (ServerVersion.getVersion() >= version) {
				if (ServerVersion.getVersion() >= 13) {
					this.enchantment = Enchantment.getByKey(NamespacedKey.minecraft(key));
				} else {
					this.enchantment = Enchantment.getByName(enchantName);
				}
			} else {
				this.enchantment = null;
			}
		}

		/**
		 * 아이템에 인첸트를 추가합니다.
		 *
		 * @param item  인첸트를 추가할 아이템
		 * @param level 인첸트 레벨
		 */
		public ItemStack addEnchantment(ItemStack item, int level) {
			if (enchantment != null) {
				item.addEnchantment(enchantment, level);
			}
			return item;
		}

		/**
		 * 아이템에 인첸트를 추가합니다.
		 *
		 * @param item  인첸트를 추가할 아이템
		 * @param level 인첸트 레벨
		 */
		public ItemStack addUnsafeEnchantment(ItemStack item, int level) {
			if (enchantment != null) {
				item.addUnsafeEnchantment(enchantment, level);
			}
			return item;
		}

		/**
		 * 아이템에서 인첸트를 제거합니다.
		 *
		 * @param item 인첸트를 제거할 아이템
		 */
		public ItemStack removeEnchantment(ItemStack item) {
			if (enchantment != null) {
				item.removeEnchantment(enchantment);
			}
			return item;
		}

		/**
		 * 아이템에 부여된 인첸트의 레벨을 확인합니다.
		 * 현재 서버 버전에서 사용할 수 없는 인첸트일 경우 -1을 반환합니다.
		 *
		 * @param item 인첸트 레벨을 확인할 아이템
		 */
		public int getEnchantmentLevel(ItemStack item) {
			if (enchantment != null) {
				return item.getEnchantmentLevel(enchantment);
			} else {
				return -1;
			}
		}

	}

}
