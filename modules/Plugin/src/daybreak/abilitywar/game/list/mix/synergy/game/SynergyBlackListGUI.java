package daybreak.abilitywar.game.list.mix.synergy.game;

import com.google.common.collect.Table.Cell;
import daybreak.abilitywar.ability.AbilityFactory.AbilityRegistration;
import daybreak.abilitywar.ability.AbilityFactory.AbilityRegistration.Flag;
import daybreak.abilitywar.ability.AbilityManifest;
import daybreak.abilitywar.config.Configuration;
import daybreak.abilitywar.config.Configuration.Settings;
import daybreak.abilitywar.game.list.mix.synergy.SynergyFactory;
import daybreak.abilitywar.game.manager.AbilityList;
import daybreak.abilitywar.utils.base.logging.Logger;
import daybreak.abilitywar.utils.base.minecraft.item.builder.ItemBuilder;
import daybreak.abilitywar.utils.library.MaterialX;
import daybreak.abilitywar.utils.library.SoundLib;
import daybreak.abilitywar.utils.library.item.ItemLib;
import daybreak.abilitywar.utils.library.item.ItemLib.ItemColor;
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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.StringJoiner;
import java.util.TreeSet;
import java.util.logging.Level;

public class SynergyBlackListGUI implements Listener {

	private static final ItemStack PREVIOUS_PAGE = new ItemBuilder(MaterialX.ARROW)
			.displayName(ChatColor.AQUA + "이전 페이지")
			.build();

	private static final ItemStack NEXT_PAGE = new ItemBuilder(MaterialX.ARROW)
			.displayName(ChatColor.AQUA + "다음 페이지")
			.build();

	private static final Logger logger = Logger.getLogger(SynergyBlackListGUI.class.getName());

	private static final Set<Cell<AbilityRegistration, AbilityRegistration, AbilityRegistration>> synergies = new TreeSet<>(new Comparator<Cell<AbilityRegistration, AbilityRegistration, AbilityRegistration>>() {
		@Override
		public int compare(Cell<AbilityRegistration, AbilityRegistration, AbilityRegistration> o1, Cell<AbilityRegistration, AbilityRegistration, AbilityRegistration> o2) {
			return String.CASE_INSENSITIVE_ORDER.compare(o1.getValue().getManifest().name(), o2.getValue().getManifest().name());
		}
	});

	private final Player player;
	private int playerPage = 1;
	private Inventory gui;

	public SynergyBlackListGUI(Player player, Plugin Plugin) {
		this.player = player;
		Bukkit.getPluginManager().registerEvents(this, Plugin);
		checkCache();
	}

	private static void checkCache() {
		final Set<Cell<AbilityRegistration, AbilityRegistration, AbilityRegistration>> cellSet = SynergyFactory.cellSet();
		if (synergies.size() == cellSet.size()) return;
		synergies.clear();
		synergies.addAll(cellSet);
	}

	public Set<String> getAbilityNames() {
		Set<String> set = new TreeSet<>();
		set.addAll(AbilityList.nameValues());
		set.addAll(Settings.getBlackList());
		return set;
	}

	public void openGUI(int page) {
		int maxPage = ((synergies.size() - 1) / 36) + 1;
		if (maxPage < page) page = 1;
		if (page < 1) page = 1;
		gui = Bukkit.createInventory(null, 36, "§c§l✖ §8§l시너지 블랙리스트 §c§l✖");
		playerPage = page;
		int count = 0;

		for (Cell<AbilityRegistration, AbilityRegistration, AbilityRegistration> cell : synergies) {
			if (count / 36 == page - 1) {
				final AbilityRegistration synergy = cell.getValue();
				final String name = synergy.getManifest().name();
				final ItemStack stack;
				if (Settings.isBlacklisted(name)) {
					stack = ItemLib.WOOL.getItemStack(ItemColor.RED);
					final ItemMeta meta = stack.getItemMeta();
					meta.setDisplayName("§b" + name);
					List<String> lore = new ArrayList<>();
					lore.add("§7" + cell.getRowKey().getManifest().name() + " §f+ §7" + cell.getColumnKey().getManifest().name());
					final StringJoiner joiner = new StringJoiner(ChatColor.WHITE + ", ");
					if (synergy.hasFlag(Flag.ACTIVE_SKILL)) joiner.add(ChatColor.GREEN + "액티브");
					if (synergy.hasFlag(Flag.TARGET_SKILL)) joiner.add(ChatColor.GOLD + "타게팅");
					if (synergy.hasFlag(Flag.BETA)) joiner.add(ChatColor.DARK_AQUA + "베타");
					final AbilityManifest manifest = synergy.getManifest();
					lore.add("§f등급: " + manifest.rank().getRankName());
					lore.add("§f종류: " + manifest.species().getSpeciesName());
					lore.add(joiner.toString());
					lore.add("");
					lore.add("§7이 능력은 능력을 추첨할 때 예외됩니다.");
					lore.add("§b» §f예외 처리를 해제하려면 클릭하세요.");
					meta.setLore(lore);
					stack.setItemMeta(meta);
				} else {
					stack = ItemLib.WOOL.getItemStack(ItemColor.LIME);
					final ItemMeta meta = stack.getItemMeta();
					meta.setDisplayName("§b" + name);
					List<String> lore = new ArrayList<>();
					lore.add("§7" + cell.getRowKey().getManifest().name() + " §f+ §7" + cell.getColumnKey().getManifest().name());
					final StringJoiner joiner = new StringJoiner(ChatColor.WHITE + ", ");
					if (synergy.hasFlag(Flag.ACTIVE_SKILL)) joiner.add(ChatColor.GREEN + "액티브");
					if (synergy.hasFlag(Flag.TARGET_SKILL)) joiner.add(ChatColor.GOLD + "타게팅");
					if (synergy.hasFlag(Flag.BETA)) joiner.add(ChatColor.DARK_AQUA + "베타");
					final AbilityManifest manifest = synergy.getManifest();
					lore.add("§f등급: " + manifest.rank().getRankName());
					lore.add("§f종류: " + manifest.species().getSpeciesName());
					lore.add(joiner.toString());
					lore.add("");
					lore.add("§7이 능력은 능력을 추첨할 때 예외되지 않습니다.");
					lore.add("§b» §f예외 처리를 하려면 클릭하세요.");
					meta.setLore(lore);
					stack.setItemMeta(meta);
				}
				gui.setItem(count % 36, stack);
			}
			count++;
		}

		if (page > 1) gui.setItem(30, PREVIOUS_PAGE);
		if (page != maxPage) gui.setItem(32, NEXT_PAGE);

		final ItemStack stack = new ItemStack(Material.PAPER, 1);
		final ItemMeta meta = stack.getItemMeta();
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
			final ItemStack currentItem = e.getCurrentItem();
			if (currentItem != null && currentItem.hasItemMeta() && currentItem.getItemMeta().hasDisplayName()) {
				if (currentItem.getItemMeta().getDisplayName().equals(ChatColor.AQUA + "이전 페이지")) {
					openGUI(playerPage - 1);
				} else if (currentItem.getItemMeta().getDisplayName().equals(ChatColor.AQUA + "다음 페이지")) {
					openGUI(playerPage + 1);
				} else {
					if (ItemLib.WOOL.compareType(currentItem.getType())) {
						final String stripItemName = ChatColor.stripColor(currentItem.getItemMeta().getDisplayName());
						if (MaterialX.RED_WOOL.compare(currentItem)) {
							Settings.removeBlacklist(stripItemName);
							SoundLib.ENTITY_EXPERIENCE_ORB_PICKUP.playSound(player);
							openGUI(playerPage);
						} else if (MaterialX.LIME_WOOL.compare(currentItem)) {
							Settings.addBlacklist(stripItemName);
							SoundLib.BLOCK_ANVIL_LAND.playSound(player);
							openGUI(playerPage);
						}
					}
				}
			}
		}
	}

	private void blacklist(ClickType clickType, Collection<String> abilityNames) {
		if (clickType == ClickType.LEFT) {
			Settings.addBlacklist(abilityNames);
			SoundLib.BLOCK_ANVIL_LAND.playSound(player);
		} else if (clickType == ClickType.RIGHT) {
			Settings.removeBlacklist(abilityNames);
			SoundLib.ENTITY_EXPERIENCE_ORB_PICKUP.playSound(player);
		}
		openGUI(playerPage);
	}

}