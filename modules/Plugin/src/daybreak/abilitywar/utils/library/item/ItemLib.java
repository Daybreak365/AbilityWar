package daybreak.abilitywar.utils.library.item;

import com.google.common.base.Enums;
import daybreak.abilitywar.utils.base.logging.Logger;
import daybreak.abilitywar.utils.base.minecraft.version.ServerVersion;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionData;
import org.bukkit.potion.PotionType;

/**
 * Item 라이브러리
 *
 * @author Daybreak 새벽
 */
public class ItemLib {

	private static final Logger logger = Logger.getLogger(ItemLib.class.getName());

	private ItemLib() {
	}

	public static ColouredItem WOOL = new ColouredItem("WOOL");
	public static ColouredItem STAINED_GLASS = new ColouredItem("STAINED_GLASS");
	public static ColouredItem STAINED_GLASS_PANE = new ColouredItem("STAINED_GLASS_PANE");

	@SuppressWarnings("deprecation")
	public static ItemStack setDurability(ItemStack is, short durability) {
		if (ServerVersion.getVersion() >= 13) {
			if (is.hasItemMeta() && is.getItemMeta() instanceof Damageable) {
				Damageable dmg = (Damageable) is.getItemMeta();
				dmg.setDamage(durability);
				is.setItemMeta((ItemMeta) dmg);
			}
		} else {
			is.setDurability(durability);
		}

		return is;
	}

	public enum ItemColor {

		WHITE((short) 0),
		ORANGE((short) 1),
		MAGENTA((short) 2),
		LIGHT_BLUE((short) 3),
		YELLOW((short) 4),
		LIME((short) 5),
		PINK((short) 6),
		GRAY((short) 7),
		LIGHT_GRAY((short) 8),
		CYAN((short) 9),
		PURPLE((short) 10),
		BLUE((short) 11),
		BROWN((short) 12),
		GREEN((short) 13),
		RED((short) 14),
		BLACK((short) 15);

		private final short damage;

		ItemColor(short damage) {
			this.damage = damage;
		}

		public short getDamage() {
			return damage;
		}

		public static ItemColor getByDamage(short damage) {
			return values()[damage];
		}

	}

	private static final ItemStack AIR = new ItemStack(Material.AIR);

	public static class PotionBuilder {

		private PotionType type;
		private PotionShape shape;
		private boolean extended = false;
		private boolean upgraded = false;

		public PotionBuilder(PotionType type, PotionShape shape) {
			this.type = type;
			this.shape = shape;
		}

		public PotionBuilder setType(PotionType type) {
			this.type = type;
			this.extended = type.isExtendable() && extended;
			this.upgraded = type.isUpgradeable() && upgraded;
			return this;
		}

		public PotionBuilder setShape(PotionShape shape) {
			this.shape = shape;
			return this;
		}

		public PotionBuilder setExtended(boolean extended) {
			this.extended = type.isExtendable() && extended;
			return this;
		}

		public PotionBuilder setUpgraded(boolean upgraded) {
			this.upgraded = type.isUpgradeable() && upgraded;
			return this;
		}

		/**
		 * 포션을 ItemStack으로 반환합니다.
		 *
		 * @param amount 개수
		 * @return ItemStack
		 */
		public ItemStack build(int amount) {
			ItemStack stack = new ItemStack(shape.material);
			stack.setAmount(amount);
			try {
				PotionMeta meta = (PotionMeta) stack.getItemMeta();
				meta.setBasePotionData(new PotionData(type, extended, upgraded));
				stack.setItemMeta(meta);
			} catch (Exception ignored) {
			}
			return stack;
		}

		public enum PotionShape {
			NORMAL(Material.POTION),
			SPLASH(Material.SPLASH_POTION),
			LINGERING(Material.LINGERING_POTION);

			final Material material;

			PotionShape(Material material) {
				this.material = material;
			}
		}

	}

	public static class ColouredItem {

		private final String materialName;

		private ColouredItem(String materialName) {
			this.materialName = materialName;
		}

		@SuppressWarnings("deprecation")
		public ItemStack getItemStack(ItemColor color) {
			if (ServerVersion.getVersion() >= 13) {
				return new ItemStack(Material.valueOf(color.name() + "_" + this.materialName));
			} else {
				return new ItemStack(Material.valueOf(this.materialName), 1, color.getDamage());
			}
		}

		public boolean compareType(Material material) {
			if (ServerVersion.getVersion() >= 13) {
				String name = material.toString();
				String color = name.split("_")[0];
				if (Enums.getIfPresent(ItemColor.class, color).isPresent()) {
					name = name.replaceAll(color + "_", "");
				}
				return name.equalsIgnoreCase(this.materialName);
			} else {
				return material.toString().equalsIgnoreCase(this.materialName);
			}
		}

	}

	public static boolean addItem(Inventory inventory, Material type, int amount) {
		final Map<Integer, ItemStack> updates = new HashMap<>();
		for (int i = 0; i < inventory.getContents().length; i++) {
			ItemStack stack = inventory.getItem(i);
			if (stack == null || stack.getType() == Material.AIR) {
				if (amount <= 64) {
					updates.put(i, new ItemStack(type, amount));
					amount = 0;
					break;
				} else {
					updates.put(i, new ItemStack(type, 64));
					amount -= 64;
				}
			} else {
				if (stack.getType() != type || stack.hasItemMeta()) continue;
				int left = 64 - stack.getAmount();
				if (left >= amount) {
					updates.put(i, new ItemStack(type, stack.getAmount() + amount));
					amount = 0;
					break;
				} else {
					updates.put(i, new ItemStack(type, 64));
					amount -= left;
				}
			}
		}
		if (amount <= 0) {
			for (Entry<Integer, ItemStack> entry : updates.entrySet()) {
				inventory.setItem(entry.getKey(), entry.getValue());
			}
			return true;
		} else return false;
	}

	public static boolean removeItem(Inventory inventory, Material type, int amount) {
		final Map<Integer, ItemStack> updates = new HashMap<>();
		for (int i = 0; i < inventory.getContents().length; i++) {
			ItemStack stack = inventory.getItem(i);
			if (stack == null || stack.getType() != type) continue;
			if (stack.getAmount() >= amount) {
				updates.put(i, new ItemStack(type, stack.getAmount() - amount));
				amount = 0;
				break;
			} else {
				updates.put(i, AIR);
				amount -= stack.getAmount();
			}
		}
		if (amount <= 0) {
			for (Entry<Integer, ItemStack> entry : updates.entrySet()) {
				inventory.setItem(entry.getKey(), entry.getValue());
			}
			return true;
		} else return false;
	}

	public static int removeItem(Inventory inventory, Material type) {
		int amount = 0;
		for (int i = 0; i < inventory.getContents().length; i++) {
			ItemStack stack = inventory.getItem(i);
			if (stack == null || stack.getType() != type) continue;
			amount += stack.getAmount();
			inventory.setItem(i, AIR);
		}
		return amount;
	}

}
