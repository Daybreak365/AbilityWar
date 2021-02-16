package daybreak.abilitywar.game.manager.gui;

import daybreak.abilitywar.ability.AbilityFactory;
import daybreak.abilitywar.ability.AbilityFactory.AbilityRegistration;
import daybreak.abilitywar.addon.Addon;
import daybreak.abilitywar.game.manager.effect.registry.EffectManifest;
import daybreak.abilitywar.game.manager.effect.registry.EffectRegistry;
import daybreak.abilitywar.game.manager.effect.registry.EffectRegistry.EffectRegistration;
import daybreak.abilitywar.game.manager.gui.tip.AbilityTipGUI;
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
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

public class EffectsListGUI implements Listener, PagedGUI {

	private static final ItemStack PREVIOUS_PAGE = new ItemBuilder(MaterialX.ARROW)
			.displayName(ChatColor.AQUA + "이전 페이지")
			.build();

	private static final ItemStack NEXT_PAGE = new ItemBuilder(MaterialX.ARROW)
			.displayName(ChatColor.AQUA + "다음 페이지")
			.build();

	private static final ItemStack GLASS_PANE = new ItemBuilder(MaterialX.WHITE_STAINED_GLASS_PANE)
			.displayName(ChatColor.WHITE.toString())
			.build();

	private static Map<String, EffectRegistration<?>> values = new TreeMap<>();

	static {
		for (EffectRegistration<?> registration : EffectRegistry.values()) {
			values.put(registration.getManifest().name(), registration);
		}
	}

	private final Plugin plugin;
	private final Player player;
	private int currentPage = 1;
	private Inventory gui;

	@Override
	public int getCurrentPage() {
		return currentPage;
	}

	public EffectsListGUI(Player player, Plugin plugin) {
		this.plugin = plugin;
		this.player = player;
		Bukkit.getPluginManager().registerEvents(this, plugin);
		if (values.size() != AbilityFactory.getRegistrations().size()) {
			values = new TreeMap<>();
			for (EffectRegistration<?> registration : EffectRegistry.values()) {
				values.put(registration.getManifest().name(), registration);
			}
		}
	}

	@Override
	public void openGUI(int page) {
		int maxPage = ((values.size() - 1) / 9) + 1;
		if (maxPage < page) page = 1;
		if (page < 1) page = 1;
		gui = Bukkit.createInventory(null, 36, "§0상태 이상 목록");
		this.currentPage = page;
		int count = 0;

		for (Entry<String, EffectRegistration<?>> entry : values.entrySet()) {
			if (count / 9 == page - 1) {
				final EffectRegistration<?> registration = entry.getValue();
				final EffectManifest manifest = registration.getManifest();
				final ItemStack stack = MaterialX.IRON_BLOCK.createItem();
				final ItemMeta meta = stack.getItemMeta();
				meta.setDisplayName("§b" + manifest.displayName());
				final String providerName = registration.getProvider() instanceof Addon ? ((Addon) registration.getProvider()).getDisplayName() : null;
				final List<String> lore = new ArrayList<>();
				if (providerName != null) {
					lore.add(providerName);
					lore.add("");
				}
				for (final String line : manifest.description()) {
					lore.add(ChatColor.WHITE.toString().concat(line));
				}
				meta.setLore(lore);
				stack.setItemMeta(meta);
				gui.setItem((count % 9) + 9, stack);
			}
			count++;
		}

		if (page > 1) gui.setItem(21, PREVIOUS_PAGE);
		if (page != maxPage) gui.setItem(23, NEXT_PAGE);

		final ItemStack stack = new ItemStack(Material.PAPER, 1);
		final ItemMeta meta = stack.getItemMeta();
		meta.setDisplayName("§6페이지 §e" + page + " §6/ §e" + maxPage);
		stack.setItemMeta(meta);
		gui.setItem(22, stack);

		for (int i = 0; i <= 8; i++) {
			gui.setItem(i, GLASS_PANE);
		}
		for (int i = 27; i <= 35; i++) {
			gui.setItem(i, GLASS_PANE);
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
			final ItemStack currentItem = e.getCurrentItem();
			if (currentItem != null && currentItem.hasItemMeta() && currentItem.getItemMeta().hasDisplayName()) {
				final String displayName = ChatColor.stripColor(currentItem.getItemMeta().getDisplayName());
				if (currentItem.getType() == Material.IRON_BLOCK || MaterialX.WHITE_STAINED_GLASS.compare(currentItem)) {
					final AbilityRegistration registration = AbilityFactory.getByName(displayName);
					if (registration != null) {
						new AbilityTipGUI(player, registration, this, plugin).openGUI(1);
					}
				} else {
					if (displayName.equals("이전 페이지")) {
						openGUI(currentPage - 1);
					} else if (displayName.equals("다음 페이지")) {
						openGUI(currentPage + 1);
					}
				}
			}
		}
	}

}