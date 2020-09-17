package daybreak.abilitywar.game.manager.gui.tip;

import com.google.common.collect.ImmutableSet;
import daybreak.abilitywar.ability.AbilityFactory.AbilityRegistration;
import daybreak.abilitywar.ability.AbilityFactory.AbilityRegistration.Tip;
import daybreak.abilitywar.ability.AbilityFactory.AbilityRegistration.Tip.Description;
import daybreak.abilitywar.ability.Tips.Stats;
import daybreak.abilitywar.game.manager.gui.PagedGUI;
import daybreak.abilitywar.utils.base.Formatter;
import daybreak.abilitywar.utils.base.minecraft.item.builder.ItemBuilder;
import daybreak.abilitywar.utils.library.MaterialX;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.List;

public class AbilityTipGUI implements Listener {

	private static final ItemStack PREVIOUS_PAGE = new ItemBuilder(MaterialX.ARROW)
			.displayName(ChatColor.DARK_AQUA + "이전 페이지")
			.build();

	private static final ItemStack NEXT_PAGE = new ItemBuilder(MaterialX.ARROW)
			.displayName(ChatColor.DARK_AQUA + "다음 페이지")
			.build();

	private static final ItemStack QUIT = new ItemBuilder(MaterialX.SPRUCE_DOOR)
			.displayName(ChatColor.DARK_AQUA + "나가기")
			.build();

	private static final ItemStack NO_TIP = new ItemBuilder(MaterialX.BARRIER)
			.displayName(ChatColor.RED + "작성되지 않았습니다.")
			.build();

	private static final ItemStack DECO = new ItemBuilder(MaterialX.GRAY_STAINED_GLASS_PANE)
			.displayName("§f")
			.build();

	private final Plugin plugin;
	private final Player player;
	private final AbilityRegistration registration;
	private int currentPage = 1;
	private State state = State.MAIN;
	private PagedGUI pagedGUI;
	private Inventory gui;

	public AbilityTipGUI(Player player, AbilityRegistration registration, Plugin plugin) {
		this.plugin = plugin;
		this.player = player;
		this.registration = registration;
		this.pagedGUI = null;
		Bukkit.getPluginManager().registerEvents(this, plugin);
	}

	public AbilityTipGUI(Player player, AbilityRegistration registration, PagedGUI pagedGUI, Plugin plugin) {
		this.plugin = plugin;
		this.player = player;
		this.registration = registration;
		this.pagedGUI = pagedGUI;
		Bukkit.getPluginManager().registerEvents(this, plugin);
	}

	public void openGUI(int page) {
		switch (state) {
			case MAIN: {
				this.gui = Bukkit.createInventory(null, InventoryType.HOPPER, "§9" + registration.getManifest().name() + " §0능력 팁");
				this.currentPage = 1;
				final Tip tip = registration.getTip();
				if (tip == null) {
					for (int i = 0; i < 4; i++) {
						gui.setItem(i, NO_TIP);
					}
				} else {
					{
						final ItemStack stack = MaterialX.ENCHANTED_BOOK.createItem();
						final ItemMeta meta = stack.getItemMeta();
						meta.addItemFlags(ItemFlag.values());
						meta.setDisplayName("§b" + registration.getManifest().name());
						final Stats stats = tip.stats;
						final List<String> lore = new ArrayList<>(10 + tip.tips.size());
						lore.add("§f난이도§7: " + tip.difficulty.getDisplay());
						lore.add("§8--------------------------");
						lore.add("§f공격     §7: " + stats.offense().getDisplay());
						lore.add("§f생존     §7: " + stats.survival().getDisplay());
						lore.add("§f군중제어 §7: " + stats.crowdControl().getDisplay());
						lore.add("§f기동     §7: " + stats.mobility().getDisplay());
						lore.add("§f유틸     §7: " + stats.utility().getDisplay());
						lore.add("§8--------------------------");
						for (String s : tip.tips) {
							lore.add(ChatColor.WHITE + s);
						}
						meta.setLore(lore);
						stack.setItemMeta(meta);
						gui.setItem(0, stack);
					}
					{
						final ItemStack stack = (MaterialX.NETHERITE_SWORD.isSupported() ? MaterialX.NETHERITE_SWORD : MaterialX.DIAMOND_SWORD).createItem();
						final ItemMeta meta = stack.getItemMeta();
						meta.addItemFlags(ItemFlag.values());
						meta.setDisplayName("§b강점");
						final List<String> lore = new ArrayList<>(2 + tip.strong.size());
						for (Description description : tip.strong) {
							lore.add(ChatColor.WHITE + "- " + ChatColor.AQUA + description.subject);
						}
						lore.add("");
						if (tip.strong.isEmpty()) {
							lore.add("§7※ 작성된 강점이 없습니다.");
						} else {
							lore.add("§7※ 세부 내용을 확인하려면 클릭하세요.");
						}
						meta.setLore(lore);
						stack.setItemMeta(meta);
						gui.setItem(1, stack);
					}
					{
						final ItemStack stack = MaterialX.WOODEN_SWORD.createItem();
						final ItemMeta meta = stack.getItemMeta();
						meta.addItemFlags(ItemFlag.values());
						meta.setDisplayName("§b약점");
						final List<String> lore = new ArrayList<>(2 + tip.weak.size());
						for (Description description : tip.weak) {
							lore.add(ChatColor.WHITE + "- " + ChatColor.AQUA + description.subject);
						}
						lore.add("");
						if (tip.weak.isEmpty()) {
							lore.add("§7※ 작성된 약점이 없습니다.");
						} else {
							lore.add("§7※ 세부 내용을 확인하려면 클릭하세요.");
						}
						meta.setLore(lore);
						stack.setItemMeta(meta);
						gui.setItem(2, stack);
					}
				}
				gui.setItem(3, DECO);
				gui.setItem(4, QUIT);
			}
			break;
			case STRONGS: {
				final Tip tip = registration.getTip();
				final ImmutableSet<Description> strong = tip.strong;
				int maxPage = ((strong.size() - 1) / 9) + 1;
				if (maxPage < page) page = 1;
				if (page < 1) page = 1;
				gui = Bukkit.createInventory(null, 18, "§0강점");
				this.currentPage = page;
				int count = 0;

				for (final Description description : strong) {
					if (count / 9 == page - 1) {
						final ItemStack stack = description.icon.createItem();
						final ItemMeta meta = stack.getItemMeta();
						meta.setDisplayName("§b" + description.subject);
						final List<String> lore = new ArrayList<>(description.explain.size());
						for (String s : description.explain) {
							lore.add(ChatColor.WHITE + s);
						}
						meta.setLore(lore);
						stack.setItemMeta(meta);
						gui.setItem(count % 9, stack);
					}
					count++;
				}

				gui.setItem(9, QUIT);
				if (page > 1) gui.setItem(12, PREVIOUS_PAGE);
				if (page != maxPage) gui.setItem(14, NEXT_PAGE);
				final ItemStack stack = new ItemStack(Material.PAPER, 1);
				final ItemMeta meta = stack.getItemMeta();
				meta.setDisplayName("§6페이지 §e" + page + " §6/ §e" + maxPage);
				stack.setItemMeta(meta);
				gui.setItem(13, stack);
			}
			break;
			case WEAKS: {
				final Tip tip = registration.getTip();
				final ImmutableSet<Description> weak = tip.weak;
				int maxPage = ((weak.size() - 1) / 9) + 1;
				if (maxPage < page) page = 1;
				if (page < 1) page = 1;
				gui = Bukkit.createInventory(null, 18, "§0약점");
				this.currentPage = page;
				int count = 0;

				for (final Description description : weak) {
					if (count / 9 == page - 1) {
						final ItemStack stack = description.icon.createItem();
						final ItemMeta meta = stack.getItemMeta();
						meta.setDisplayName("§b" + description.subject);
						final List<String> lore = new ArrayList<>(description.explain.size());
						for (String s : description.explain) {
							lore.add(ChatColor.WHITE + s);
						}
						meta.setLore(lore);
						stack.setItemMeta(meta);
						gui.setItem(count % 9, stack);
					}
					count++;
				}

				gui.setItem(9, QUIT);
				if (page > 1) gui.setItem(12, PREVIOUS_PAGE);
				if (page != maxPage) gui.setItem(14, NEXT_PAGE);
				final ItemStack stack = new ItemStack(Material.PAPER, 1);
				final ItemMeta meta = stack.getItemMeta();
				meta.setDisplayName("§6페이지 §e" + page + " §6/ §e" + maxPage);
				stack.setItemMeta(meta);
				gui.setItem(13, stack);
			}
			break;
		}
		player.openInventory(gui);
	}

	private enum State {
		MAIN, STRONGS, WEAKS
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
				final String displayName = e.getCurrentItem().getItemMeta().getDisplayName();
				switch (state) {
					case STRONGS: case WEAKS: {
						switch (displayName) {
							case "§3이전 페이지":
								openGUI(currentPage - 1);
								break;
							case "§3다음 페이지":
								openGUI(currentPage + 1);
								break;
							case "§3나가기":
								this.state = State.MAIN;
								openGUI(1);
								break;
						}
					}
					break;
					case MAIN: {
						if (displayName.equals("§b팁")) {
							player.closeInventory();
							for (String s : Formatter.formatTip(registration)) {
								player.sendMessage(s);
							}
						} else if (displayName.equals("§b강점")) {
							if (!registration.getTip().strong.isEmpty()) {
								this.state = State.STRONGS;
								openGUI(1);
							}
						} else if (displayName.equals("§b약점")) {
							if (!registration.getTip().weak.isEmpty()) {
								this.state = State.WEAKS;
								openGUI(1);
							}
						} else if (displayName.equals("§3나가기")) {
							if (pagedGUI != null) {
								Bukkit.getPluginManager().registerEvents(pagedGUI, plugin);
								pagedGUI.openGUI(pagedGUI.getCurrentPage());
							} else {
								player.closeInventory();
							}
						}
					}
					break;
				}
			}
		}
	}

}
