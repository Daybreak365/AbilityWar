package daybreak.abilitywar.utils.library.item;

import com.google.common.base.Enums;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import com.mojang.authlib.properties.PropertyMap;
import daybreak.abilitywar.utils.base.minecraft.version.ServerVersion;
import daybreak.abilitywar.utils.base.reflect.ReflectionUtil.FieldUtil;
import daybreak.abilitywar.utils.library.MaterialX;
import org.apache.commons.codec.binary.Base64;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import java.lang.reflect.InvocationTargetException;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

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

	public static class ColouredItem {

		private final String materialName;

		private ColouredItem(String materialName) {
			this.materialName = materialName;
		}

		@SuppressWarnings("deprecation")
		public ItemStack getItemStack(ItemColor color) {
			if (ServerVersion.getVersionNumber() >= 13) {
				return new ItemStack(Material.valueOf(color.name() + "_" + this.materialName));
			} else {
				return new ItemStack(Material.valueOf(this.materialName), 1, color.getDamage());
			}
		}

		public Block setBlock(Location location, ItemColor color) {
			Block block = location.getBlock();
			if (ServerVersion.getVersionNumber() >= 13) {
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
			if (ServerVersion.getVersionNumber() >= 13) {
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
		meta.setOwner(playerName);
		return meta;
	}

	public static ItemStack getSkull(String owner) {
		ItemStack item = MaterialX.PLAYER_HEAD.parseItem();
		item.setItemMeta(setOwner((SkullMeta) item.getItemMeta(), owner));
		return item;
	}

	public static ItemStack getCustomSkull(String url) {
		ItemStack skull = MaterialX.PLAYER_HEAD.parseItem();
		if (url == null || url.isEmpty()) return skull;
		SkullMeta skullMeta = (SkullMeta) skull.getItemMeta();
		GameProfile profile = new GameProfile(UUID.randomUUID(), null);
		PropertyMap propertyMap = profile.getProperties();
		byte[] encodedData = Base64.encodeBase64(String.format("{textures:{SKIN:{url:\"%s\"}}}", url).getBytes());
		propertyMap.put("textures", new Property("textures", new String(encodedData)));
		try {
			FieldUtil.setValue(skullMeta.getClass(), skullMeta, "profile", profile);
		} catch (NoSuchFieldException | IllegalArgumentException | IllegalAccessException e) {
			logger.log(Level.SEVERE, "getCustomSkull(String): " + e.getClass().getSimpleName());
		}
		skull.setItemMeta(skullMeta);
		return skull;
	}

	public static ItemStack setDurability(ItemStack is, short durability) {
		is.setDurability(durability);
		return is;
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
