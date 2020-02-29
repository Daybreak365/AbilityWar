package daybreak.abilitywar.game.manager.gui;

import daybreak.abilitywar.game.manager.SpectatorManager;
import daybreak.abilitywar.utils.base.Messager;
import daybreak.abilitywar.utils.library.MaterialX;
import daybreak.abilitywar.utils.library.item.ItemLib;
import daybreak.abilitywar.utils.library.item.ItemLib.ItemColor;
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

import java.util.Set;
import java.util.TreeSet;

/**
 * 관전자 설정 GUI
 */
public class SpectatorGUI implements Listener {

	private final Player p;

	public SpectatorGUI(Player p, Plugin Plugin) {
		this.p = p;
		Bukkit.getPluginManager().registerEvents(this, Plugin);
	}

	private int currentPage = 1;

	private Inventory gui;

	public Set<String> getPlayers() {
		Set<String> list = new TreeSet<>();

		for (Player p : Bukkit.getOnlinePlayers()) {
			list.add(p.getName());
		}
		list.addAll(SpectatorManager.getSpectators());
		return list;
	}

	public void openSpectateGUI(int page) {
		Set<String> Players = getPlayers();
		int MaxPage = ((Players.size() - 1) / 36) + 1;
		if (MaxPage < page)
			page = 1;
		if (page < 1) page = 1;
		gui = Bukkit.createInventory(null, 54, ChatColor.translateAlternateColorCodes('&', "&b플레이어 &f목록"));
		currentPage = page;
		int Count = 0;

		for (String player : Players) {
			ItemStack is;

			if (SpectatorManager.isSpectator(player)) {
				is = ItemLib.WOOL.getItemStack(ItemColor.RED);
				ItemMeta im = is.getItemMeta();
				im.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&b" + player));
				im.setLore(Messager.asList(
						ChatColor.translateAlternateColorCodes('&', "&7이 플레이어는 게임에서 예외됩니다."),
						ChatColor.translateAlternateColorCodes('&', "&b» &f예외 처리를 해제하려면 클릭하세요.")
				));
				is.setItemMeta(im);
			} else {
				is = ItemLib.WOOL.getItemStack(ItemColor.LIME);
				ItemMeta im = is.getItemMeta();
				im.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&b" + player));
				im.setLore(Messager.asList(
						ChatColor.translateAlternateColorCodes('&', "&7이 플레이어는 게임에서 예외되지 않습니다."),
						ChatColor.translateAlternateColorCodes('&', "&b» &f예외 처리를 하려면 클릭하세요.")
				));
				is.setItemMeta(im);
			}

			if (Count / 36 == page - 1) {
				gui.setItem(Count % 36, is);
			}
			Count++;
		}

		if (page > 1) {
			ItemStack previousPage = new ItemStack(Material.ARROW, 1);
			ItemMeta previousMeta = previousPage.getItemMeta();
			previousMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&b이전 페이지"));
			previousPage.setItemMeta(previousMeta);
			gui.setItem(48, previousPage);
		}

		if (page != MaxPage) {
			ItemStack nextPage = new ItemStack(Material.ARROW, 1);
			ItemMeta nextMeta = nextPage.getItemMeta();
			nextMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&b다음 페이지"));
			nextPage.setItemMeta(nextMeta);
			gui.setItem(50, nextPage);
		}

		ItemStack Page = new ItemStack(Material.PAPER, 1);
		ItemMeta PageMeta = Page.getItemMeta();
		PageMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&',
				"&6페이지 &e" + page + " &6/ &e" + MaxPage));
		Page.setItemMeta(PageMeta);
		gui.setItem(49, Page);

		p.openInventory(gui);
	}

	@EventHandler
	private void onInventoryClose(InventoryCloseEvent e) {
		if (e.getInventory().equals(this.gui)) {
			HandlerList.unregisterAll(this);
		}
	}

	@EventHandler
	private void onInventoryClick(InventoryClickEvent e) {
		if (e.getInventory().equals(gui)) {
			e.setCancelled(true);
			Player p = (Player) e.getWhoClicked();
			if (e.getCurrentItem() != null && e.getCurrentItem().hasItemMeta() && e.getCurrentItem().getItemMeta().hasDisplayName()) {
				if (e.getCurrentItem().getItemMeta().getDisplayName().equals(ChatColor.translateAlternateColorCodes('&', "&b이전 페이지"))) {
					openSpectateGUI(currentPage - 1);
				} else if (e.getCurrentItem().getItemMeta().getDisplayName().equals(ChatColor.translateAlternateColorCodes('&', "&b다음 페이지"))) {
					openSpectateGUI(currentPage + 1);
				}
			}

			if (e.getCurrentItem() != null && e.getCurrentItem().getType() != null && ItemLib.WOOL.compareType(e.getCurrentItem().getType())) {
				try {
					if (MaterialX.RED_WOOL.compareType(e.getCurrentItem())) {
						String targetName = ChatColor.stripColor(e.getCurrentItem().getItemMeta().getDisplayName());

						String target = null;

						for (String player : SpectatorManager.getSpectators()) {
							if (player.equals(targetName)) {
								target = player;
							}
						}

						if (target != null) {
							SpectatorManager.removeSpectator(target);

							openSpectateGUI(currentPage);
						} else {
							throw new Exception("해당 플레이어가 존재하지 않습니다.");
						}
					} else if (MaterialX.LIME_WOOL.compareType(e.getCurrentItem())) {
						String target = Bukkit.getPlayerExact(ChatColor.stripColor(e.getCurrentItem().getItemMeta().getDisplayName())).getName();
						if (target != null) {
							SpectatorManager.addSpectator(target);

							openSpectateGUI(currentPage);
						} else {
							throw new Exception("해당 플레이어가 존재하지 않습니다.");
						}
					}
				} catch (Exception ex) {
					if (!ex.getMessage().isEmpty()) {
						Messager.sendErrorMessage(p, ex.getMessage());
					} else {
						Messager.sendErrorMessage(p, "설정 도중 오류가 발생하였습니다.");
					}
				}
			}
		}
	}

}