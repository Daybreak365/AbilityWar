package daybreak.abilitywar.config.wizard;

import daybreak.abilitywar.config.kitpreset.KitConfiguration;
import daybreak.abilitywar.config.kitpreset.KitConfiguration.KitSettings;
import daybreak.abilitywar.config.kitpreset.KitNodes;
import daybreak.abilitywar.config.serializable.KitPreset;
import daybreak.abilitywar.utils.library.MaterialX;
import daybreak.abilitywar.utils.library.item.ItemBuilder;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.Nullable;

public class KitPresetWizard implements Listener {

	private static final ItemStack PREVIOUS_PAGE = new ItemBuilder()
			.type(Material.ARROW)
			.displayName(ChatColor.AQUA + "이전 페이지")
			.build();

	private static final ItemStack NEXT_PAGE = new ItemBuilder()
			.type(Material.ARROW)
			.displayName(ChatColor.AQUA + "다음 페이지")
			.build();

	private static final ItemStack NEW_PRESET = new ItemBuilder()
			.type(MaterialX.LIME_WOOL)
			.displayName(ChatColor.GREEN + "프리셋 추가")
			.build();

	private static final ItemStack SET_AS_KIT = new ItemBuilder()
			.type(MaterialX.LIME_WOOL)
			.displayName(ChatColor.GREEN + "적용")
			.build();

	private static final ItemStack QUIT = new ItemBuilder()
			.type(MaterialX.SPRUCE_DOOR)
			.displayName(ChatColor.AQUA + "나가기")
			.build();

	private static final ItemStack BOOTS = new ItemBuilder()
			.type(MaterialX.BLACK_STAINED_GLASS_PANE)
			.displayName(ChatColor.AQUA + "부츠")
			.lore(
					"§7부츠 아이템을 변경하려면 아이템을 들고",
					"§7여기를 클릭하세요."
			)
			.build();

	private static final ItemStack LEGGINGS = new ItemBuilder()
			.type(MaterialX.BLACK_STAINED_GLASS_PANE)
			.displayName(ChatColor.AQUA + "각반")
			.lore(
					"§7각반 아이템을 변경하려면 아이템을 들고",
					"§7여기를 클릭하세요."
			)
			.build();

	private static final ItemStack CHESTPLATE = new ItemBuilder()
			.type(MaterialX.BLACK_STAINED_GLASS_PANE)
			.displayName(ChatColor.AQUA + "흉갑")
			.lore(
					"§7흉갑 아이템을 변경하려면 아이템을 들고",
					"§7여기를 클릭하세요."
			)
			.build();

	private static final ItemStack HELMET = new ItemBuilder()
			.type(MaterialX.BLACK_STAINED_GLASS_PANE)
			.displayName(ChatColor.AQUA + "투구")
			.lore(
					"§7투구 아이템을 변경하려면 아이템을 들고",
					"§7여기를 클릭하세요."
			)
			.build();

	private static final ItemStack DECO = new ItemBuilder()
			.type(MaterialX.WHITE_STAINED_GLASS_PANE)
			.displayName("§f")
			.build();

	private final Player player;
	@Nullable
	private KitPreset editing;
	private int playerPage = 1;
	private Inventory gui;

	public KitPresetWizard(Player player, Plugin plugin) {
		this.player = player;
		Bukkit.getPluginManager().registerEvents(this, plugin);
	}

	public void openGUI(int page) {
		if (editing == null) {
			int maxPage = ((KitSettings.getKitPresets().size() - 1) / 36) + 1;
			if (maxPage < page || page < 1) page = 1;
			gui = Bukkit.createInventory(null, 27, "§8§l킷 프리셋 목록");
			playerPage = page;
			int count = 0;

			final int size = KitSettings.getKitPresets().size();
			for (int i = 0; i < size; i++) {
				final ItemStack stack = MaterialX.CHEST.createItem();
				final ItemMeta meta = stack.getItemMeta();
				meta.setDisplayName(ChatColor.AQUA.toString() + i);
				meta.setLore(Arrays.asList(
						"§a편집§7하려면 좌클릭하세요.",
						"§7이 프리셋을 §a적용§7하려면 SHIFT + 좌클릭하세요.",
						"§c삭제§7하려면 SHIFT + 우클릭하세요."
				));
				stack.setItemMeta(meta);

				if (count / 18 == page - 1) {
					gui.setItem(count % 18, stack);
				}
				count++;
			}

			if (page > 1) gui.setItem(21, PREVIOUS_PAGE);
			if (page != maxPage) gui.setItem(23, NEXT_PAGE);
			gui.setItem(18, NEW_PRESET);

			final ItemStack stack = new ItemStack(Material.PAPER, 1);
			final ItemMeta meta = stack.getItemMeta();
			meta.setDisplayName("§6페이지 §e" + page + " §6/ §e" + maxPage);
			stack.setItemMeta(meta);
			gui.setItem(22, stack);
		} else {
			gui = Bukkit.createInventory(null, 54, "§8§l킷 프리셋 편집");
			gui.setItem(0, editing.getHelmet() != null ? editing.getHelmet() : HELMET);
			gui.setItem(1, editing.getChestplate() != null ? editing.getChestplate() : CHESTPLATE);
			gui.setItem(2, editing.getLeggings() != null ? editing.getLeggings() : LEGGINGS);
			gui.setItem(3, editing.getBoots() != null ? editing.getBoots() : BOOTS);
			for (int i = 4; i <= 8; i++) gui.setItem(i, DECO);
			for (int i = 46; i <= 52; i++) gui.setItem(i, DECO);
			gui.setItem(45, SET_AS_KIT);
			gui.setItem(53, QUIT);
			gui.addItem(editing.getItems().toArray(new ItemStack[0]));
		}
		player.openInventory(gui);
	}

	@EventHandler
	private void onInventoryClose(InventoryCloseEvent e) {
		if (e.getInventory().equals(this.gui)) {
			HandlerList.unregisterAll(this);
			if (this.editing != null) {
				save();
			} else KitConfiguration.getInstance().updateProperty(KitNodes.KIT_PRESET);
			try {
				KitConfiguration.getInstance().update();
			} catch (IOException | InvalidConfigurationException ignored) {}
		}
	}

	@EventHandler
	private void onQuit(PlayerQuitEvent e) {
		if (e.getPlayer().getUniqueId().equals(player.getUniqueId())) {
			HandlerList.unregisterAll(this);
			if (this.editing != null) {
				save();
			} else KitConfiguration.getInstance().updateProperty(KitNodes.KIT_PRESET);
			try {
				KitConfiguration.getInstance().update();
			} catch (IOException | InvalidConfigurationException ignored) {}
		}
	}

	@EventHandler
	private void onInventoryClick(InventoryClickEvent e) {
		if (gui.equals(e.getInventory())) {
			final Inventory inventory = e.getClickedInventory();
			if (editing == null) {
				e.setCancelled(true);
				if (e.getCurrentItem() != null && e.getCurrentItem().hasItemMeta()) {
					final ItemMeta meta = e.getCurrentItem().getItemMeta();
					final String displayName = meta.hasDisplayName() ? meta.getDisplayName() : "";
					if (displayName.equals(ChatColor.AQUA + "이전 페이지")) {
						openGUI(playerPage - 1);
					} else if (displayName.equals(ChatColor.AQUA + "다음 페이지")) {
						openGUI(playerPage + 1);
					} else if (displayName.equals(ChatColor.GREEN + "프리셋 추가")) {
						final List<KitPreset> presets = KitSettings.getKitPresets();
						presets.add(new KitPreset());
						KitConfiguration.getInstance().modifyProperty(KitNodes.KIT_PRESET, presets);
						openGUI(playerPage);
					} else if (e.getCurrentItem().getType() == MaterialX.CHEST.getMaterial()) {
						try {
							final int index = Integer.parseInt(ChatColor.stripColor(displayName));
							if (e.getClick() == ClickType.LEFT) {
								this.editing = KitSettings.getKitPresets().get(index);
								openGUI(1);
							} else if (e.getClick() == ClickType.SHIFT_LEFT) {
								KitConfiguration.getInstance().modifyProperty(KitNodes.KIT, KitSettings.getKitPresets().get(index));
							} else if (e.getClick() == ClickType.SHIFT_RIGHT) {
								final List<KitPreset> presets = KitSettings.getKitPresets();
								presets.remove(index);
								KitConfiguration.getInstance().modifyProperty(KitNodes.KIT_PRESET, presets);
								openGUI(1);
							}
						} catch (NumberFormatException | IndexOutOfBoundsException ignored) {}
					}
				}
			} else {
				if (!gui.equals(e.getClickedInventory())) return;
				if (e.getSlot() == 0) {
					e.setCancelled(true);
					if (MaterialX.BLACK_STAINED_GLASS_PANE.compare(e.getCursor())) return;
					if (MaterialX.BLACK_STAINED_GLASS_PANE.compare(inventory.getItem(0))) {
						if (isEmpty(e.getCursor())) return;
						editing.setHelmet(e.getCursor());
						inventory.setItem(0, e.getCursor());
						e.getWhoClicked().setItemOnCursor(null);
					} else {
						final ItemStack cursor = e.getCursor();
						editing.setHelmet(cursor);
						e.getWhoClicked().setItemOnCursor(inventory.getItem(0));
						inventory.setItem(0, !isEmpty(cursor) ? cursor : HELMET);
					}
					KitConfiguration.getInstance().updateProperty(KitNodes.KIT_PRESET);
				} else if (e.getSlot() == 1) {
					e.setCancelled(true);
					if (MaterialX.BLACK_STAINED_GLASS_PANE.compare(e.getCursor())) return;
					if (MaterialX.BLACK_STAINED_GLASS_PANE.compare(inventory.getItem(1))) {
						if (isEmpty(e.getCursor())) return;
						editing.setChestplate(e.getCursor());
						inventory.setItem(1, e.getCursor());
						e.getWhoClicked().setItemOnCursor(null);
					} else {
						final ItemStack cursor = e.getCursor();
						editing.setChestplate(cursor);
						e.getWhoClicked().setItemOnCursor(inventory.getItem(1));
						inventory.setItem(1, !isEmpty(cursor) ? cursor : CHESTPLATE);
					}
					KitConfiguration.getInstance().updateProperty(KitNodes.KIT_PRESET);
				} else if (e.getSlot() == 2) {
					e.setCancelled(true);
					if (MaterialX.BLACK_STAINED_GLASS_PANE.compare(e.getCursor())) return;
					if (MaterialX.BLACK_STAINED_GLASS_PANE.compare(inventory.getItem(2))) {
						if (isEmpty(e.getCursor())) return;
						editing.setLeggings(e.getCursor());
						inventory.setItem(2, e.getCursor());
						e.getWhoClicked().setItemOnCursor(null);
					} else {
						final ItemStack cursor = e.getCursor();
						editing.setLeggings(cursor);
						e.getWhoClicked().setItemOnCursor(inventory.getItem(2));
						inventory.setItem(2, !isEmpty(cursor) ? cursor : LEGGINGS);
					}
					KitConfiguration.getInstance().updateProperty(KitNodes.KIT_PRESET);
				} else if (e.getSlot() == 3) {
					e.setCancelled(true);
					if (MaterialX.BLACK_STAINED_GLASS_PANE.compare(e.getCursor())) return;
					if (MaterialX.BLACK_STAINED_GLASS_PANE.compare(inventory.getItem(3))) {
						if (isEmpty(e.getCursor())) return;
						editing.setBoots(e.getCursor());
						inventory.setItem(3, e.getCursor());
						e.getWhoClicked().setItemOnCursor(null);
					} else {
						final ItemStack cursor = e.getCursor();
						editing.setBoots(cursor);
						e.getWhoClicked().setItemOnCursor(inventory.getItem(3));
						inventory.setItem(3, !isEmpty(cursor) ? cursor : BOOTS);
					}
					KitConfiguration.getInstance().updateProperty(KitNodes.KIT_PRESET);
				} else if ((e.getSlot() >= 4 && e.getSlot() <= 8) || (e.getSlot() >= 46 && e.getSlot() <= 52)) {
					e.setCancelled(true);
				} else if (e.getSlot() == 53) {
					e.setCancelled(true);
					save();
					this.editing = null;
					openGUI(playerPage);
				} else if (e.getSlot() == 45) {
					e.setCancelled(true);
					save();
					KitConfiguration.getInstance().modifyProperty(KitNodes.KIT, editing);
				}
			}
		}
	}

	private void save() {
		final List<ItemStack> stacks = new ArrayList<>();
		for (int i = 9; i <= 44; i++) {
			final ItemStack stack = gui.getItem(i);
			if (!isEmpty(stack)) {
				stacks.add(stack);
			}
		}
		editing.setItems(stacks);
		KitConfiguration.getInstance().updateProperty(KitNodes.KIT_PRESET);
	}

	private boolean isEmpty(final ItemStack stack) {
		return stack == null || stack.getType() == Material.AIR;
	}

}
