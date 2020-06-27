package daybreak.abilitywar.game.manager.gui;

import daybreak.abilitywar.game.manager.SpectatorManager;
import daybreak.abilitywar.utils.base.Messager;
import daybreak.abilitywar.utils.library.MaterialX;
import daybreak.abilitywar.utils.library.item.ItemBuilder;
import daybreak.abilitywar.utils.library.item.ItemLib;
import daybreak.abilitywar.utils.library.item.ItemLib.ItemColor;
import java.util.Set;
import java.util.TreeSet;
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

/**
 * 관전자 설정 GUI
 */
public class SpectatorGUI implements Listener {

	private static final ItemStack PREVIOUS_PAGE = new ItemBuilder()
			.type(Material.ARROW)
			.displayName(ChatColor.AQUA + "이전 페이지")
			.build();

	private static final ItemStack NEXT_PAGE = new ItemBuilder()
			.type(Material.ARROW)
			.displayName(ChatColor.AQUA + "다음 페이지")
			.build();

	private final Player p;

	public SpectatorGUI(Player p, Plugin Plugin) {
		this.p = p;
		Bukkit.getPluginManager().registerEvents(this, Plugin);
	}

	private int currentPage = 1;

	private Inventory gui;

	private Set<String> getPlayers() {
		Set<String> list = new TreeSet<>();

		for (Player p : Bukkit.getOnlinePlayers()) {
			list.add(p.getName());
		}
		list.addAll(SpectatorManager.getSpectators());
		return list;
	}

	public void openGUI(int page) {
		Set<String> players = getPlayers();
		int maxPage = ((players.size() - 1) / 36) + 1;
		if (maxPage < page)
			page = 1;
		if (page < 1) page = 1;
		gui = Bukkit.createInventory(null, 54, "§b플레이어 §f목록");
		currentPage = page;

		int count = 0;
		for (String player : players) {
			ItemStack is;

			if (SpectatorManager.isSpectator(player)) {
				is = ItemLib.WOOL.getItemStack(ItemColor.RED);
				ItemMeta im = is.getItemMeta();
				im.setDisplayName("§b" + player);
				im.setLore(Messager.asList(
						"§7이 플레이어는 게임에서 예외됩니다.",
						"§b» §f예외 처리를 해제하려면 클릭하세요."
				));
				is.setItemMeta(im);
			} else {
				is = ItemLib.WOOL.getItemStack(ItemColor.LIME);
				ItemMeta im = is.getItemMeta();
				im.setDisplayName("§b" + player);
				im.setLore(Messager.asList(
						"§7이 플레이어는 게임에서 예외되지 않습니다.",
						"§b» §f예외 처리를 하려면 클릭하세요."
				));
				is.setItemMeta(im);
			}

			if (count / 36 == page - 1) {
				gui.setItem(count % 36, is);
			}
			count++;
		}

		if (page > 1) gui.setItem(48, PREVIOUS_PAGE);
		if (page != maxPage) gui.setItem(50, NEXT_PAGE);

		ItemStack stack = new ItemStack(Material.PAPER, 1);
		ItemMeta meta = stack.getItemMeta();
		meta.setDisplayName("§6페이지 §e" + page + " §6/ §e" + maxPage);
		stack.setItemMeta(meta);
		gui.setItem(49, stack);

		p.openInventory(gui);
	}

	@EventHandler
	private void onInventoryClose(InventoryCloseEvent e) {
		if (e.getInventory().equals(this.gui)) {
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
			Player p = (Player) e.getWhoClicked();
			if (e.getCurrentItem() != null && e.getCurrentItem().hasItemMeta() && e.getCurrentItem().getItemMeta().hasDisplayName()) {
				if (e.getCurrentItem().getItemMeta().getDisplayName().equals(ChatColor.AQUA + "이전 페이지")) {
					openGUI(currentPage - 1);
				} else if (e.getCurrentItem().getItemMeta().getDisplayName().equals(ChatColor.AQUA + "다음 페이지")) {
					openGUI(currentPage + 1);
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

							openGUI(currentPage);
						} else {
							throw new Exception("해당 플레이어가 존재하지 않습니다.");
						}
					} else if (MaterialX.LIME_WOOL.compareType(e.getCurrentItem())) {
						String target = Bukkit.getPlayerExact(ChatColor.stripColor(e.getCurrentItem().getItemMeta().getDisplayName())).getName();
						if (target != null) {
							SpectatorManager.addSpectator(target);

							openGUI(currentPage);
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