package daybreak.abilitywar.game.list.mix;

import daybreak.abilitywar.ability.AbilityBase;
import daybreak.abilitywar.ability.AbilityFactory.AbilityRegistration;
import daybreak.abilitywar.ability.AbilityFactory.AbilityRegistration.Flag;
import daybreak.abilitywar.ability.AbilityManifest;
import daybreak.abilitywar.game.AbstractGame;
import daybreak.abilitywar.game.AbstractGame.GameUpdate;
import daybreak.abilitywar.game.AbstractGame.Observer;
import daybreak.abilitywar.game.AbstractGame.Participant;
import daybreak.abilitywar.game.GameManager;
import daybreak.abilitywar.game.manager.AbilityList;
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
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.InvocationTargetException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.StringJoiner;
import java.util.TreeMap;
import java.util.concurrent.CompletableFuture;

public class MixAbilityGUI implements Listener, Observer {

	private static final ItemStack PREVIOUS_PAGE = new ItemBuilder(MaterialX.ARROW)
			.displayName(ChatColor.AQUA + "이전 페이지")
			.build();

	private static final ItemStack NEXT_PAGE = new ItemBuilder(MaterialX.ARROW)
			.displayName(ChatColor.AQUA + "다음 페이지")
			.build();

	private static final ItemStack REMOVE_ABILITY = new ItemBuilder(MaterialX.BARRIER)
			.displayName(ChatColor.RED + "능력 제거")
			.build();

	private final Player player;
	private final Participant target;
	private final Map<String, AbilityRegistration> values;
	private int currentPage = 1;
	private Inventory abilityGUI;
	private AbilityRegistration firstAbility = null;
	private CompletableFuture<Void> asyncWork;

	public MixAbilityGUI(@NotNull final Player player, @Nullable final Participant target, @NotNull final AbstractGame game, @NotNull final Plugin plugin) {
		this.player = player;
		this.target = target;
		game.attachObserver(this);
		Bukkit.getPluginManager().registerEvents(this, plugin);

		values = new TreeMap<>();
		for (AbilityRegistration registration : AbilityList.values()) {
			values.put(registration.getManifest().name(), registration);
		}
	}

	public MixAbilityGUI(@NotNull final Player player, @NotNull final AbstractGame game, @NotNull final Plugin plugin) {
		this(player, null, game, plugin);
	}

	public void openGUI(int page) {
		if (asyncWork != null) {
			asyncWork.cancel(true);
			this.asyncWork = null;
		}
		int maxPage = ((values.size() - 1) / 36) + 1;
		if (maxPage < page)
			page = 1;
		if (page < 1)
			page = 1;
		abilityGUI = Bukkit.createInventory(null, 54, "§c능력 부여");
		currentPage = page;
		int count = 0;

		for (Entry<String, AbilityRegistration> entry : values.entrySet()) {
			if (count / 36 == page - 1) {
				AbilityRegistration registration = entry.getValue();
				AbilityManifest manifest = registration.getManifest();
				ItemStack stack = MaterialX.LIGHT_BLUE_STAINED_GLASS.createItem();
				ItemMeta meta = stack.getItemMeta();
				meta.setDisplayName("§b" + manifest.name());
				StringJoiner joiner = new StringJoiner(ChatColor.WHITE + ", ");
				if (registration.hasFlag(Flag.ACTIVE_SKILL)) joiner.add(ChatColor.GREEN + "액티브");
				if (registration.hasFlag(Flag.TARGET_SKILL)) joiner.add(ChatColor.GOLD + "타게팅");
				if (registration.hasFlag(Flag.BETA)) joiner.add(ChatColor.DARK_AQUA + "베타");
				final List<String> lore = Messager.asList(
						"§f등급: " + manifest.rank().getRankName(),
						"§f종류: " + manifest.species().getSpeciesName(),
						joiner.toString(),
						"");
				for (final String line : registration.getManifest().explain()) {
					lore.add(ChatColor.WHITE.toString().concat(line));
				}
				lore.add("");
				lore.add("§2» §f이 능력을 부여하려면 클릭하세요.");
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
							AbilityRegistration registration = entry.getValue();
							AbilityManifest manifest = registration.getManifest();
							ItemStack stack = new ItemStack(Material.DIAMOND_BLOCK);
							ItemMeta meta = stack.getItemMeta();
							meta.setDisplayName("§b" + manifest.name());
							StringJoiner joiner = new StringJoiner(ChatColor.WHITE + ", ");
							if (registration.hasFlag(Flag.ACTIVE_SKILL)) joiner.add(ChatColor.GREEN + "액티브");
							if (registration.hasFlag(Flag.TARGET_SKILL)) joiner.add(ChatColor.GOLD + "타게팅");
							if (registration.hasFlag(Flag.BETA)) joiner.add(ChatColor.DARK_AQUA + "베타");
							final List<String> lore = Messager.asList(
									"§f등급: " + manifest.rank().getRankName(),
									"§f종류: " + manifest.species().getSpeciesName(),
									joiner.toString(),
									"");
							for (final Iterator<String> iterator = AbilityBase.getExplanation(registration); iterator.hasNext();) {
								lore.add(ChatColor.WHITE.toString().concat(iterator.next()));
							}
							lore.add("");
							lore.add("§2» §f이 능력을 부여하려면 클릭하세요.");
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

		abilityGUI.setItem(45, REMOVE_ABILITY);

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
			final Player player = (Player) e.getWhoClicked();
			final ItemStack currentItem = e.getCurrentItem();
			e.setCancelled(true);
			if (currentItem != null && currentItem.hasItemMeta() && currentItem.getItemMeta().hasDisplayName()) {
				if (currentItem.getType() == Material.DIAMOND_BLOCK || MaterialX.LIGHT_BLUE_STAINED_GLASS.compare(currentItem)) {
					AbilityRegistration registration = values.get(ChatColor.stripColor(currentItem.getItemMeta().getDisplayName()));
					if (firstAbility == null) {
						this.firstAbility = registration;
						openGUI(1);
					} else {
						try {
							if (registration != null) {
								if (GameManager.isGameRunning()) {
									AbstractGame game = GameManager.getGame();
									if (target != null) {
										Mix mix = (Mix) target.getAbility();
										mix.setAbility(firstAbility.getAbilityClass(), registration.getAbilityClass());
										Bukkit.broadcastMessage("§e" + player.getName() + "§a님이 §f" + target.getPlayer().getName() + "§a님에게 능력을 임의로 부여하였습니다.");
									} else {
										for (Participant participant : game.getParticipants()) {
											Mix mix = (Mix) participant.getAbility();
											mix.setAbility(firstAbility.getAbilityClass(), registration.getAbilityClass());
										}
										Bukkit.broadcastMessage("§e" + player.getName() + "§a님이 §f모든 참가자§a에게 능력을 임의로 부여하였습니다.");
									}
								}
							}
						} catch (SecurityException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
							ex.printStackTrace();
							if (ex.getMessage() != null && !ex.getMessage().isEmpty()) {
								Messager.sendErrorMessage(player, ex.getMessage());
							} else {
								Messager.sendErrorMessage(player, "설정 도중 오류가 발생하였습니다.");
							}
						}
						player.closeInventory();
					}
				} else {
					if (currentItem.getItemMeta().getDisplayName().equals(ChatColor.AQUA + "이전 페이지")) {
						openGUI(currentPage - 1);
					} else if (currentItem.getItemMeta().getDisplayName().equals(ChatColor.AQUA + "다음 페이지")) {
						openGUI(currentPage + 1);
					} else if (currentItem.getItemMeta().getDisplayName().equals(ChatColor.RED + "능력 제거")) {
						if (GameManager.isGameRunning()) {
							AbstractGame game = GameManager.getGame();
							if (target != null) {
								Mix mix = (Mix) target.getAbility();
								mix.removeAbility();
								Bukkit.broadcastMessage("§e" + player.getName() + "§a님이 §f" + target.getPlayer().getName() + "§a님의 능력을 제거하였습니다.");
							} else {
								for (Participant participant : game.getParticipants()) {
									Mix mix = (Mix) participant.getAbility();
									mix.removeAbility();
								}
								Bukkit.broadcastMessage("§e" + player.getName() + "§a님이 §f모든 참가자§a의 능력을 제거하였습니다.");
							}
						}
						player.closeInventory();
					}
				}
			}
		}
	}

	@Override
	public void update(GameUpdate update) {
		if (update == GameUpdate.END) {
			player.closeInventory();
		}
	}
}
