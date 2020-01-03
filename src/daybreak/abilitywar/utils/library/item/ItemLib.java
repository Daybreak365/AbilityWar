package daybreak.abilitywar.utils.library.item;

import com.google.common.base.Enums;
import daybreak.abilitywar.utils.versioncompat.ServerVersion;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.potion.PotionData;
import org.bukkit.potion.PotionType;

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

	public static class ColouredItem {

		private final String materialName;

		private ColouredItem(String materialName) {
			this.materialName = materialName;
		}

		@SuppressWarnings("deprecation")
		public ItemStack getItemStack(ItemColor color) {
			if (ServerVersion.getVersion() >= 13) {
				Material material = Material.valueOf(color.toString() + "_" + this.materialName);
				return new ItemStack(material);
			} else {
				Material material = Material.valueOf(this.materialName);
				return new ItemStack(material, 1, color.getDamage());
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

	public enum ItemColor {

		/**
		 * 하얀색
		 */
		WHITE((short) 0),
		/**
		 * 주황색
		 */
		ORANGE((short) 1),
		/**
		 * 자홍색
		 */
		MAGENTA((short) 2),
		/**
		 * 하늘색
		 */
		LIGHT_BLUE((short) 3),
		/**
		 * 노란색
		 */
		YELLOW((short) 4),
		/**
		 * 연두색
		 */
		LIME((short) 5),
		/**
		 * 분홍색
		 */
		PINK((short) 6),
		/**
		 * 회색
		 */
		GRAY((short) 7),
		/**
		 * 회백색
		 */
		LIGHT_GRAY((short) 8),
		/**
		 * 청록색
		 */
		CYAN((short) 9),
		/**
		 * 보라색
		 */
		PURPLE((short) 10),
		/**
		 * 파란색
		 */
		BLUE((short) 11),
		/**
		 * 갈색
		 */
		BROWN((short) 12),
		/**
		 * 초록색
		 */
		GREEN((short) 13),
		/**
		 * 빨간색
		 */
		RED((short) 14),
		/**
		 * 검은색
		 */
		BLACK((short) 15),
		/**
		 * 에러가 났을 경우 사용되는 값입니다.
		 */
		ERROR((short) -1);

		private short damage;

		private ItemColor(short damage) {
			this.damage = damage;
		}

		public short getDamage() {
			return damage;
		}

		/**
		 * Damage로 ItemColor를 받아옵니다.
		 * 해당하는 ItemColor가 없을 경우 ERROR를 반환합니다.
		 */
		public static ItemColor getByDamage(short damage) {
			for (ItemColor color : ItemColor.values()) {
				if (color.getDamage() == damage) {
					return color;
				}
			}

			return ItemColor.ERROR;
		}

	}

	@SuppressWarnings("deprecation")
	public static SkullMeta setOwner(SkullMeta meta, String Player) {
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
					return Player;
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
			meta.setOwner(Player);
		}

		return meta;
	}

	public static ItemStack getHead(String owner) {
		ItemStack item = MaterialLib.PLAYER_HEAD.getItem();

		SkullMeta meta = (SkullMeta) item.getItemMeta();
		item.setItemMeta(setOwner(meta, owner));

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

}
