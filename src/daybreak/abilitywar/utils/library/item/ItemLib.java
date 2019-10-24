package daybreak.abilitywar.utils.library.item;

import java.util.Map;
import java.util.UUID;

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

import daybreak.abilitywar.utils.versioncompat.ServerVersion;

/**
 * Item 라이브러리
 * @author DayBreak 새벽
 */
public class ItemLib {
	
	private ItemLib() {}
	
	public static ColouredItem WOOL = new ColouredItem("WOOL");
	
	public static class ColouredItem {
		
		private final String materialName;
		
		private ColouredItem(String materialName) {
			this.materialName = materialName;
		}
		
		@SuppressWarnings("deprecation")
		public ItemStack getItemStack(ItemColor color) {
			if(ServerVersion.getVersion() >= 13) {
				Material material = Material.valueOf(color.toString() + "_" + this.materialName);
				return new ItemStack(material);
			} else {
				Material material = Material.valueOf(this.materialName);
				return new ItemStack(material, 1, color.getDamage());
			}
		}

		public boolean compareType(Material material) {
			if(ServerVersion.getVersion() >= 13) {
				String name = material.toString();
				for(ItemColor color : ItemColor.values()) {
					name = name.replaceAll(color.toString() + "_", "");
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
		if(ServerVersion.getVersion() >= 13) {
			if(is.hasItemMeta() && is.getItemMeta() instanceof Damageable) {
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
		
		private Object Potion;
		private PotionType effectType;
		private boolean Extended = false;
		private boolean Upgraded = false;

		public PotionBuilder(PotionType effect, PotionShape type) {
			if(ServerVersion.getVersion() >= 9) {
				switch(type) {
					case NORMAL:
						Potion = new ItemStack(Material.POTION);
						break;
					case SPLASH:
						Potion = new ItemStack(Material.SPLASH_POTION);
						break;
					case LINGERING:
						Potion = new ItemStack(Material.LINGERING_POTION);
						break;
				}
			} else {
				try {
					Class<?> potionClass = Class.forName("org.bukkit.potion.Potion");
					Potion = potionClass.getConstructor(PotionType.class).newInstance(effect);
					potionClass.getMethod("setSplash", boolean.class).invoke(potionClass.cast(Potion), type.equals(PotionShape.SPLASH));
				} catch(Exception ex) {}
			}
			
			this.effectType = effect;
		}
		
		public PotionBuilder setExtended(boolean Extended) {
			this.Extended = Extended;
			return this;
		}
		
		public PotionBuilder setUpgraded(boolean Upgraded) {
			this.Upgraded = Upgraded;
			return this;
		}
		
		/**
		 * 포션을 ItemStack으로 받아옵니다.
		 * @param Amount	개수
		 * @return			ItemStack
		 */
		public ItemStack getItemStack(int Amount) throws Exception {
			if(ServerVersion.getVersion() >= 9) {
				ItemStack potion = (ItemStack) Potion;
				potion.setAmount(Amount);
				PotionMeta meta = (PotionMeta) potion.getItemMeta();
				try {
					boolean Extend = Extended, Upgrade = Upgraded;
					if(!effectType.isExtendable()) Extend = false;
					if(!effectType.isUpgradeable()) Upgrade = false;

					meta.setBasePotionData(new PotionData(effectType, Extend, Upgrade));
				} catch(Exception ex) {}
				potion.setItemMeta(meta);
				
				return potion;
			} else {
				Class<?> potionClass = Class.forName("org.bukkit.potion.Potion");
				potionClass.getMethod("setHasExtendedDuration", boolean.class).invoke(potionClass.cast(Potion), Extended);
				potionClass.getMethod("setLevel", int.class).invoke(potionClass.cast(Potion), Upgraded ? 2 : 1);
				return (ItemStack) potionClass.getMethod("toItemStack", int.class).invoke(potionClass.cast(Potion), Amount);
			}
		}
		
		public enum PotionShape { NORMAL, SPLASH, LINGERING }
		
	}
	
}
