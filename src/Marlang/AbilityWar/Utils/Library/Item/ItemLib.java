package Marlang.AbilityWar.Utils.Library.Item;

import java.util.Map;
import java.util.UUID;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import Marlang.AbilityWar.Utils.VersionCompat.ServerVersion;

public class ItemLib {
	
	public static ColouredItem WOOL = new ColouredItem() {
		
		@Override
		public String getName() {
			return "WOOL";
		}
		
	};
	
	abstract public static class ColouredItem {
		
		private ColouredItem() {}
		
		/**
		 * Material 이름
		 */
		abstract public String getName();
		
		public ItemStack getItemStack(ItemColor color) {
			if(ServerVersion.getVersion() >= 13) {
				Material material = Material.valueOf(color.toString() + "_" + getName());
				return new ItemStack(material);
			} else {
				Material material = Material.valueOf(getName());
				return new ItemStack(material, 1, color.getDamage());
			}
		}

		public boolean compareType(Material material) {
			if(ServerVersion.getVersion() >= 13) {
				String name = material.toString();
				for(ItemColor color : ItemColor.values()) {
					name = name.replaceAll(color.toString() + "_", "");
				}
				
				return name.equalsIgnoreCase(getName());
			} else {
				return material.toString().equalsIgnoreCase(getName());
			}
		}
		
	}

	public static enum ItemColor {

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
			for(ItemColor color : ItemColor.values()) {
				if(color.getDamage() == damage) {
					return color;
				}
			}
			
			return ItemColor.ERROR;
		}
		
	}
	
	@SuppressWarnings("deprecation")
	public static SkullMeta setOwner(SkullMeta meta, String Player) {
		if(ServerVersion.getVersion() >= 13) {
			meta.setOwningPlayer(new OfflinePlayer() {
				
				@Override
				public Map<String, Object> serialize() {
					return null;
				}
				
				@Override
				public void setOp(boolean value) {}
				
				@Override
				public boolean isOp() {
					return false;
				}
				
				@Override
				public void setWhitelisted(boolean value) {}
				
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
					// TODO Auto-generated method stub
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
	
}
