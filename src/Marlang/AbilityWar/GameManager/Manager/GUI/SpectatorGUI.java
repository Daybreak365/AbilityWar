package Marlang.AbilityWar.GameManager.Manager.GUI;

import java.util.ArrayList;
import java.util.Comparator;

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
import Marlang.AbilityWar.GameManager.Game;
import Marlang.AbilityWar.Utils.Messager;

/**
 * 관전자 설정 GUI
 */
public class SpectatorGUI implements Listener {
	
	Player p;
	
	public SpectatorGUI(Player p, Plugin Plugin) {
		this.p = p;
		Bukkit.getPluginManager().registerEvents(this, Plugin);
	}
	
	Integer PlayerPage = 1;
	
	Inventory SpectateGUI;
	
	public ArrayList<String> getPlayers() {
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
		if ((AbilityList.values().size() - 1) / 36 + 1 < page)
			page = 1;
		if(page < 1) page = 1;
		SpectateGUI = Bukkit.createInventory(null, 54, ChatColor.translateAlternateColorCodes('&', "&b플레이어 &f목록"));
		PlayerPage = page;
		int Count = 0;
		Integer MaxPage = ((AbilityList.values().size() - 1) / 36) + 1;
		
		for(String player : getPlayers()) {
			ItemStack is;
			
			if(Game.getSpectators().contains(player)) {
				is = new ItemStack(Material.WOOL, 1, (short) 14);
				ItemMeta im = is.getItemMeta();
				im.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&b" + player));
				im.setLore(Messager.getStringList(
						ChatColor.translateAlternateColorCodes('&', "&7이 플레이어는 게임에서 예외됩니다."),
						ChatColor.translateAlternateColorCodes('&', "&b» &f예외 처리를 해제하려면 클릭하세요.")
						));
				is.setItemMeta(im);
			} else {
				is = new ItemStack(Material.WOOL, 1, (short) 5);
				ItemMeta im = is.getItemMeta();
				im.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&b" + player));
				im.setLore(Messager.getStringList(
						ChatColor.translateAlternateColorCodes('&', "&7이 플레이어는 게임에서 예외되지 않습니다."),
						ChatColor.translateAlternateColorCodes('&', "&b» &f예외 처리를 하려면 클릭하세요.")
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
			previousMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&b이전 페이지"));
			previousPage.setItemMeta(previousMeta);
			SpectateGUI.setItem(48, previousPage);
		}
		
		if(page != MaxPage) {
			ItemStack nextPage = new ItemStack(Material.ARROW, 1);
			ItemMeta nextMeta = nextPage.getItemMeta();
			nextMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&b다음 페이지"));
			nextPage.setItemMeta(nextMeta);
			SpectateGUI.setItem(50, nextPage);
		}

		ItemStack Page = new ItemStack(Material.PAPER, 1);
		ItemMeta PageMeta = Page.getItemMeta();
		PageMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&',
				"&6페이지 &e" + page + " &6/ &e" + MaxPage));
		Page.setItemMeta(PageMeta);
		SpectateGUI.setItem(49, Page);
		
		p.openInventory(SpectateGUI);
	}

	@EventHandler
	public void onInventoryClose(InventoryCloseEvent e) {
		if(e.getInventory().equals(this.SpectateGUI)) {
			HandlerList.unregisterAll(this);
			Bukkit.broadcastMessage("Unregistered!");
		}
	}
	
	@EventHandler
	public void onInventoryClick(InventoryClickEvent e) {
		if(e.getInventory().equals(SpectateGUI)) {
			e.setCancelled(true);
			Player p = (Player) e.getWhoClicked();
			if(e.getCurrentItem() != null && e.getCurrentItem().hasItemMeta() && e.getCurrentItem().getItemMeta().hasDisplayName()) {
				if(e.getCurrentItem().getItemMeta().getDisplayName().equals(ChatColor.translateAlternateColorCodes('&', "&b이전 페이지"))) {
					openSpectateGUI(PlayerPage - 1);
				} else if(e.getCurrentItem().getItemMeta().getDisplayName().equals(ChatColor.translateAlternateColorCodes('&', "&b다음 페이지"))) {
					openSpectateGUI(PlayerPage + 1);
				}
			}
			
			if(e.getCurrentItem() != null && e.getCurrentItem().getType() != null && e.getCurrentItem().getType().equals(Material.WOOL)) {
				try {
					if(e.getCurrentItem().getDurability() == (short) 14) {
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
							throw new Exception("해당 플레이어가 존재하지 않습니다.");
						}
					} else if(e.getCurrentItem().getDurability() == (short) 5) {
						String target = Bukkit.getPlayerExact(ChatColor.stripColor(e.getCurrentItem().getItemMeta().getDisplayName())).getName();
						if(target != null) {
							if(!Game.getSpectators().contains(target)) {
								Game.getSpectators().add(target);
							}
							
							openSpectateGUI(PlayerPage);
						} else {
							throw new Exception("해당 플레이어가 존재하지 않습니다.");
						}
					}
				} catch(Exception ex) {
					if(!ex.getMessage().isEmpty()) {
						Messager.sendErrorMessage(p, ex.getMessage());
					} else {
						Messager.sendErrorMessage(p, "설정 도중 오류가 발생하였습니다.");
					}
				}
			}
		}
	}
	
}
