package daybreak.abilitywar.utils.library.item;

import com.google.common.base.Enums;
import daybreak.abilitywar.utils.base.Hashes;
import daybreak.abilitywar.utils.base.minecraft.version.ServerVersion;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
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

	private ItemLib() {
	}

	public static final ColouredItem WOOL = new ColouredItem("WOOL");
	public static final ColouredItem STAINED_GLASS = new ColouredItem("STAINED_GLASS");
	public static final ColouredItem STAINED_GLASS_PANE = new ColouredItem("STAINED_GLASS_PANE");


	private static final int prime = 31;
	public static int hashCode(final ItemStack stack) {
		int hashCode = 1;
		hashCode = prime * hashCode + stack.getType().ordinal();
		hashCode = prime * hashCode + stack.getAmount();
		final ItemMeta meta = stack.getItemMeta();
		if (ServerVersion.getVersion() >= 13) {
			if (meta instanceof Damageable) {
				hashCode = prime * hashCode + ((Damageable) meta).getDamage();
			}
		} else {
			hashCode = prime * hashCode + stack.getDurability();
		}
		if (meta != null) {
			if (meta.hasDisplayName()) {
				hashCode = prime * hashCode + meta.getDisplayName().hashCode();
			}
			if (meta.hasEnchants()) {
				for (Entry<Enchantment, Integer> entry : meta.getEnchants().entrySet()) {
					hashCode = prime * hashCode + Hashes.hashCode(entry.getKey().hashCode(), entry.getValue().hashCode());
				}
			}
		}
		return hashCode;
	}

	@SuppressWarnings("deprecation")
	public static ItemStack setDurability(final ItemStack itemStack, final short durability) {
		if (ServerVersion.getVersion() >= 13) {
			if (itemStack.hasItemMeta() && itemStack.getItemMeta() instanceof Damageable) {
				final Damageable damageable = (Damageable) itemStack.getItemMeta();
				damageable.setDamage(durability);
				itemStack.setItemMeta((ItemMeta) damageable);
			}
		} else {
			itemStack.setDurability(durability);
		}

		return itemStack;
	}

	public enum ItemColor {

		WHITE(0),
		ORANGE(1),
		MAGENTA(2),
		LIGHT_BLUE(3),
		YELLOW(4),
		LIME(5),
		PINK(6),
		GRAY(7),
		LIGHT_GRAY(8),
		CYAN(9),
		PURPLE(10),
		BLUE(11),
		BROWN(12),
		GREEN(13),
		RED(14),
		BLACK(15);

		private final short damage;

		ItemColor(final int damage) {
			this.damage = (short) damage;
		}

		public short getDamage() {
			return damage;
		}

		public static ItemColor getByDamage(final int damage) {
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
				String name = material.toString(), color = name.split("_")[0];
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
