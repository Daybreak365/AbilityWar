package daybreak.abilitywar.config.wizard;

import daybreak.abilitywar.config.game.GameSettings;
import daybreak.abilitywar.config.game.GameSettings.Setting;
import daybreak.abilitywar.config.wizard.setter.Setter;
import daybreak.abilitywar.utils.base.collect.Pair;
import daybreak.abilitywar.utils.library.MaterialX;
import daybreak.abilitywar.utils.library.item.ItemBuilder;
import java.util.Map;
import java.util.Set;
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

public class GameSettingWizard implements Listener {

	private static final ItemStack PREVIOUS_PAGE = new ItemBuilder()
			.type(Material.ARROW)
			.displayName(ChatColor.AQUA + "이전 페이지")
			.build();

	private static final ItemStack NEXT_PAGE = new ItemBuilder()
			.type(Material.ARROW)
			.displayName(ChatColor.AQUA + "다음 페이지")
			.build();

	private static final ItemStack QUIT = new ItemBuilder()
			.type(MaterialX.SPRUCE_DOOR)
			.displayName(ChatColor.AQUA + "나가기")
			.build();

	private final Player player;
	private GameSettings gameSettings = null;
	private Pair<String, Map<String, Setting<?>>> settings = null;

	public GameSettingWizard(Player player, Plugin plugin) {
		this.player = player;
		Bukkit.getPluginManager().registerEvents(this, plugin);
	}

	private int currentPage;
	private Inventory gui;

	public void openGUI(int page) {
		if (gameSettings != null) {
			if (settings == null) {
				this.gui = Bukkit.createInventory(null, 54, "게임 목록");
				final Set<String> rowKeySet = gameSettings.getSettings().rowKeySet();
				int maxPage = ((rowKeySet.size() - 1) / 36) + 1;
				if (maxPage < page || page < 1) page = 1;
				this.currentPage = page;

				int count = 0;
				for (String game : rowKeySet) {
					if (count / 36 == page - 1) {
						final ItemStack stack = new ItemStack(Material.BOOK);
						final ItemMeta meta = stack.getItemMeta();
						meta.setDisplayName(ChatColor.AQUA + game);
						stack.setItemMeta(meta);
						gui.setItem(count % 36, stack);
					}
					count++;
				}

				gui.setItem(45, new ItemBuilder()
						.type(MaterialX.RED_WOOL)
						.displayName(ChatColor.RED + "초기화")
						.lore(ChatColor.WHITE + gameSettings.getConfigFile().getName() + " 파일의 모든 게임 설정을 초기화합니다.")
						.build());
				if (page > 1) gui.setItem(48, PREVIOUS_PAGE);
				if (page != maxPage) gui.setItem(50, NEXT_PAGE);

				gui.setItem(53, QUIT);

				final ItemStack stack = new ItemStack(Material.PAPER, 1);
				final ItemMeta meta = stack.getItemMeta();
				meta.setDisplayName("§6페이지 §e" + page + " §6/ §e" + maxPage);
				stack.setItemMeta(meta);
				gui.setItem(49, stack);
			} else {
				this.gui = Bukkit.createInventory(null, 27, settings.getLeft());

				int maxPage = ((settings.getRight().size() - 1) / 18) + 1;
				if (maxPage < page || page < 1) page = 1;
				this.currentPage = page;

				int count = 0;
				for (Setting<?> setting : settings.getRight().values()) {
					if (count / 18 == page - 1) {
						gui.setItem(count % 18, Setter.getInstance(setting).getItem(setting));
					}
					count++;
				}

				gui.setItem(18, new ItemBuilder()
						.type(MaterialX.RED_WOOL)
						.displayName(ChatColor.RED + "초기화")
						.lore(ChatColor.WHITE + settings.getLeft() + " 게임의 모든 설정을 초기화합니다.")
						.build());
				if (page > 1) gui.setItem(21, PREVIOUS_PAGE);
				if (page != maxPage) gui.setItem(23, NEXT_PAGE);

				gui.setItem(26, QUIT);

				final ItemStack stack = new ItemStack(Material.PAPER, 1);
				final ItemMeta meta = stack.getItemMeta();
				meta.setDisplayName("§6페이지 §e" + page + " §6/ §e" + maxPage);
				stack.setItemMeta(meta);
				gui.setItem(22, stack);
			}
		} else {
			this.gui = Bukkit.createInventory(null, 18, "게임 설정 파일 목록");
			int maxPage = ((GameSettings.getGameSettings().size() - 1) / 9) + 1;
			if (maxPage < page || page < 1) page = 1;
			this.currentPage = page;

			int count = 0;
			for (GameSettings gameSettings : GameSettings.getGameSettings()) {
				if (count / 9 == page - 1) {
					final ItemStack stack = new ItemStack(Material.PAPER);
					final ItemMeta meta = stack.getItemMeta();
					final String fileName = gameSettings.getConfigFile().getName();
					final int dotIndex = fileName.lastIndexOf(46);
					meta.setDisplayName(dotIndex == -1 ? fileName : (ChatColor.AQUA + fileName.substring(0, dotIndex) + ChatColor.WHITE + fileName.substring(dotIndex)));
					stack.setItemMeta(meta);
					gui.setItem(count % 9, stack);
				}
				count++;
			}

			gui.setItem(9, new ItemBuilder()
					.type(MaterialX.RED_WOOL)
					.displayName(ChatColor.RED + "초기화")
					.lore(ChatColor.WHITE + "모든 게임 설정을 초기화합니다.")
					.build());
			if (page > 1) gui.setItem(12, PREVIOUS_PAGE);
			if (page != maxPage) gui.setItem(14, NEXT_PAGE);

			final ItemStack stack = new ItemStack(Material.PAPER, 1);
			final ItemMeta meta = stack.getItemMeta();
			meta.setDisplayName("§6페이지 §e" + page + " §6/ §e" + maxPage);
			stack.setItemMeta(meta);
			gui.setItem(13, stack);
		}
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

			final ItemStack clicked = e.getCurrentItem();
			if (clicked != null && !clicked.getType().equals(Material.AIR) && clicked.hasItemMeta() && clicked.getItemMeta().hasDisplayName()) {
				final String stripName = ChatColor.stripColor(clicked.getItemMeta().getDisplayName());
				if (clicked.getType().equals(Material.ARROW)) {
					if (stripName.equals("이전 페이지")) {
						openGUI(currentPage - 1);
						return;
					} else if (stripName.equals("다음 페이지")) {
						openGUI(currentPage + 1);
						return;
					}
				}
				if (gameSettings != null) {
					if (settings == null) {
						if (MaterialX.SPRUCE_DOOR.compare(clicked) && stripName.equals("나가기")) {
							gameSettings = null;
							openGUI(1);
						} else if (MaterialX.RED_WOOL.compare(clicked) && stripName.equals("초기화")) {
							for (Setting<?> setting : gameSettings.getSettings().values()) {
								setting.reset();
							}
						} else if (clicked.getType() == Material.BOOK) {
							this.settings = Pair.of(stripName, gameSettings.getSettings(stripName));
							openGUI(1);
						}
					} else {
						if (MaterialX.SPRUCE_DOOR.compare(clicked) && stripName.equals("나가기")) {
							settings = null;
							openGUI(1);
						} else if (MaterialX.RED_WOOL.compare(clicked) && stripName.equals("초기화")) {
							for (Setting<?> setting : settings.getRight().values()) {
								setting.reset();
							}
							openGUI(currentPage);
						} else {
							final Setting<?> settingObject = gameSettings.getSetting(settings.getLeft(), stripName);
							if (settingObject != null) {
								if (Setter.getInstance(settingObject).onClick(settingObject, e.getClick()))
									openGUI(currentPage);
							}
						}
					}
				} else {
					if (MaterialX.RED_WOOL.compare(clicked) && stripName.equals("초기화")) {
						for (GameSettings gameSetting : GameSettings.getGameSettings()) {
							for (Setting<?> setting : gameSetting.getSettings().values()) {
								setting.reset();
							}
						}
					} else {
						this.gameSettings = GameSettings.getGameSetting(stripName);
						openGUI(1);
					}
				}
			}
		}
	}

}
