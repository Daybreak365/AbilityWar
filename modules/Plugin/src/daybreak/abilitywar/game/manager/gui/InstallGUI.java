package daybreak.abilitywar.game.manager.gui;

import daybreak.abilitywar.utils.installer.Installer;
import daybreak.abilitywar.utils.installer.Installer.UpdateObject;
import daybreak.abilitywar.utils.installer.Version;
import daybreak.abilitywar.utils.library.MaterialX;
import java.util.Arrays;
import java.util.Map;
import java.util.Map.Entry;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;

public class InstallGUI implements Listener {

	private final Player p;
	private final Installer installer;
	private final Map<Version, UpdateObject> versions;
	private Inventory gui;

	public InstallGUI(Player p, Plugin plugin, Installer installer) {
		this.p = p;
		this.installer = installer;
		this.versions = installer.getVersions();
		Bukkit.getPluginManager().registerEvents(this, plugin);
	}

	private int playerPage = 1;

	public void openGUI(int page) {
		gui = Bukkit.createInventory(null, 27, "§0§l버전 목록");
		int maxPage = ((versions.size() - 1) / 18) + 1;
		if (maxPage < page || page < 1) page = 1;
		playerPage = page;
		int count = 0;
		for (Entry<Version, UpdateObject> entry : versions.entrySet()) {
			Version version = entry.getKey();
			UpdateObject update = entry.getValue();
			ItemStack is;
			if (installer.getPluginVersion().equals(version)) {
				is = MaterialX.ENCHANTED_BOOK.parseItem();
				ItemMeta im = is.getItemMeta();
				im.setDisplayName("§b" + version.getVersionString());
				im.setLore(Arrays.asList(
						"§7현재 플러그인 버전입니다.",
						"§b태그§f: " + update.getTag(),
						"§b버전§f: " + (update.isPrerelease() ? "§3PRE-RELEASE§f " : "" + update.getVersion()),
						"§b파일 크기§f: " + (update.getFileSize() / 1024) + "KB",
						"§b다운로드 횟수§f: " + update.getDownloadCount()));
				is.setItemMeta(im);
			} else {
				is = MaterialX.BOOK.parseItem();
				ItemMeta im = is.getItemMeta();
				im.setDisplayName("§b" + version.getVersionString());
				im.setLore(Arrays.asList(
						"§b» §f이 버전을 설치하려면 클릭하세요.",
						"§b태그§f: " + update.getTag(),
						"§b버전§f: " + (update.isPrerelease() ? "§3PRE-RELEASE§f " : "" + update.getVersion()),
						"§b파일 크기§f: " + (update.getFileSize() / 1024) + "KB",
						"§b다운로드 횟수§f: " + update.getDownloadCount()));
				is.setItemMeta(im);
			}

			if (count / 18 == page - 1) gui.setItem(count % 18, is);
			count++;
		}

		if (page > 1) {
			ItemStack previousPage = new ItemStack(Material.ARROW, 1);
			ItemMeta previousMeta = previousPage.getItemMeta();
			previousMeta.setDisplayName(ChatColor.AQUA + "이전 페이지");
			previousPage.setItemMeta(previousMeta);
			gui.setItem(21, previousPage);
		}

		if (page != maxPage) {
			ItemStack nextPage = new ItemStack(Material.ARROW, 1);
			ItemMeta nextMeta = nextPage.getItemMeta();
			nextMeta.setDisplayName(ChatColor.AQUA + "다음 페이지");
			nextPage.setItemMeta(nextMeta);
			gui.setItem(23, nextPage);
		}

		ItemStack Page = new ItemStack(Material.PAPER, 1);
		ItemMeta PageMeta = Page.getItemMeta();
		PageMeta.setDisplayName("§6페이지 §e" + page + " §6/ §e" + maxPage);
		Page.setItemMeta(PageMeta);
		gui.setItem(22, Page);

		p.openInventory(gui);
	}

	@EventHandler
	private void onInventoryClose(InventoryCloseEvent e) {
		if (e.getInventory().equals(gui)) {
			HandlerList.unregisterAll(this);
		}
	}

	@EventHandler
	private void onQuit(PlayerQuitEvent e) {
		if (e.getPlayer().getUniqueId().equals(p.getUniqueId())) {
			HandlerList.unregisterAll(this);
		}
	}

	@EventHandler
	private void onInventoryClick(InventoryClickEvent e) {
		if (e.getInventory().equals(gui)) {
			e.setCancelled(true);
			if (e.getCurrentItem() != null && e.getCurrentItem().hasItemMeta()
					&& e.getCurrentItem().getItemMeta().hasDisplayName()) {
				String displayName = e.getCurrentItem().getItemMeta().getDisplayName();
				switch (displayName) {
					case "§b이전 페이지":
						openGUI(playerPage - 1);
						break;
					case "§b다음 페이지":
						openGUI(playerPage + 1);
						break;
					default:
						if (e.getCurrentItem().getType().equals(Material.BOOK)) {
							String strip = ChatColor.stripColor(displayName);
							Version version = installer.getVersion(strip);
							if (version != null) {
								p.closeInventory();
								installer.Install(p, versions.get(version));
							}
						}
						break;
				}
			}
		}
	}

}