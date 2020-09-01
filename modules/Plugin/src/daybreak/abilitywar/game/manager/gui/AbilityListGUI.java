package daybreak.abilitywar.game.manager.gui;

import daybreak.abilitywar.ability.AbilityBase;
import daybreak.abilitywar.ability.AbilityFactory.AbilityRegistration;
import daybreak.abilitywar.ability.AbilityFactory.AbilityRegistration.Flag;
import daybreak.abilitywar.ability.AbilityManifest;
import daybreak.abilitywar.game.manager.AbilityList;
import daybreak.abilitywar.utils.base.Messager;
import daybreak.abilitywar.utils.base.minecraft.item.builder.ItemBuilder;
import daybreak.abilitywar.utils.library.MaterialX;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.StringJoiner;
import java.util.TreeMap;
import java.util.concurrent.CompletableFuture;
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

public class AbilityListGUI implements Listener {

	private static final ItemStack PREVIOUS_PAGE = new ItemBuilder(MaterialX.ARROW)
			.displayName(ChatColor.AQUA + "이전 페이지")
			.build();

	private static final ItemStack NEXT_PAGE = new ItemBuilder(MaterialX.ARROW)
			.displayName(ChatColor.AQUA + "다음 페이지")
			.build();

	private final Player player;
	private final Map<String, AbilityRegistration> values;
	private int currentPage = 1;
	private Inventory abilityGUI;
	private CompletableFuture<Void> asyncWork;

	public AbilityListGUI(Player player, Plugin plugin) {
		this.player = player;
		Bukkit.getPluginManager().registerEvents(this, plugin);

		values = new TreeMap<>();
		for (AbilityRegistration registration : AbilityList.values()) {
			values.put(registration.getManifest().name(), registration);
		}
	}

	public void openGUI(int page) {
		if (asyncWork != null) {
			asyncWork.cancel(true);
			this.asyncWork = null;
		}
		int maxPage = ((values.size() - 1) / 36) + 1;
		if (maxPage < page) page = 1;
		if (page < 1) page = 1;
		abilityGUI = Bukkit.createInventory(null, 54, "§cAbilityWar §e능력 목록");
		this.currentPage = page;
		int count = 0;

		for (Entry<String, AbilityRegistration> entry : values.entrySet()) {
			if (count / 36 == page - 1) {
				final AbilityRegistration registration = entry.getValue();
				final AbilityManifest manifest = registration.getManifest();
				final ItemStack stack = MaterialX.WHITE_STAINED_GLASS.createItem();
				final ItemMeta meta = stack.getItemMeta();
				meta.setDisplayName("§b" + manifest.name());
				final StringJoiner joiner = new StringJoiner(ChatColor.WHITE + ", ");
				if (registration.hasFlag(Flag.ACTIVE_SKILL)) joiner.add(ChatColor.GREEN + "액티브");
				if (registration.hasFlag(Flag.TARGET_SKILL)) joiner.add(ChatColor.GOLD + "타겟팅");
				if (registration.hasFlag(Flag.BETA)) joiner.add(ChatColor.DARK_AQUA + "베타");
				final List<String> lore = Messager.asList(
						"§f등급: " + manifest.rank().getRankName(),
						"§f종류: " + manifest.species().getSpeciesName(),
						joiner.toString(),
						"");
				for (final String line : registration.getManifest().explain()) {
					lore.add(ChatColor.WHITE.toString().concat(line));
				}
				meta.setLore(lore);
				stack.setItemMeta(meta);
				abilityGUI.setItem(count % 36, stack);
			}
			count++;
		}

		{
			final int finalPage = page;
			this.asyncWork = CompletableFuture.runAsync(new Runnable() {
				@Override
				public void run() {
					int count = 0;

					for (Entry<String, AbilityRegistration> entry : values.entrySet()) {
						if (currentPage != finalPage) break;
						if (count / 36 == finalPage - 1) {
							final AbilityRegistration registration = entry.getValue();
							final AbilityManifest manifest = registration.getManifest();
							final ItemStack stack = new ItemStack(Material.IRON_BLOCK);
							final ItemMeta meta = stack.getItemMeta();
							meta.setDisplayName("§b" + manifest.name());
							final StringJoiner joiner = new StringJoiner(ChatColor.WHITE + ", ");
							if (registration.hasFlag(Flag.ACTIVE_SKILL)) joiner.add(ChatColor.GREEN + "액티브");
							if (registration.hasFlag(Flag.TARGET_SKILL)) joiner.add(ChatColor.GOLD + "타겟팅");
							if (registration.hasFlag(Flag.BETA)) joiner.add(ChatColor.DARK_AQUA + "베타");
							final List<String> lore = Messager.asList(
									"§f등급: " + manifest.rank().getRankName(),
									"§f종류: " + manifest.species().getSpeciesName(),
									joiner.toString(),
									"");
							for (final Iterator<String> iterator = AbilityBase.getExplanation(registration); iterator.hasNext();) {
								lore.add(ChatColor.WHITE.toString().concat(iterator.next()));
							}
							meta.setLore(lore);
							stack.setItemMeta(meta);
							if (currentPage != finalPage) break;
							abilityGUI.setItem(count % 36, stack);
						}
						count++;
					}
				}
			});
		}

		if (page > 1) {
			abilityGUI.setItem(48, PREVIOUS_PAGE);
		}

		if (page != maxPage) {
			abilityGUI.setItem(50, NEXT_PAGE);
		}

		ItemStack stack = new ItemStack(Material.PAPER, 1);
		ItemMeta meta = stack.getItemMeta();
		meta.setDisplayName("§6페이지 §e" + page + " §6/ §e" + maxPage);
		stack.setItemMeta(meta);
		abilityGUI.setItem(49, stack);

		player.openInventory(abilityGUI);
	}

	@EventHandler
	private void onInventoryClose(InventoryCloseEvent e) {
		if (e.getInventory().equals(this.abilityGUI)) {
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
		if (e.getInventory().equals(abilityGUI)) {
			e.setCancelled(true);
			if (e.getCurrentItem() != null && e.getCurrentItem().hasItemMeta() && e.getCurrentItem().getItemMeta().hasDisplayName() && e.getCurrentItem().getType() != Material.IRON_BLOCK) {
				String displayName = ChatColor.stripColor(e.getCurrentItem().getItemMeta().getDisplayName());
				if (displayName.equals("이전 페이지")) {
					openGUI(currentPage - 1);
				} else if (displayName.equals("다음 페이지")) {
					openGUI(currentPage + 1);
				}
			}
		}
	}

}