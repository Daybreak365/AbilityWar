package Marlang.AbilityWar.GameManager.Manager.GUI;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;

import Marlang.AbilityWar.Ability.AbilityList;
import Marlang.AbilityWar.Config.AbilityWarSettings;
import Marlang.AbilityWar.Utils.Messager;
import Marlang.AbilityWar.Utils.Library.SoundLib;

/**
 * 능력 금지 GUI
 */
public class BlackListGUI implements Listener {
	
	Player p;
	
	public BlackListGUI(Player p, Plugin Plugin) {
		this.p = p;
		Bukkit.getPluginManager().registerEvents(this, Plugin);
	}
	
	Integer PlayerPage = 1;
	
	Inventory BlackListGUI;
	
	public ArrayList<String> getBlackList() {
		ArrayList<String> list = new ArrayList<String>();
		
		for(String name : AbilityList.values()) {
			if(!list.contains(name)) {
				list.add(name);
			}
		}
		
		for(String name : AbilityWarSettings.getBlackList()) {
			if(!list.contains(name)) {
				list.add(name);
			}
		}
		
		list.sort(new Comparator<String>() {
			
			public int compare(String obj1, String obj2) {
				return obj1.compareToIgnoreCase(obj2);
			}
			
		});
		
		return list;
	}
	
	public void openBlackListGUI(Integer page) {
		if ((AbilityList.values().size() - 1) / 36 + 1 < page)
			page = 1;
		if(page < 1) page = 1;
		BlackListGUI = Bukkit.createInventory(null, 54, ChatColor.translateAlternateColorCodes('&', "&c&l✖ &8&l능력 블랙리스트 &c&l✖"));
		PlayerPage = page;
		int Count = 0;
		Integer MaxPage = ((AbilityList.values().size() - 1) / 36) + 1;
		
		List<String> blackList = AbilityWarSettings.getBlackList();
		
		for(String name : getBlackList()) {
			ItemStack is;
			
			if(blackList.contains(name)) {
				is = new ItemStack(Material.WOOL, 1, (short) 14);
				ItemMeta im = is.getItemMeta();
				im.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&b" + name));
				im.setLore(Messager.getStringList(
						ChatColor.translateAlternateColorCodes('&', "&7이 능력은 능력을 추첨할 때 예외됩니다."),
						ChatColor.translateAlternateColorCodes('&', "&b» &f예외 처리를 해제하려면 클릭하세요.")
						));
				is.setItemMeta(im);
			} else {
				is = new ItemStack(Material.WOOL, 1, (short) 5);
				ItemMeta im = is.getItemMeta();
				im.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&b" + name));
				im.setLore(Messager.getStringList(
						ChatColor.translateAlternateColorCodes('&', "&7이 능력은 능력을 추첨할 때 예외되지 않습니다."),
						ChatColor.translateAlternateColorCodes('&', "&b» &f예외 처리를 하려면 클릭하세요.")
						));
				is.setItemMeta(im);
			}
			
			if (Count / 36 == page - 1) {
				BlackListGUI.setItem(Count % 36, is);
			}
			Count++;
		}
		
		if(page > 1) {
			ItemStack previousPage = new ItemStack(Material.ARROW, 1);
			ItemMeta previousMeta = previousPage.getItemMeta();
			previousMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&b이전 페이지"));
			previousPage.setItemMeta(previousMeta);
			BlackListGUI.setItem(48, previousPage);
		}
		
		if(page != MaxPage) {
			ItemStack nextPage = new ItemStack(Material.ARROW, 1);
			ItemMeta nextMeta = nextPage.getItemMeta();
			nextMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&b다음 페이지"));
			nextPage.setItemMeta(nextMeta);
			BlackListGUI.setItem(50, nextPage);
		}

		ItemStack Page = new ItemStack(Material.PAPER, 1);
		ItemMeta PageMeta = Page.getItemMeta();
		PageMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&',
				"&6페이지 &e" + page + " &6/ &e" + MaxPage));
		Page.setItemMeta(PageMeta);
		BlackListGUI.setItem(49, Page);
		
		p.openInventory(BlackListGUI);
	}

	@EventHandler
	public void onInventoryClose(InventoryCloseEvent e) {
		if(e.getInventory().equals(this.BlackListGUI)) {
			HandlerList.unregisterAll(this);
		}
	}
	
	@EventHandler
	public void onInventoryClick(InventoryClickEvent e) {
		if(e.getInventory().equals(BlackListGUI)) {
			e.setCancelled(true);
			if(e.getCurrentItem() != null && e.getCurrentItem().hasItemMeta() && e.getCurrentItem().getItemMeta().hasDisplayName()) {
				if(e.getCurrentItem().getItemMeta().getDisplayName().equals(ChatColor.translateAlternateColorCodes('&', "&b이전 페이지"))) {
					openBlackListGUI(PlayerPage - 1);
				} else if(e.getCurrentItem().getItemMeta().getDisplayName().equals(ChatColor.translateAlternateColorCodes('&', "&b다음 페이지"))) {
					openBlackListGUI(PlayerPage + 1);
				}
			}
			
			if(e.getCurrentItem() != null && e.getCurrentItem().getType() != null && e.getCurrentItem().getType().equals(Material.WOOL)) {
				if(e.getCurrentItem().getDurability() == (short) 14) {
					String abiName = ChatColor.stripColor(e.getCurrentItem().getItemMeta().getDisplayName());
					
					AbilityWarSettings.removeBlackList(abiName);
					SoundLib.ENTITY_EXPERIENCE_ORB_PICKUP.playSound(p);
					openBlackListGUI(PlayerPage);
				} else if(e.getCurrentItem().getDurability() == (short) 5) {
					String abiName = ChatColor.stripColor(e.getCurrentItem().getItemMeta().getDisplayName());
					
					AbilityWarSettings.addBlackList(abiName);
					SoundLib.BLOCK_ANVIL_LAND.playSound(p);
					openBlackListGUI(PlayerPage);
				}
			}
		}
	}
	
}
