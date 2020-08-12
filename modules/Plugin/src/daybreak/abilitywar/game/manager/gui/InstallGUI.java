package daybreak.abilitywar.game.manager.gui;

import daybreak.abilitywar.utils.installer.Installer;
import daybreak.abilitywar.utils.installer.Installer.VersionObject;
import daybreak.abilitywar.utils.library.MaterialX;
import daybreak.abilitywar.utils.library.item.ItemBuilder;
import java.util.Arrays;
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

	private static final ItemStack PREVIOUS_PAGE = new ItemBuilder()
			.type(Material.ARROW)
			.displayName(ChatColor.AQUA + "이전 페이지")
			.build();

	private static final ItemStack NEXT_PAGE = new ItemBuilder()
			.type(Material.ARROW)
			.displayName(ChatColor.AQUA + "다음 페이지")
			.build();

	private final Player player;
	private final Plugin plugin;
	private final Installer installer;
	private Inventory gui;

	public InstallGUI(Player player, Plugin plugin, Installer installer) {
		this.player = player;
		this.plugin = plugin;
		this.installer = installer;
		Bukkit.getPluginManager().registerEvents(this, plugin);
	}

	private int playerPage = 1;

	public void openGUI(int page) {
		gui = Bukkit.createInventory(null, 27, "§0§l버전 목록 §0(§8맨 앞이 가장 최신 버전§0)");
		final int maxPage = ((installer.getVersions().size() - 1) / 18) + 1;
		if (maxPage < page || page < 1) page = 1;
		playerPage = page;
		int count = 0;
		for (final VersionObject version : installer.getVersions()) {
			final ItemStack stack;
			if (version.getVersion().equals(plugin.getDescription().getVersion())) {
				stack = MaterialX.ENCHANTED_BOOK.createItem();
				final ItemMeta meta = stack.getItemMeta();
				meta.setDisplayName("§b" + version.getVersion());
				meta.setLore(Arrays.asList(
						"§7현재 플러그인 버전입니다.",
						"§b태그§f: " + version.getTag(),
						"§b버전§f: " + (version.isPrerelease() ? "§3PRE-RELEASE§f " : "" + version.getVersion()),
						"§b파일 크기§f: " + (version.getFileSize() / 1024) + "KB"));
				stack.setItemMeta(meta);
			} else {
				stack = MaterialX.BOOK.createItem();
				final ItemMeta meta = stack.getItemMeta();
				meta.setDisplayName("§b" + version.getVersion());
				meta.setLore(Arrays.asList(
						"§b» §f이 버전을 설치하려면 클릭하세요.",
						"§b태그§f: " + version.getTag(),
						"§b버전§f: " + (version.isPrerelease() ? "§3PRE-RELEASE§f " : "" + version.getVersion()),
						"§b파일 크기§f: " + (version.getFileSize() / 1024) + "KB"));
				stack.setItemMeta(meta);
			}

			if (count / 18 == page - 1) gui.setItem(count % 18, stack);
			count++;
		}

		if (page > 1) gui.setItem(21, PREVIOUS_PAGE);

		if (page != maxPage) gui.setItem(23, NEXT_PAGE);

		final ItemStack stack = new ItemStack(Material.PAPER, 1);
		final ItemMeta meta = stack.getItemMeta();
		meta.setDisplayName("§6페이지 §e" + page + " §6/ §e" + maxPage);
		stack.setItemMeta(meta);
		gui.setItem(22, stack);

		player.openInventory(gui);
	}

	@EventHandler
	private void onInventoryClose(InventoryCloseEvent e) {
		if (e.getInventory().equals(gui)) {
			HandlerList.unregisterAll(this);
		}
	}

	@EventHandler
	private void onQuit(PlayerQuitEvent e) {
		if (e.getPlayer().getUniqueId().equals(player.getUniqueId())) {
			HandlerList.unregisterAll(this);
		}
	}

	@EventHandler
	private void onInventoryClick(InventoryClickEvent e) {
		if (e.getInventory().equals(gui)) {
			e.setCancelled(true);
			if (e.getCurrentItem() != null && e.getCurrentItem().hasItemMeta()
					&& e.getCurrentItem().getItemMeta().hasDisplayName()) {
				final String displayName = e.getCurrentItem().getItemMeta().getDisplayName();
				switch (displayName) {
					case "§b이전 페이지":
						openGUI(playerPage - 1);
						break;
					case "§b다음 페이지":
						openGUI(playerPage + 1);
						break;
					default:
						if (e.getCurrentItem().getType().equals(Material.BOOK)) {
							VersionObject version = installer.getVersion(ChatColor.stripColor(displayName));
							if (version != null) {
								player.closeInventory();
								version.install();
							}
						}
						break;
				}
			}
		}
	}

}