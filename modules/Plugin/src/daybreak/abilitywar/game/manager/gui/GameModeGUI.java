package daybreak.abilitywar.game.manager.gui;

import daybreak.abilitywar.config.Configuration;
import daybreak.abilitywar.config.Configuration.Settings;
import daybreak.abilitywar.config.enums.ConfigNodes;
import daybreak.abilitywar.game.AbstractGame;
import daybreak.abilitywar.game.Category.GameCategory;
import daybreak.abilitywar.game.GameManifest;
import daybreak.abilitywar.game.manager.GameFactory;
import daybreak.abilitywar.game.manager.GameFactory.GameRegistration;
import daybreak.abilitywar.game.manager.GameFactory.GameRegistration.Flag;
import daybreak.abilitywar.utils.base.Messager;
import daybreak.abilitywar.utils.base.logging.Logger;
import daybreak.abilitywar.utils.base.minecraft.item.builder.ItemBuilder;
import daybreak.abilitywar.utils.library.MaterialX;
import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.enchantments.Enchantment;
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

public class GameModeGUI implements Listener {

	private static final ItemStack PREVIOUS_PAGE = new ItemBuilder(MaterialX.ARROW)
			.displayName(ChatColor.AQUA + "이전 페이지")
			.build();

	private static final ItemStack NEXT_PAGE = new ItemBuilder(MaterialX.ARROW)
			.displayName(ChatColor.AQUA + "다음 페이지")
			.build();

	protected static final ItemStack DECO = new ItemBuilder(MaterialX.GRAY_STAINED_GLASS_PANE)
			.displayName(ChatColor.WHITE.toString())
			.build();

	private static final Logger logger = Logger.getLogger(GameModeGUI.class.getName());

	private final Player player;
	private GameCategory category = GameCategory.GAME;

	public GameModeGUI(Player player, Plugin Plugin) {
		this.player = player;
		Bukkit.getPluginManager().registerEvents(this, Plugin);
	}

	private int playerPage = 1;

	private Inventory gui;

	public void openGUI(int page) {
		int maxPage = ((GameFactory.getByCategory(category).size() - 1) / 18) + 1;
		if (maxPage < page) page = 1;
		if (page < 1) page = 1;
		gui = Bukkit.createInventory(null, 36, "§cAbilityWar §8게임 모드 (" + category.getDisplayName() + ")");
		this.playerPage = page;
		int count = 0;

		final Class<? extends AbstractGame> gameClass = Settings.getGameMode();

		for (final GameRegistration registration : GameFactory.getByCategory(category)) {
			final Class<? extends AbstractGame> mode = registration.getGameClass();
			final GameManifest manifest = registration.getManifest();
			final ItemStack stack;
			if (gameClass.equals(mode)) {
				stack = MaterialX.ENCHANTED_BOOK.createItem();
				ItemMeta meta = stack.getItemMeta();
				meta.setDisplayName("§b" + manifest.name());
				List<String> lore = Messager.asList(manifest.description());
				if (registration.hasFlag(Flag.TEAM_GAME_SUPPORTED)) {
					lore.add("");
					lore.add("§3● §f팀 게임이 지원됩니다.");
					lore.add("");
				}
				lore.add("§7선택된 게임모드입니다.");
				meta.setLore(lore);
				stack.setItemMeta(meta);
			} else {
				stack = MaterialX.BOOK.createItem();
				ItemMeta meta = stack.getItemMeta();
				meta.setDisplayName("§b" + manifest.name());
				List<String> lore = Messager.asList(manifest.description());
				if (registration.hasFlag(Flag.TEAM_GAME_SUPPORTED)) {
					lore.add("");
					lore.add("§3● §f팀 게임이 지원됩니다.");
					lore.add("");
				}
				lore.add("§b» §f이 게임모드를 선택하려면 클릭하세요.");
				meta.setLore(lore);
				stack.setItemMeta(meta);
			}

			if (count / 18 == page - 1) {
				gui.setItem(count % 18, stack);
			}
			count++;
		}

		final GameCategory[] categories = GameCategory.values();
		for (int i = 0; i < categories.length; i++) {
			final GameCategory gameCategory = categories[i];
			final ItemStack stack = new ItemStack(gameCategory.getIcon());
			if (category.equals(gameCategory)) stack.addUnsafeEnchantment(Enchantment.DURABILITY, 1);
			final ItemMeta meta = stack.getItemMeta();
			meta.setDisplayName(ChatColor.WHITE + gameCategory.getDisplayName());
			stack.setItemMeta(meta);
			gui.setItem(27 + i, stack);
		}

		for (int i = 18; i <= 26; i++) gui.setItem(i, DECO);

		if (page > 1) gui.setItem(30, PREVIOUS_PAGE);
		if (page != maxPage) gui.setItem(32, NEXT_PAGE);

		ItemStack stack = new ItemStack(Material.PAPER, 1);
		ItemMeta meta = stack.getItemMeta();
		meta.setDisplayName("§6페이지 §e" + page + " §6/ §e" + maxPage);
		stack.setItemMeta(meta);
		gui.setItem(31, stack);

		player.openInventory(gui);
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
	private void onQuit(PlayerQuitEvent e) {
		if (e.getPlayer().getUniqueId().equals(player.getUniqueId())) {
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
				final String displayName = e.getCurrentItem().getItemMeta().getDisplayName();
				final String stripName = ChatColor.stripColor(displayName);
				if (displayName.equals(ChatColor.AQUA + "이전 페이지")) {
					openGUI(playerPage - 1);
				} else if (displayName.equals(ChatColor.AQUA + "다음 페이지")) {
					openGUI(playerPage + 1);
				}

				if (e.getCurrentItem().getType().equals(Material.BOOK)) {
					final GameRegistration gameMode = GameFactory.getByName(stripName);
					if (gameMode != null) {
						Configuration.modifyProperty(ConfigNodes.GAME_MODE, gameMode.getGameClass().getName());
					} else {
						Messager.sendErrorMessage(player, "§c" + stripName + " §f클래스는 등록되지 않았습니다.");
					}

					openGUI(playerPage);
				} else if (e.getSlot() >= 27 && e.getSlot() <= 29) {
					if (category.getDisplayName().equals(stripName)) return;
					for (GameCategory value : GameCategory.values()) {
						if (value.getDisplayName().equals(stripName)) {
							this.category = value;
							break;
						}
					}
					openGUI(1);

				}
			}
		}
	}

}
