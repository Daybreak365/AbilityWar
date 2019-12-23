package daybreak.abilitywar.game.manager.gui;

import daybreak.abilitywar.config.AbilityWarSettings;
import daybreak.abilitywar.config.AbilityWarSettings.Settings;
import daybreak.abilitywar.config.enums.ConfigNodes;
import daybreak.abilitywar.game.games.mode.AbstractGame;
import daybreak.abilitywar.game.games.mode.GameManifest;
import daybreak.abilitywar.game.manager.GameMode;
import daybreak.abilitywar.utils.Messager;
import daybreak.abilitywar.utils.library.item.MaterialLib;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.InvalidConfigurationException;
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

import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

public class GameModeGUI implements Listener {

	private static final Logger logger = Logger.getLogger(GameModeGUI.class.getName());

	private final Player p;
	
	public GameModeGUI(Player p, Plugin Plugin) {
		this.p = p;
		Bukkit.getPluginManager().registerEvents(this, Plugin);
	}

	private int PlayerPage = 1;
	
	private Inventory GameModeGUI;
	
	public void openGameModeGUI(int page) {
		int MaxPage = ((GameMode.nameValues().size() - 1) / 18) + 1;
		if (MaxPage < page) page = 1;
		if(page < 1) page = 1;
		GameModeGUI = Bukkit.createInventory(null, 27, ChatColor.translateAlternateColorCodes('&', "&cAbilityWar &8게임 모드"));
		PlayerPage = page;
		int Count = 0;
		
		Class<? extends AbstractGame> gameClass = Settings.getGameMode();
		
		for(String name : GameMode.nameValues()) {
			Class<? extends AbstractGame> mode = GameMode.getByString(name);
			
			if(mode != null) {
				GameManifest manifest = mode.getAnnotation(GameManifest.class);
				if(manifest != null) {
					ItemStack is;
					
					if(gameClass.equals(mode)) {
						is = MaterialLib.ENCHANTED_BOOK.getItem();
						ItemMeta im = is.getItemMeta();
						im.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&b" + name));
						ArrayList<String> lore = Messager.asList(manifest.Description());
						lore.add(ChatColor.translateAlternateColorCodes('&', "&7선택된 게임모드입니다."));
						im.setLore(lore);
						is.setItemMeta(im);
					} else {
						is = MaterialLib.BOOK.getItem();
						ItemMeta im = is.getItemMeta();
						im.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&b" + name));
						ArrayList<String> lore = Messager.asList(manifest.Description());
						lore.add(ChatColor.translateAlternateColorCodes('&', "&b» &f이 게임모드를 선택하려면 클릭하세요."));
						im.setLore(lore);
						is.setItemMeta(im);
					}

					if (Count / 18 == page - 1) {
						GameModeGUI.setItem(Count % 18, is);
					}
					Count++;
				}
			}
		}
		
		if(page > 1) {
			ItemStack previousPage = new ItemStack(Material.ARROW, 1);
			ItemMeta previousMeta = previousPage.getItemMeta();
			previousMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&b이전 페이지"));
			previousPage.setItemMeta(previousMeta);
			GameModeGUI.setItem(21, previousPage);
		}
		
		if(page != MaxPage) {
			ItemStack nextPage = new ItemStack(Material.ARROW, 1);
			ItemMeta nextMeta = nextPage.getItemMeta();
			nextMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&b다음 페이지"));
			nextPage.setItemMeta(nextMeta);
			GameModeGUI.setItem(23, nextPage);
		}

		ItemStack Page = new ItemStack(Material.PAPER, 1);
		ItemMeta PageMeta = Page.getItemMeta();
		PageMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&',
				"&6페이지 &e" + page + " &6/ &e" + MaxPage));
		Page.setItemMeta(PageMeta);
		GameModeGUI.setItem(22, Page);
		
		p.openInventory(GameModeGUI);
	}

	@EventHandler
	private void onInventoryClose(InventoryCloseEvent e) {
		if (e.getInventory().equals(this.GameModeGUI)) {
			HandlerList.unregisterAll(this);
			try {
				AbilityWarSettings.update();
			} catch (IOException | InvalidConfigurationException e1) {
				logger.log(Level.SEVERE, "콘피그를 업데이트하는 도중 오류가 발생하였습니다.");
			}
		}
	}
	
	@EventHandler
	private void onInventoryClick(InventoryClickEvent e) {
		if(e.getInventory().equals(GameModeGUI)) {
			e.setCancelled(true);
			if(e.getCurrentItem() != null && e.getCurrentItem().hasItemMeta() && e.getCurrentItem().getItemMeta().hasDisplayName()) {
				if(e.getCurrentItem().getItemMeta().getDisplayName().equals(ChatColor.translateAlternateColorCodes('&', "&b이전 페이지"))) {
					openGameModeGUI(PlayerPage - 1);
				} else if(e.getCurrentItem().getItemMeta().getDisplayName().equals(ChatColor.translateAlternateColorCodes('&', "&b다음 페이지"))) {
					openGameModeGUI(PlayerPage + 1);
				}
				
				if(e.getCurrentItem().getType().equals(Material.BOOK)) {
					if(e.getCurrentItem().hasItemMeta() && e.getCurrentItem().getItemMeta().hasDisplayName()) {
						String modeName = ChatColor.stripColor(e.getCurrentItem().getItemMeta().getDisplayName());
						Class<? extends AbstractGame> gameMode = GameMode.getByString(modeName);
						if(gameMode != null) {
							AbilityWarSettings.modifyProperty(ConfigNodes.GAME_MODE, gameMode.getName());
						} else {
							Messager.sendErrorMessage(p, ChatColor.translateAlternateColorCodes('&', "&c" + modeName + " &f클래스는 등록되지 않았습니다."));
						}
						
						openGameModeGUI(PlayerPage);
					}
				}
			}
		}
	}
	
}
