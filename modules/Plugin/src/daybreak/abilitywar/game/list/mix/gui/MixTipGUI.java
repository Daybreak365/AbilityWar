package daybreak.abilitywar.game.list.mix.gui;

import daybreak.abilitywar.ability.AbilityBase;
import daybreak.abilitywar.ability.AbilityFactory.AbilityRegistration;
import daybreak.abilitywar.ability.AbilityFactory.AbilityRegistration.Flag;
import daybreak.abilitywar.ability.AbilityManifest;
import daybreak.abilitywar.game.list.mix.Mix;
import daybreak.abilitywar.game.manager.gui.PagedGUI;
import daybreak.abilitywar.game.manager.gui.tip.AbilityTipGUI;
import daybreak.abilitywar.utils.base.Messager;
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
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;

import java.util.Iterator;
import java.util.List;
import java.util.StringJoiner;

public class MixTipGUI implements Listener, PagedGUI {

	private static final ItemStack NO_ABILITY = new ItemBuilder(MaterialX.BARRIER)
			.displayName(ChatColor.RED + "능력이 없습니다.")
			.build();

	private static final ItemStack DECO = new ItemBuilder(MaterialX.GRAY_STAINED_GLASS_PANE)
			.displayName("§f")
			.build();

	private final Plugin plugin;
	private final Player player;
	private final AbilityRegistration first, second;
	private Inventory gui;

	public MixTipGUI(Player player, Mix mix, Plugin plugin) {
		this.plugin = plugin;
		this.player = player;
		final AbilityBase first = mix.getFirst();
		this.first = first != null ? first.getRegistration() : null;
		final AbilityBase second = mix.getSecond();
		this.second = second != null ? second.getRegistration() : null;
		Bukkit.getPluginManager().registerEvents(this, plugin);
	}

	@Override
	public int getCurrentPage() {
		return 1;
	}

	public void openGUI() {
		this.gui = Bukkit.createInventory(null, InventoryType.HOPPER, "§0능력 팁");
		gui.setItem(0, createItem(first));
		gui.setItem(4, createItem(second));
		for (int i = 1; i <= 3; i++) {
			gui.setItem(i, DECO);
		}
		player.openInventory(gui);
	}

	@Override
	public void openGUI(int page) {
		openGUI();
	}

	private ItemStack createItem(AbilityRegistration registration) {
		if (registration == null) return NO_ABILITY;
		final AbilityManifest manifest = registration.getManifest();
		final ItemStack stack = MaterialX.IRON_BLOCK.createItem();
		final ItemMeta meta = stack.getItemMeta();
		meta.setDisplayName("§b" + manifest.name());
		final StringJoiner joiner = new StringJoiner(ChatColor.WHITE + ", ");
		if (registration.hasFlag(Flag.ACTIVE_SKILL)) joiner.add(ChatColor.GREEN + "액티브");
		if (registration.hasFlag(Flag.TARGET_SKILL)) joiner.add(ChatColor.GOLD + "타게팅");
		if (registration.hasFlag(Flag.BETA)) joiner.add(ChatColor.DARK_AQUA + "베타");
		final List<String> lore = Messager.asList(
				"§f등급: " + manifest.rank().getRankName(),
				"§f종류: " + manifest.species().getSpeciesName(),
				joiner.toString(),
				"", "§7※ 팁을 보려면 클릭하세요.", "");
		for (final Iterator<String> iterator = AbilityBase.getExplanation(registration); iterator.hasNext();) {
			lore.add(ChatColor.WHITE.toString().concat(iterator.next()));
		}
		meta.setLore(lore);
		stack.setItemMeta(meta);
		return stack;
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
				if (currentItem.getType() == Material.IRON_BLOCK) {
					if (e.getSlot() == 0) {
						new AbilityTipGUI(player, first, this, plugin).openGUI(1);
					} else if (e.getSlot() == 4) {
						new AbilityTipGUI(player, second, this, plugin).openGUI(1);
					}
				}
			}
		}
	}

}
