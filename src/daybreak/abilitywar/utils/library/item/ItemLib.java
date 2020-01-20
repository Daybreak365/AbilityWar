package daybreak.abilitywar.utils.library.item;

import com.google.common.base.Enums;
import daybreak.abilitywar.utils.versioncompat.ServerVersion;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Block;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.potion.PotionData;
import org.bukkit.potion.PotionType;

import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import java.util.UUID;

/**
 * Item 라이브러리
 *
 * @author Daybreak 새벽
 */
public class ItemLib {

	private ItemLib() {
	}

	public static ColouredItem WOOL = new ColouredItem("WOOL");
	public static ColouredItem STAINED_GLASS = new ColouredItem("STAINED_GLASS");
	public static ColouredItem STAINED_GLASS_PANE = new ColouredItem("STAINED_GLASS_PANE");

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

		public Block setBlock(Location location, ItemColor color) {
			Block block = location.getBlock();
			if (ServerVersion.getVersion() >= 13) {
				block.setType(Material.valueOf(color.name() + "_" + this.materialName));
			} else {
				block.setType(Material.valueOf(this.materialName));
				try {
					block.getClass().getDeclaredMethod("setData", byte.class).invoke(block, (byte) color.getDamage());
				} catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException ignored) {
				}
			}
			return block;
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

		private short damage;

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

	@SuppressWarnings("deprecation")
	public static SkullMeta setOwner(SkullMeta meta, String playerName) {
		if (ServerVersion.getVersion() >= 13) {
			meta.setOwningPlayer(new OfflinePlayer() {

				@Override
				public Map<String, Object> serialize() {
					return null;
				}

				@Override
				public void setOp(boolean value) {
				}

				@Override
				public boolean isOp() {
					return false;
				}

				@Override
				public void setWhitelisted(boolean value) {
				}

				@Override
				public boolean isWhitelisted() {
					return false;
				}

				@Override
				public boolean isOnline() {
					return false;
				}

				@Override
				public boolean isBanned() {
					return false;
				}

				@Override
				public boolean hasPlayedBefore() {
					return false;
				}

				@Override
				public UUID getUniqueId() {
					return null;
				}

				@Override
				public org.bukkit.entity.Player getPlayer() {
					return null;
				}

				@Override
				public String getName() {
					return playerName;
				}

				@Override
				public long getLastPlayed() {
					return 0;
				}

				@Override
				public long getFirstPlayed() {
					return 0;
				}

				@Override
				public Location getBedSpawnLocation() {
					return null;
				}
			});
		} else {
			meta.setOwner(playerName);
		}
		return meta;
	}

	public static ItemStack getHead(String owner) {
		ItemStack item = MaterialLib.PLAYER_HEAD.getItem();
		item.setItemMeta(setOwner((SkullMeta) item.getItemMeta(), owner));
		return item;
	}

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
		public ItemStack getItemStack(int amount) {
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

	public static void removeItem(Inventory inventory, Material type, int amount) {
		for (int i = 0; i < inventory.getContents().length; i++) {
			ItemStack stack = inventory.getItem(i);
			if (stack == null || !stack.getType().equals(type)) continue;
			if (stack.getAmount() >= amount) {
				stack.setAmount(stack.getAmount() - amount);
				break;
			} else {
				amount -= stack.getAmount();
				stack.setAmount(0);
			}
		}
	}

}
