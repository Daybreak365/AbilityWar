package Marlang.AbilityWar.GameManager.Manager.GUI;

import java.util.ArrayList;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.plugin.Plugin;

import Marlang.AbilityWar.Utils.Messager;
import Marlang.AbilityWar.Utils.MojangAPI;

/**
 * 기여자 목록 GUI
 */
public class SpecialThanksGUI implements Listener {
	
	private static final SpecialThank[] SpecialThanks = {
				new SpecialThank("f6cef0829b7e48c1a973532389b6e3e1", "능력 아이디어 제공"),
				new SpecialThank("ecb53e2ffdf34089ae3486cff3fc5f34", "능력 아이디어 제공"),
				new SpecialThank("2dcb3299e24049adb8bb554d862bd7be", "능력 아이디어 제공")
			};
	
	public static class SpecialThank {
		
		private String name;
		private String[] role;
		private OfflinePlayer offlinePlayer;
		
		public SpecialThank(String UUID, String... role) {
			try {
				this.name = MojangAPI.getNickname(UUID);
			} catch (Exception e) {
				this.name = "Error";
			}
			this.role = role;
			
			this.offlinePlayer = new OfflinePlayer() {
				
				@Override
				public Map<String, Object> serialize() {
					return null;
				}
				
				@Override
				public void setOp(boolean arg0) {}
				
				@Override
				public boolean isOp() {
					return false;
				}
				
				@Override
				public void setWhitelisted(boolean arg0) {}
				
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
				public Player getPlayer() {
					return null;
				}
				
				@Override
				public String getName() {
					return SpecialThank.this.getName();
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
				
			};
		}
		
		public String getName() {
			return name;
		}
		
		public ArrayList<String> getRole() {
			ArrayList<String> list = new ArrayList<String>();
			for(String s : role) {
				list.add(ChatColor.translateAlternateColorCodes('&', "&f" + s));
			}
			
			return list;
		}
		
		public OfflinePlayer getOfflinePlayer() {
			return offlinePlayer;
		}
		
	}
	
	Player p;
	
	public SpecialThanksGUI(Player p, Plugin plugin) {
		this.p = p;
		Bukkit.getPluginManager().registerEvents(this, plugin);
	}
	
	Integer PlayerPage = 1;
	
	Inventory STGUI;
	
	public void openGUI(Integer page) {
		Integer MaxPage = ((SpecialThanks.length - 1) / 18) + 1;
		if (MaxPage < page) page = 1;
		if(page < 1) page = 1;
		STGUI = Bukkit.createInventory(null, 27, ChatColor.translateAlternateColorCodes('&', "&c&l✿ &0&lSpecial Thanks &c&l✿"));
		PlayerPage = page;
		int Count = 0;
		
		for (SpecialThank st : SpecialThanks) {
			ItemStack is = new ItemStack(Material.SKULL_ITEM, 1, (short) 3);
			SkullMeta im = (SkullMeta) is.getItemMeta();
			if(!st.getName().equals("Error")) {
				im.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&e" + st.getName()));
				
				im.setOwningPlayer(st.getOfflinePlayer());

				im.setLore(st.getRole());
			} else {
				im.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&c오류"));

				im.setLore(Messager.getStringList(ChatColor.translateAlternateColorCodes('&', "&bMojang API&f에 연결할 수 없습니다.")));
			}
			
			is.setItemMeta(im);
			
			if (Count / 18 == page - 1) {
				STGUI.setItem(Count % 18, is);
			}
			Count++;
		}
		
		if(page > 1) {
			ItemStack previousPage = new ItemStack(Material.ARROW, 1);
			ItemMeta previousMeta = previousPage.getItemMeta();
			previousMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&b이전 페이지"));
			previousPage.setItemMeta(previousMeta);
			STGUI.setItem(21, previousPage);
		}
		
		if(page != MaxPage) {
			ItemStack nextPage = new ItemStack(Material.ARROW, 1);
			ItemMeta nextMeta = nextPage.getItemMeta();
			nextMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&b다음 페이지"));
			nextPage.setItemMeta(nextMeta);
			STGUI.setItem(23, nextPage);
		}

		ItemStack Page = new ItemStack(Material.PAPER, 1);
		ItemMeta PageMeta = Page.getItemMeta();
		PageMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&',
				"&6페이지 &e" + page + " &6/ &e" + MaxPage));
		Page.setItemMeta(PageMeta);
		STGUI.setItem(22, Page);
		
		p.openInventory(STGUI);
	}
	
	@EventHandler
	public void onInventoryClose(InventoryCloseEvent e) {
		if(e.getInventory().equals(this.STGUI)) {
			HandlerList.unregisterAll(this);
		}
	}
	
	@EventHandler
	public void onInventoryClick(InventoryClickEvent e) {
		if(e.getInventory().equals(STGUI)) {
			e.setCancelled(true);
			if(e.getCurrentItem() != null && e.getCurrentItem().hasItemMeta() && e.getCurrentItem().getItemMeta().hasDisplayName()) {
				if(e.getCurrentItem().getItemMeta().getDisplayName().equals(ChatColor.translateAlternateColorCodes('&', "&b이전 페이지"))) {
					openGUI(PlayerPage - 1);
				} else if(e.getCurrentItem().getItemMeta().getDisplayName().equals(ChatColor.translateAlternateColorCodes('&', "&b다음 페이지"))) {
					openGUI(PlayerPage + 1);
				}
			}
		}
	}
	
}
