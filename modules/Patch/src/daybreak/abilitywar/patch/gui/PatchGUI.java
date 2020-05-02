package daybreak.abilitywar.patch.gui;

import daybreak.abilitywar.patch.IPatch;
import daybreak.abilitywar.patch.Patches;
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

public class PatchGUI implements Listener {

	private static final ItemStack PREVIOUS_PAGE = new ItemStack(Material.ARROW) {
		{
			ItemMeta meta = getItemMeta();
			meta.setDisplayName(ChatColor.AQUA + "이전 페이지");
			setItemMeta(meta);
		}
	};

	private static final ItemStack NEXT_PAGE = new ItemStack(Material.ARROW) {
		{
			ItemMeta meta = getItemMeta();
			meta.setDisplayName(ChatColor.AQUA + "다음 페이지");
			setItemMeta(meta);
		}
	};

	private final Player player;
	private int currentPage = 1;
	private Inventory gui;

	public PatchGUI(Player player, Plugin plugin) {
		this.player = player;
		Bukkit.getPluginManager().registerEvents(this, plugin);
	}

	public void openGUI(int page) {
		int maxPage = ((Patches.patches.size() - 1) / 36) + 1;
		if (maxPage < page) page = 1;
		if (page < 1) page = 1;
		gui = Bukkit.createInventory(null, 54, ChatColor.translateAlternateColorCodes('&', "&cAbilityWar &e능력 목록"));
		currentPage = page;

		int count = 0;
		for (IPatch patch : Patches.patches) {
			if (patch.condition() && count / 36 == page - 1) {
				ItemStack stack = new ItemStack(patch.isApplied() ? Material.IRON_BLOCK : Material.IRON_BLOCK);
				ItemMeta meta = stack.getItemMeta();
				meta.setDisplayName(ChatColor.AQUA + patch.getName());
				stack.setItemMeta(meta);
				gui.setItem(count % 36, stack);
			}
			count++;
		}

		if (page > 1) {
			gui.setItem(48, PREVIOUS_PAGE);
		}

		if (page != maxPage) {
			gui.setItem(50, NEXT_PAGE);
		}

		ItemStack stack = new ItemStack(Material.PAPER, 1);
		ItemMeta meta = stack.getItemMeta();
		meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&6페이지 &e" + page + " &6/ &e" + maxPage));
		stack.setItemMeta(meta);
		gui.setItem(49, stack);

		player.openInventory(gui);
	}

	@EventHandler
	private void onInventoryClose(InventoryCloseEvent e) {
		if (e.getInventory().equals(this.gui)) {
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
			if (e.getCurrentItem() != null && e.getCurrentItem().hasItemMeta() && e.getCurrentItem().getItemMeta().hasDisplayName() && e.getCurrentItem().getType() != Material.IRON_BLOCK) {
				String displayName = ChatColor.stripColor(e.getCurrentItem().getItemMeta().getDisplayName());
				if (displayName.equals("이전 페이지")) {
					openGUI(currentPage - 1);
				} else if (displayName.equals("다음 페이지")) {
					openGUI(currentPage + 1);
				}
			}
		}
	}

}
