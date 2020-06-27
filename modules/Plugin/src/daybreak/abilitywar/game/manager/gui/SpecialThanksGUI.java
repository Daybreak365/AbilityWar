package daybreak.abilitywar.game.manager.gui;

import daybreak.abilitywar.utils.base.minecraft.MojangAPI;
import daybreak.abilitywar.utils.library.item.ItemBuilder;
import daybreak.abilitywar.utils.library.item.ItemLib;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
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
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.plugin.Plugin;

/**
 * 기여자 목록 GUI
 */
public class SpecialThanksGUI implements Listener {

	private static final ItemStack PREVIOUS_PAGE = new ItemBuilder()
			.type(Material.ARROW)
			.displayName(ChatColor.AQUA + "이전 페이지")
			.build();

	private static final ItemStack NEXT_PAGE = new ItemBuilder()
			.type(Material.ARROW)
			.displayName(ChatColor.AQUA + "다음 페이지")
			.build();

	private static final SpecialThank[] SpecialThanks = {
			new SpecialThank("f6cef0829b7e48c1a973532389b6e3e1", "다량의 능력 아이디어를 제공해주셨습니다."),
			new SpecialThank("ecb53e2ffdf34089ae3486cff3fc5f34", "능력 아이디어 제공 및 코드 개선에 도움을 주셨습니다."),
			new SpecialThank("2dcb3299e24049adb8bb554d862bd7be", "테스팅에 도움을 주셨습니다."),
			new SpecialThank("101ceb32a2bc4dbd9d32291c86b66eca", "테스팅에 도움을 주셨습니다."),
			new SpecialThank("2b4af44c86434a2fa3b07a34f4406636", "테스팅에 도움을 주셨습니다."),
			new SpecialThank("507fc49666fb43489200251f48bf4719", "몇몇 업데이트에 기여하셨습니다.")
	};

	@EventHandler
	private void onQuit(PlayerQuitEvent e) {
		if (e.getPlayer().getUniqueId().equals(p.getUniqueId())) {
			HandlerList.unregisterAll(this);
		}
	}

	private final Player p;

	public SpecialThanksGUI(Player p, Plugin plugin) {
		this.p = p;
		Bukkit.getPluginManager().registerEvents(this, plugin);
	}

	private int currentPage = 1;
	private Inventory gui;

	public void openGUI(int page) {
		int maxPage = ((SpecialThanks.length - 1) / 18) + 1;
		if (maxPage < page) page = 1;
		if (page < 1) page = 1;
		gui = Bukkit.createInventory(null, 27, "§c§l✿ §0§lSpecial Thanks §c§l✿");
		currentPage = page;
		int count = 0;

		for (SpecialThank thank : SpecialThanks) {
			ItemStack stack = ItemLib.getSkull(thank.getName());
			SkullMeta meta = (SkullMeta) stack.getItemMeta();
			if (!thank.getName().equals("ERROR")) {
				meta.setDisplayName("§e" + thank.getName());
				meta.setLore(thank.getRole());
			} else {
				meta.setDisplayName("§c오류");
				meta.setLore(Collections.singletonList("§bMojang API§f에 연결할 수 없습니다."));
			}

			stack.setItemMeta(meta);

			if (count / 18 == page - 1) {
				gui.setItem(count % 18, stack);
			}
			count++;
		}

		if (page > 1) gui.setItem(21, PREVIOUS_PAGE);
		if (page != maxPage) gui.setItem(23, NEXT_PAGE);

		ItemStack stack = new ItemStack(Material.PAPER, 1);
		ItemMeta meta = stack.getItemMeta();
		meta.setDisplayName("§6페이지 §e" + page + " §6/ §e" + maxPage);
		stack.setItemMeta(meta);
		gui.setItem(22, stack);

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
			if (e.getCurrentItem() != null && e.getCurrentItem().hasItemMeta() && e.getCurrentItem().getItemMeta().hasDisplayName()) {
				if (e.getCurrentItem().getItemMeta().getDisplayName().equals(ChatColor.AQUA + "이전 페이지")) {
					openGUI(currentPage - 1);
				} else if (e.getCurrentItem().getItemMeta().getDisplayName().equals(ChatColor.AQUA + "다음 페이지")) {
					openGUI(currentPage + 1);
				}
			}
		}
	}

	public static class SpecialThank {

		private String name;
		private final String[] role;

		public SpecialThank(String UUID, String... role) {
			try {
				this.name = MojangAPI.getNickname(UUID);
			} catch (IOException e) {
				this.name = "ERROR";
			}
			this.role = role;
			for (int i = 0; i < role.length; i++) {
				role[i] = ChatColor.WHITE + role[i];
			}
		}

		public String getName() {
			return name;
		}

		public List<String> getRole() {
			return Arrays.asList(role);
		}

	}

}