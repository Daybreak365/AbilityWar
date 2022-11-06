package daybreak.abilitywar.game.specialthanks;

import daybreak.abilitywar.AbilityWar;
import daybreak.abilitywar.game.specialthanks.SpecialThanks.Category;
import daybreak.abilitywar.game.specialthanks.SpecialThanks.SpecialThank;
import daybreak.abilitywar.utils.base.minecraft.item.Skulls;
import daybreak.abilitywar.utils.base.minecraft.item.builder.ItemBuilder;
import daybreak.abilitywar.utils.library.MaterialX;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class SpecialThanksGUI implements Listener {

	private static final ItemStack CANT_SCROLL = new ItemBuilder(MaterialX.BLACK_STAINED_GLASS_PANE)
			.displayName("§f넘길 수 없음")
			.build();

	private static final ItemStack GRAY_PANE = new ItemBuilder(MaterialX.GRAY_STAINED_GLASS_PANE)
			.displayName("§f")
			.build();

	private static final ItemStack SCROLL_LEFT = new ItemBuilder(MaterialX.LIME_STAINED_GLASS_PANE)
			.displayName("§f앞으로 넘기기")
			.lore("§7앞으로 넘기려면 클릭하세요.")
			.build();

	private static final ItemStack SCROLL_RIGHT = new ItemBuilder(MaterialX.LIME_STAINED_GLASS_PANE)
			.displayName("§f뒤로 넘기기")
			.lore("§7뒤로 넘기려면 클릭하세요.")
			.build();

	public static void openGUI(final Player player) {
		new SpecialThanksGUI(player).openGUI();
	}

	private final Player player;
	private SpecialThanks.Category category = null;
	private Inventory gui;
	private int offset = 0;

	private SpecialThanksGUI(final Player player) {
		this.player = player;
		Bukkit.getPluginManager().registerEvents(this, AbilityWar.getPlugin());
	}

	public void scrollRight() {
		this.offset = Math.min(Math.max(0, category.getSpecialThanks().size() - 5), offset + 1);
	}

	public void scrollLeft() {
		this.offset = Math.max(0, offset - 1);
	}

	public void openGUI() {
		if (this.category == null) {
			this.gui = Bukkit.createInventory(null, InventoryType.HOPPER, "§c§l✿ §0§l고마운 분들 §c§l✿");
			final Category[] categories = SpecialThanks.categories;
			for (int i = 0; i < categories.length; i++) {
				final Category category = categories[i];
				final ItemStack stack = category.getIcon();
				final ItemMeta meta = stack.getItemMeta();
				meta.setDisplayName(category.getDisplayName());
				meta.setLore(Collections.singletonList("§7이 분야의 고마운 분들을 확인하려면 클릭하세요."));
				stack.setItemMeta(meta);
				gui.setItem(i, stack);
			}
		} else {
			this.gui = Bukkit.createInventory(null, 9, "§c§l✿ §0§l고마운 분들 §c§l✿");
			gui.setItem(0, offset > 0 ? SCROLL_LEFT : CANT_SCROLL);
			gui.setItem(1, GRAY_PANE);
			final List<SpecialThank> specialThanks = category.getSpecialThanks();
			for (int i = 0; i < 5; i++) {
				final int index = offset + i;
				if (specialThanks.size() > index) {
					final SpecialThank specialThank = specialThanks.get(index);
					final String name = specialThank.getName();
					final ItemStack stack = name == null ? MaterialX.BARRIER.createItem() : Skulls.createSkull(name);
					final ItemMeta meta = stack.getItemMeta();
					if (name == null) {
						meta.setDisplayName("§c???");
						meta.setLore(Arrays.asList("§7모장 API에 연결 중 문제가 발생했거나", "§7아직 불러와지지 않았습니다.", "§7GUI를 다시 열어보세요."));
					} else {
						meta.setDisplayName("§b" + name);
						meta.setLore(Arrays.asList(specialThank.getDescription()));
					}
					stack.setItemMeta(meta);
					gui.setItem(2 + i, stack);
				}
			}
			gui.setItem(7, GRAY_PANE);
			gui.setItem(8, (category.getSpecialThanks().size() - 5) > offset ? SCROLL_RIGHT : CANT_SCROLL);
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
	private void onInventoryClick(final InventoryClickEvent e) {
		if (e.getInventory().equals(this.gui)) {
			e.setCancelled(true);
			final ItemStack currentItem = e.getCurrentItem();
			final int slot = e.getSlot();
			if (category == null) {
				if (currentItem == null) return;
				if (slot >= SpecialThanks.categories.length) return;
				this.category = SpecialThanks.categories[slot];
				openGUI();
			} else {
				if (currentItem == null) {
					this.category = null;
					this.offset = 0;
					openGUI();
				} else {
					if (MaterialX.LIME_STAINED_GLASS_PANE.compare(currentItem)) {
						if (slot == 0) {
							scrollLeft();
						} else if (slot == 8) {
							scrollRight();
						}
						openGUI();
					}
				}
			}
		}
	}

}
