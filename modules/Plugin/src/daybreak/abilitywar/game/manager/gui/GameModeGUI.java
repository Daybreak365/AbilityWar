package daybreak.abilitywar.game.manager.gui;

import daybreak.abilitywar.config.Configuration;
import daybreak.abilitywar.config.Configuration.Settings;
import daybreak.abilitywar.config.enums.ConfigNodes;
import daybreak.abilitywar.game.games.mode.AbstractGame;
import daybreak.abilitywar.game.games.mode.GameManifest;
import daybreak.abilitywar.game.manager.GameMode;
import daybreak.abilitywar.utils.base.Messager;
import daybreak.abilitywar.utils.library.MaterialX;
import daybreak.abilitywar.utils.library.item.ItemBuilder;
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
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class GameModeGUI implements Listener {

	private static final ItemStack PREVIOUS_PAGE = new ItemBuilder()
			.type(Material.ARROW)
			.displayName(ChatColor.translateAlternateColorCodes('&', "&b이전 페이지"))
			.build();

	private static final ItemStack NEXT_PAGE = new ItemBuilder()
			.type(Material.ARROW)
			.displayName(ChatColor.translateAlternateColorCodes('&', "&b다음 페이지"))
			.build();

	private static final Logger logger = Logger.getLogger(GameModeGUI.class.getName());

	private final Player p;

	public GameModeGUI(Player p, Plugin Plugin) {
		this.p = p;
		Bukkit.getPluginManager().registerEvents(this, Plugin);
	}

	private int PlayerPage = 1;

	private Inventory gui;

	public void openGUI(int page) {
		int MaxPage = ((GameMode.nameValues().size() - 1) / 18) + 1;
		if (MaxPage < page) page = 1;
		if (page < 1) page = 1;
		gui = Bukkit.createInventory(null, 27, ChatColor.translateAlternateColorCodes('&', "&cAbilityWar &8게임 모드"));
		PlayerPage = page;
		int Count = 0;

		Class<? extends AbstractGame> gameClass = Settings.getGameMode();

		for (String name : GameMode.nameValues()) {
			Class<? extends AbstractGame> mode = GameMode.getByName(name);

			if (mode != null) {
				GameManifest manifest = mode.getAnnotation(GameManifest.class);
				if (manifest != null) {
					ItemStack is;

					if (gameClass.equals(mode)) {
						is = MaterialX.ENCHANTED_BOOK.parseItem();
						ItemMeta im = is.getItemMeta();
						im.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&b" + name));
						List<String> lore = Messager.asList(manifest.Description());
						lore.add(ChatColor.translateAlternateColorCodes('&', "&7선택된 게임모드입니다."));
						im.setLore(lore);
						is.setItemMeta(im);
					} else {
						is = MaterialX.BOOK.parseItem();
						ItemMeta im = is.getItemMeta();
						im.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&b" + name));
						List<String> lore = Messager.asList(manifest.Description());
						lore.add(ChatColor.translateAlternateColorCodes('&', "&b» &f이 게임모드를 선택하려면 클릭하세요."));
						im.setLore(lore);
						is.setItemMeta(im);
					}

					if (Count / 18 == page - 1) {
						gui.setItem(Count % 18, is);
					}
					Count++;
				}
			}
		}

		if (page > 1) gui.setItem(21, PREVIOUS_PAGE);

		if (page != MaxPage) gui.setItem(23, NEXT_PAGE);

		ItemStack stack = new ItemStack(Material.PAPER, 1);
		ItemMeta meta = stack.getItemMeta();
		meta.setDisplayName(ChatColor.translateAlternateColorCodes('&',
				"&6페이지 &e" + page + " &6/ &e" + MaxPage));
		stack.setItemMeta(meta);
		gui.setItem(22, stack);

		p.openInventory(gui);
	}

	@EventHandler
	private void onInventoryClose(InventoryCloseEvent e) {
		if (e.getInventory().equals(this.gui)) {
			HandlerList.unregisterAll(this);
			try {
				Configuration.update();
			} catch (IOException | InvalidConfigurationException e1) {
				logger.log(Level.SEVERE, "콘피그를 업데이트하는 도중 오류가 발생하였습니다.");
			}
		}
	}

	@EventHandler
	private void onInventoryClick(InventoryClickEvent e) {
		if (e.getInventory().equals(gui)) {
			e.setCancelled(true);
			if (e.getCurrentItem() != null && e.getCurrentItem().hasItemMeta() && e.getCurrentItem().getItemMeta().hasDisplayName()) {
				if (e.getCurrentItem().getItemMeta().getDisplayName().equals(ChatColor.translateAlternateColorCodes('&', "&b이전 페이지"))) {
					openGUI(PlayerPage - 1);
				} else if (e.getCurrentItem().getItemMeta().getDisplayName().equals(ChatColor.translateAlternateColorCodes('&', "&b다음 페이지"))) {
					openGUI(PlayerPage + 1);
				}

				if (e.getCurrentItem().getType().equals(Material.BOOK)) {
					if (e.getCurrentItem().hasItemMeta() && e.getCurrentItem().getItemMeta().hasDisplayName()) {
						String modeName = ChatColor.stripColor(e.getCurrentItem().getItemMeta().getDisplayName());
						Class<? extends AbstractGame> gameMode = GameMode.getByName(modeName);
						if (gameMode != null) {
							Configuration.modifyProperty(ConfigNodes.GAME_MODE, gameMode.getName());
						} else {
							Messager.sendErrorMessage(p, ChatColor.translateAlternateColorCodes('&', "&c" + modeName + " &f클래스는 등록되지 않았습니다."));
						}

						openGUI(PlayerPage);
					}
				}
			}
		}
	}

}
