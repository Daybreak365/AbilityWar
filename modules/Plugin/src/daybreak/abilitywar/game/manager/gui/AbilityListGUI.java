package daybreak.abilitywar.game.manager.gui;

import com.google.common.collect.ImmutableMap;
import daybreak.abilitywar.ability.AbilityBase;
import daybreak.abilitywar.ability.AbilityFactory;
import daybreak.abilitywar.ability.AbilityFactory.AbilityRegistration;
import daybreak.abilitywar.ability.AbilityFactory.AbilityRegistration.Flag;
import daybreak.abilitywar.ability.AbilityManifest;
import daybreak.abilitywar.ability.AbilityManifest.Rank;
import daybreak.abilitywar.ability.AbilityManifest.Species;
import daybreak.abilitywar.game.manager.AbilityList;
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
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;

import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.StringJoiner;
import java.util.TreeSet;
import java.util.concurrent.CompletableFuture;

public class AbilityListGUI implements Listener, PagedGUI {

	private static final ItemStack PREVIOUS_PAGE = new ItemBuilder(MaterialX.ARROW)
			.displayName(ChatColor.AQUA + "이전 페이지")
			.build();

	private static final ItemStack NEXT_PAGE = new ItemBuilder(MaterialX.ARROW)
			.displayName(ChatColor.AQUA + "다음 페이지")
			.build();

	private static final Comparator<AbilityRegistration> nameComparator = new Comparator<AbilityRegistration>() {
		@Override
		public int compare(AbilityRegistration a, AbilityRegistration b) {
			return a.getManifest().name().compareTo(b.getManifest().name());
		}
	}, rankComparator = new Comparator<AbilityRegistration>() {
		@Override
		public int compare(AbilityRegistration a, AbilityRegistration b) {
			final AbilityManifest aManifest = a.getManifest(), bManifest = b.getManifest();
			if (aManifest.rank() != bManifest.rank()) {
				return aManifest.rank().compareTo(bManifest.rank());
			}
			return aManifest.name().compareTo(bManifest.name());
		}
	}, speciesComparator = new Comparator<AbilityRegistration>() {
		@Override
		public int compare(AbilityRegistration a, AbilityRegistration b) {
			final AbilityManifest aManifest = a.getManifest(), bManifest = b.getManifest();
			if (aManifest.species() != bManifest.species()) {
				return aManifest.species().compareTo(bManifest.species());
			}
			return aManifest.name().compareTo(bManifest.name());
		}
	};
	private static Set<AbilityRegistration> byName = new TreeSet<>(nameComparator);
	private static Set<AbilityRegistration> byRank = new TreeSet<>(rankComparator);
	private static Set<AbilityRegistration> bySpecies = new TreeSet<>(speciesComparator);

	static {
		for (AbilityRegistration registration : AbilityList.values()) {
			byName.add(registration);
			byRank.add(registration);
			bySpecies.add(registration);
		}
	}

	public enum SortType {

		NAME("이름", Material.PAPER) {
			@Override
			public Iterable<AbilityRegistration> getIterable() {
				return byName;
			}

			@Override
			public ItemStack getItem(AbilityRegistration registration, boolean withDescription) {
				final AbilityManifest manifest = registration.getManifest();
				final ItemStack stack = (withDescription ? MaterialX.IRON_BLOCK : MaterialX.WHITE_STAINED_GLASS).createItem();
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
				if (withDescription) {
					for (final Iterator<String> iterator = AbilityBase.getExplanation(registration); iterator.hasNext();) {
						lore.add(ChatColor.WHITE.toString().concat(iterator.next()));
					}
				} else {
					for (final String line : registration.getManifest().explain()) {
						lore.add(ChatColor.WHITE.toString().concat(line));
					}
				}
				meta.setLore(lore);
				stack.setItemMeta(meta);
				return stack;
			}

			@Override
			public SortType next() {
				return RANK;
			}
		}, RANK("등급", Material.DIAMOND_SWORD) {

			private final ImmutableMap<Rank, MaterialX> stainedGlass = ImmutableMap.<Rank, MaterialX>builder()
					.put(Rank.C, MaterialX.YELLOW_STAINED_GLASS)
					.put(Rank.B, MaterialX.LIGHT_BLUE_STAINED_GLASS)
					.put(Rank.A, MaterialX.LIME_STAINED_GLASS)
					.put(Rank.S, MaterialX.PINK_STAINED_GLASS)
					.put(Rank.L, MaterialX.ORANGE_STAINED_GLASS)
					.put(Rank.SPECIAL, MaterialX.RED_STAINED_GLASS)
					.build();

			private final ImmutableMap<Rank, MaterialX> concrete = ImmutableMap.<Rank, MaterialX>builder()
					.put(Rank.C, MaterialX.YELLOW_CONCRETE)
					.put(Rank.B, MaterialX.LIGHT_BLUE_CONCRETE)
					.put(Rank.A, MaterialX.LIME_CONCRETE)
					.put(Rank.S, MaterialX.PINK_CONCRETE)
					.put(Rank.L, MaterialX.ORANGE_CONCRETE)
					.put(Rank.SPECIAL, MaterialX.RED_CONCRETE)
					.build();

			@Override
			public Iterable<AbilityRegistration> getIterable() {
				return byRank;
			}

			@Override
			public ItemStack getItem(AbilityRegistration registration, boolean withDescription) {
				final AbilityManifest manifest = registration.getManifest();
				final ItemStack stack = withDescription ? concrete.getOrDefault(manifest.rank(), MaterialX.WHITE_CONCRETE).createItem() : stainedGlass.getOrDefault(manifest.rank(), MaterialX.WHITE_STAINED_GLASS).createItem();
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
				if (withDescription) {
					for (final Iterator<String> iterator = AbilityBase.getExplanation(registration); iterator.hasNext();) {
						lore.add(ChatColor.WHITE.toString().concat(iterator.next()));
					}
				} else {
					for (final String line : registration.getManifest().explain()) {
						lore.add(ChatColor.WHITE.toString().concat(line));
					}
				}
				meta.setLore(lore);
				stack.setItemMeta(meta);
				return stack;
			}

			@Override
			public SortType next() {
				return SPECIES;
			}
		}, SPECIES("종족", MaterialX.SPAWNER.getMaterial()) {

			private final ImmutableMap<Species, MaterialX> stainedGlass = ImmutableMap.<Species, MaterialX>builder()
					.put(Species.ANIMAL, MaterialX.LIME_STAINED_GLASS)
					.put(Species.DEMIGOD, MaterialX.ORANGE_STAINED_GLASS)
					.put(Species.GOD, MaterialX.BLUE_STAINED_GLASS)
					.put(Species.HUMAN, MaterialX.LIGHT_BLUE_STAINED_GLASS)
					.put(Species.UNDEAD, MaterialX.GRAY_STAINED_GLASS)
					.put(Species.OTHERS, MaterialX.WHITE_STAINED_GLASS)
					.put(Species.SPECIAL, MaterialX.RED_STAINED_GLASS)
					.build();

			private final ImmutableMap<Species, MaterialX> concrete = ImmutableMap.<Species, MaterialX>builder()
					.put(Species.ANIMAL, MaterialX.LIME_CONCRETE)
					.put(Species.DEMIGOD, MaterialX.ORANGE_CONCRETE)
					.put(Species.GOD, MaterialX.BLUE_CONCRETE)
					.put(Species.HUMAN, MaterialX.LIGHT_BLUE_CONCRETE)
					.put(Species.UNDEAD, MaterialX.GRAY_CONCRETE)
					.put(Species.OTHERS, MaterialX.WHITE_CONCRETE)
					.put(Species.SPECIAL, MaterialX.RED_CONCRETE)
					.build();

			@Override
			public Iterable<AbilityRegistration> getIterable() {
				return bySpecies;
			}

			@Override
			public ItemStack getItem(AbilityRegistration registration, boolean withDescription) {
				final AbilityManifest manifest = registration.getManifest();
				final ItemStack stack = withDescription ? concrete.getOrDefault(manifest.species(), MaterialX.WHITE_CONCRETE).createItem() : stainedGlass.getOrDefault(manifest.species(), MaterialX.WHITE_STAINED_GLASS).createItem();
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
				if (withDescription) {
					for (final Iterator<String> iterator = AbilityBase.getExplanation(registration); iterator.hasNext();) {
						lore.add(ChatColor.WHITE.toString().concat(iterator.next()));
					}
				} else {
					for (final String line : registration.getManifest().explain()) {
						lore.add(ChatColor.WHITE.toString().concat(line));
					}
				}
				meta.setLore(lore);
				stack.setItemMeta(meta);
				return stack;
			}

			@Override
			public SortType next() {
				return NAME;
			}
		};

		public abstract Iterable<AbilityRegistration> getIterable();
		public abstract ItemStack getItem(AbilityRegistration registration, boolean withDescription);
		public abstract SortType next();

		private final String name;
		private final Material icon;

		SortType(final String name, final Material icon) {
			this.name = name;
			this.icon = icon;
		}

	}

	private final Plugin plugin;
	private final Player player;
	private int currentPage = 1;
	private Inventory gui;
	private SortType sortType = SortType.NAME;
	private CompletableFuture<Void> asyncWork;

	@Override
	public int getCurrentPage() {
		return currentPage;
	}

	public AbilityListGUI(Player player, Plugin plugin) {
		this.plugin = plugin;
		this.player = player;
		Bukkit.getPluginManager().registerEvents(this, plugin);
		if (byName.size() != AbilityFactory.getRegistrations().size()) {
			byName = new TreeSet<>(nameComparator);
			byRank = new TreeSet<>(rankComparator);
			bySpecies = new TreeSet<>(speciesComparator);
			for (AbilityRegistration registration : AbilityList.values()) {
				byName.add(registration);
				byRank.add(registration);
				bySpecies.add(registration);
			}
		}
	}

	@Override
	public void openGUI(int page) {
		if (asyncWork != null) {
			asyncWork.cancel(true);
			this.asyncWork = null;
		}
		int maxPage = ((byName.size() - 1) / 36) + 1;
		if (maxPage < page) page = 1;
		if (page < 1) page = 1;
		gui = Bukkit.createInventory(null, 54, "§0능력 목록");
		this.currentPage = page;
		int count = 0;

		for (AbilityRegistration registration : sortType.getIterable()) {
			if (count / 36 == page - 1) {
				gui.setItem(count % 36, sortType.getItem(registration, false));
			}
			count++;
		}

		{
			final int finalPage = page;
			this.asyncWork = CompletableFuture.runAsync(new Runnable() {
				@Override
				public void run() {
					int count = 0;

					for (AbilityRegistration registration : sortType.getIterable()) {
						if (currentPage != finalPage) break;
						if (count / 36 == finalPage - 1) {
							gui.setItem(count % 36, sortType.getItem(registration, true));
						}
						count++;
					}
				}
			});
		}

		{
			final ItemStack stack = new ItemStack(sortType.icon);
			final ItemMeta meta = stack.getItemMeta();
			meta.setDisplayName("§b정렬 기준§7: §f" + sortType.name);
			meta.setLore(Collections.singletonList("§f정렬 기준을 변경하려면 클릭하세요."));
			stack.setItemMeta(meta);
			gui.setItem(45, stack);
		}

		if (page > 1) {
			gui.setItem(48, PREVIOUS_PAGE);
		}

		if (page != maxPage) {
			gui.setItem(50, NEXT_PAGE);
		}

		ItemStack stack = new ItemStack(Material.PAPER, 1);
		ItemMeta meta = stack.getItemMeta();
		meta.setDisplayName("§6페이지 §e" + page + " §6/ §e" + maxPage);
		stack.setItemMeta(meta);
		gui.setItem(49, stack);

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
				if (e.getSlot() == 45) {
					this.sortType = sortType.next();
					openGUI(currentPage);
					return;
				}
				final String displayName = ChatColor.stripColor(currentItem.getItemMeta().getDisplayName());
				if (e.getSlot() <= 35) {
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