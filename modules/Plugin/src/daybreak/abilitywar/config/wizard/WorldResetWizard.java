package daybreak.abilitywar.config.wizard;

import daybreak.abilitywar.config.Configuration;
import daybreak.abilitywar.config.Configuration.Settings;
import daybreak.abilitywar.config.enums.ConfigNodes;
import daybreak.abilitywar.utils.base.minecraft.WorldReset;
import daybreak.abilitywar.utils.base.minecraft.item.builder.ItemBuilder;
import daybreak.abilitywar.utils.library.MaterialX;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.configuration.InvalidConfigurationException;
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

import java.io.IOException;
import java.util.Arrays;

public class WorldResetWizard implements Listener {

	private static final ItemStack PREVIOUS_PAGE = new ItemBuilder(MaterialX.ARROW)
			.displayName(ChatColor.AQUA + "이전 페이지")
			.build();

	private static final ItemStack NEXT_PAGE = new ItemBuilder(MaterialX.ARROW)
			.displayName(ChatColor.AQUA + "다음 페이지")
			.build();

	private final Player player;
	private int playerPage = 1;
	private Inventory gui;

	public WorldResetWizard(Player player, Plugin Plugin) {
		this.player = player;
		Bukkit.getPluginManager().registerEvents(this, Plugin);
	}

	public void openGUI(int page) {
		final int maxPage = ((Settings.getPresetContainer().getPresets().size() - 1) / 9) + 1;
		if (maxPage < page || page < 1) page = 1;
		gui = Bukkit.createInventory(null, 18, "§8세계 목록");
		playerPage = page;
		int count = 0;

		for (World world : Bukkit.getWorlds()) {
			final ItemStack stack;
			if (WorldReset.defaultWorlds.contains(world.getName())) {
				stack = MaterialX.GRAY_WOOL.createItem();
				final ItemMeta meta = stack.getItemMeta();
				meta.setDisplayName("§7" + world.getName());
				meta.setLore(Arrays.asList(
						"§7✕ §f초기화 기능이 지원되지 않는 세계입니다.",
						"§f마인크래프트 기본 세계는 초기화할 수 없습니다."
				));
				stack.setItemMeta(meta);
			} else if (WorldReset.hasBackup(world)) {
				stack = MaterialX.GREEN_WOOL.createItem();
				final ItemMeta meta = stack.getItemMeta();
				meta.setDisplayName("§a" + world.getName());
				meta.setLore(Arrays.asList(
						"§2✔ §a백업 파일이 있습니다.",
						"§f세계 초기화 기능 사용 시 초기화됩니다."
				));
				stack.setItemMeta(meta);
			} else {
				stack = MaterialX.RED_WOOL.createItem();
				final ItemMeta meta = stack.getItemMeta();
				meta.setDisplayName("§c" + world.getName());
				meta.setLore(Arrays.asList(
						"§4✕ §c백업 파일이 없습니다.",
						"§f세계 초기화 기능 사용 시 초기화되지 않습니다.",
						"§eplugins/AbilityWar/mapBackup §f폴더 내에",
						"§f백업할 월드를 §bzip §f형식으로 압축해 넣어주세요."
				));
				stack.setItemMeta(meta);
			}

			if (count / 9 == page - 1) {
				gui.setItem(count % 9, stack);
			}
			count++;
		}

		if (page > 1) gui.setItem(12, PREVIOUS_PAGE);
		if (page != maxPage) gui.setItem(14, NEXT_PAGE);

		{
			final ItemStack stack = new ItemStack(Material.PAPER, 1);
			final ItemMeta meta = stack.getItemMeta();
			meta.setDisplayName("§6페이지 §e" + page + " §6/ §e" + maxPage);
			stack.setItemMeta(meta);
			gui.setItem(13, stack);
		}

		{
			final ItemStack stack = MaterialX.CLOCK.createItem();
			final ItemMeta meta = stack.getItemMeta();
			meta.setDisplayName("§b월드 자동 초기화");
			meta.setLore(Arrays.asList(
					"§a활성화 §f하면 게임 종료시 월드 초기화 기능을",
					"§f자동으로 실행합니다",
					"", "§7상태 : " + (Settings.isAutoWorldResetEnabled() ? "§a활성화" : "§c비활성화"),
					"§7클릭§f해서 §a활성화§f/§c비활성화 §f여부를 변경하세요."
			));
			stack.setItemMeta(meta);
			gui.setItem(9, stack);
		}

		player.openInventory(gui);
	}

	@EventHandler
	private void onInventoryClose(InventoryCloseEvent e) {
		if (e.getInventory().equals(this.gui)) {
			HandlerList.unregisterAll(this);
			try {
				Configuration.update();
			} catch (IOException | InvalidConfigurationException ignored) {
			}
		}
	}

	@EventHandler
	private void onQuit(PlayerQuitEvent e) {
		if (e.getPlayer().getUniqueId().equals(player.getUniqueId())) {
			HandlerList.unregisterAll(this);
			try {
				Configuration.update();
			} catch (IOException | InvalidConfigurationException ignored) {
			}
		}
	}

	@EventHandler
	private void onInventoryClick(InventoryClickEvent e) {
		if (e.getInventory().equals(gui)) {
			e.setCancelled(true);
			if (e.getCurrentItem() != null && e.getCurrentItem().hasItemMeta() && e.getCurrentItem().getItemMeta().hasDisplayName()) {
				final String displayName = e.getCurrentItem().getItemMeta().getDisplayName();
				if (displayName.equals(ChatColor.AQUA + "이전 페이지")) {
					openGUI(playerPage - 1);
				} else if (displayName.equals(ChatColor.AQUA + "다음 페이지")) {
					openGUI(playerPage + 1);
				} else if (displayName.equals(ChatColor.AQUA + "월드 자동 초기화")) {
					Configuration.modifyProperty(ConfigNodes.WORLD_RESET_ON_GAME_END, !Settings.isAutoWorldResetEnabled());
					openGUI(playerPage);
				}
			}
		}
	}

}
