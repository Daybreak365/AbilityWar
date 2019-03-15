package Marlang.AbilityWar.Game.Manager.GUI;

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
import Marlang.AbilityWar.Game.Games.Game;
import Marlang.AbilityWar.Utils.Messager;
import Marlang.AbilityWar.Utils.Library.Item.ItemLib;
import Marlang.AbilityWar.Utils.Library.Item.ItemLib.ItemColor;
import Marlang.AbilityWar.Utils.Library.Item.MaterialLib;

/**
 * Í¥??†Ñ?ûê ?Ñ§?†ï GUI
 */
public class SpectatorGUI implements Listener {
	
	private Player p;
	
	public SpectatorGUI(Player p, Plugin Plugin) {
		this.p = p;
		Bukkit.getPluginManager().registerEvents(this, Plugin);
	}
	
	private Integer PlayerPage = 1;
	
	private Inventory SpectateGUI;
	
	public List<String> getPlayers() {
		ArrayList<String> list = new ArrayList<String>();
		
		for(Player p : Bukkit.getOnlinePlayers()) {
			if(!list.contains(p.getName())) {
				list.add(p.getName());
			}
		}
		
		for(String p : Game.getSpectators()) {
			if(!list.contains(p)) {
				list.add(p);
			}
		}
		
		list.sort(new Comparator<String>() {
			
			public int compare(String obj1, String obj2) {
				return obj1.compareToIgnoreCase(obj2);
			}
			
		});
		
		return list;
	}
	
	public void openSpectateGUI(Integer page) {
		List<String> Players = getPlayers();
		Integer MaxPage = ((AbilityList.nameValues().size() - 1) / 36) + 1;MaxPage = 100;
		if (MaxPage < page)
			page = 1;
		if(page < 1) page = 1;
		SpectateGUI = Bukkit.createInventory(null, 54, ChatColor.translateAlternateColorCodes('&', "&b?îå?†à?ù¥?ñ¥ &fÎ™©Î°ù"));
		PlayerPage = page;
		int Count = 0;
		
		for(String player : Players) {
			ItemStack is;
			
			if(Game.getSpectators().contains(player)) {
				is = ItemLib.WOOL.getItemStack(ItemColor.RED);
				ItemMeta im = is.getItemMeta();
				im.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&b" + player));
				im.setLore(Messager.getStringList(
						ChatColor.translateAlternateColorCodes('&', "&7?ù¥ ?îå?†à?ù¥?ñ¥?äî Í≤åÏûÑ?óê?Ñú ?òà?ô∏?ê©?ãà?ã§."),
						ChatColor.translateAlternateColorCodes('&', "&b¬ª &f?òà?ô∏ Ï≤òÎ¶¨Î•? ?ï¥?†ú?ïò?†§Î©? ?Å¥Î¶??ïò?Ñ∏?öî.")
						));
				is.setItemMeta(im);
			} else {
				is = ItemLib.WOOL.getItemStack(ItemColor.LIME);
				ItemMeta im = is.getItemMeta();
				im.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&b" + player));
				im.setLore(Messager.getStringList(
						ChatColor.translateAlternateColorCodes('&', "&7?ù¥ ?îå?†à?ù¥?ñ¥?äî Í≤åÏûÑ?óê?Ñú ?òà?ô∏?êòÏß? ?ïä?äµ?ãà?ã§."),
						ChatColor.translateAlternateColorCodes('&', "&b¬ª &f?òà?ô∏ Ï≤òÎ¶¨Î•? ?ïò?†§Î©? ?Å¥Î¶??ïò?Ñ∏?öî.")
						));
				is.setItemMeta(im);
			}
			
			if (Count / 36 == page - 1) {
				SpectateGUI.setItem(Count % 36, is);
			}
			Count++;
		}
		
		if(page > 1) {
			ItemStack previousPage = new ItemStack(Material.ARROW, 1);
			ItemMeta previousMeta = previousPage.getItemMeta();
			previousMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&b?ù¥?†Ñ ?éò?ù¥Ïß?"));
			previousPage.setItemMeta(previousMeta);
			SpectateGUI.setItem(48, previousPage);
		}
		
		if(page != MaxPage) {
			ItemStack nextPage = new ItemStack(Material.ARROW, 1);
			ItemMeta nextMeta = nextPage.getItemMeta();
			nextMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&b?ã§?ùå ?éò?ù¥Ïß?"));
			nextPage.setItemMeta(nextMeta);
			SpectateGUI.setItem(50, nextPage);
		}

		ItemStack Page = new ItemStack(Material.PAPER, 1);
		ItemMeta PageMeta = Page.getItemMeta();
		PageMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&',
				"&6?éò?ù¥Ïß? &e" + page + " &6/ &e" + MaxPage));
		Page.setItemMeta(PageMeta);
		SpectateGUI.setItem(49, Page);
		
		p.openInventory(SpectateGUI);
	}
	
	@EventHandler
	public void onInventoryClose(InventoryCloseEvent e) {
		if(e.getInventory().equals(this.SpectateGUI)) {
			HandlerList.unregisterAll(this);
		}
	}
	
	@EventHandler
	public void onInventoryClick(InventoryClickEvent e) {
		if(e.getInventory().equals(SpectateGUI)) {
			e.setCancelled(true);
			Player p = (Player) e.getWhoClicked();
			if(e.getCurrentItem() != null && e.getCurrentItem().hasItemMeta() && e.getCurrentItem().getItemMeta().hasDisplayName()) {
				if(e.getCurrentItem().getItemMeta().getDisplayName().equals(ChatColor.translateAlternateColorCodes('&', "&b?ù¥?†Ñ ?éò?ù¥Ïß?"))) {
					openSpectateGUI(PlayerPage - 1);
				} else if(e.getCurrentItem().getItemMeta().getDisplayName().equals(ChatColor.translateAlternateColorCodes('&', "&b?ã§?ùå ?éò?ù¥Ïß?"))) {
					openSpectateGUI(PlayerPage + 1);
				}
			}
			
			if(e.getCurrentItem() != null && e.getCurrentItem().getType() != null && ItemLib.WOOL.compareType(e.getCurrentItem().getType())) {
				try {
					if(MaterialLib.RED_WOOL.compareMaterial(e.getCurrentItem())) {
						String targetName = ChatColor.stripColor(e.getCurrentItem().getItemMeta().getDisplayName());
						
						String target = null;
						
						for(String player : Game.getSpectators()) {
							if(player.equals(targetName)) {
								target = player;
							}
						}
						
						if(target != null) {
							if(Game.getSpectators().contains(target)) {
								Game.getSpectators().remove(target);
							}
							
							openSpectateGUI(PlayerPage);
						} else {
							throw new Exception("?ï¥?ãπ ?îå?†à?ù¥?ñ¥Í∞? Ï°¥Ïû¨?ïòÏß? ?ïä?äµ?ãà?ã§.");
						}
					} else if(MaterialLib.LIME_WOOL.compareMaterial(e.getCurrentItem())) {
						String target = Bukkit.getPlayerExact(ChatColor.stripColor(e.getCurrentItem().getItemMeta().getDisplayName())).getName();
						if(target != null) {
							if(!Game.getSpectators().contains(target)) {
								Game.getSpectators().add(target);
							}
							
							openSpectateGUI(PlayerPage);
						} else {
							throw new Exception("?ï¥?ãπ ?îå?†à?ù¥?ñ¥Í∞? Ï°¥Ïû¨?ïòÏß? ?ïä?äµ?ãà?ã§.");
						}
					}
				} catch(Exception ex) {
					if(!ex.getMessage().isEmpty()) {
						Messager.sendErrorMessage(p, ex.getMessage());
					} else {
						Messager.sendErrorMessage(p, "?Ñ§?†ï ?èÑÏ§? ?ò§Î•òÍ? Î∞úÏÉù?ïò???äµ?ãà?ã§.");
					}
				}
			}
		}
	}
	
}
