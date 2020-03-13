package daybreak.abilitywar.game.manager.gui;

import daybreak.abilitywar.ability.AbilityFactory.AbilityRegistration;
import daybreak.abilitywar.ability.AbilityFactory.AbilityRegistration.Flag;
import daybreak.abilitywar.ability.AbilityManifest;
import daybreak.abilitywar.game.AbstractGame;
import daybreak.abilitywar.game.AbstractGame.Participant;
import daybreak.abilitywar.game.manager.AbilityList;
import daybreak.abilitywar.utils.base.Messager;
import daybreak.abilitywar.utils.base.RegexReplacer;
import daybreak.abilitywar.utils.base.reflect.ReflectionUtil;
import daybreak.abilitywar.utils.library.item.ItemBuilder;
import daybreak.abilitywar.utils.thread.AbilityWarThread;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.StringJoiner;
import java.util.TreeMap;
import java.util.function.Function;
import java.util.regex.MatchResult;

/**
 * 능력 부여 GUI
 */
public class AbilityGUI implements Listener {

	private static final ItemStack PREVIOUS_PAGE = new ItemBuilder()
			.type(Material.ARROW)
			.displayName(ChatColor.translateAlternateColorCodes('&', "&b이전 페이지"))
			.build();

	private static final ItemStack NEXT_PAGE = new ItemBuilder()
			.type(Material.ARROW)
			.displayName(ChatColor.translateAlternateColorCodes('&', "&b다음 페이지"))
			.build();

	private static final RegexReplacer SQUARE_BRACKET = new RegexReplacer("\\$\\[([^\\[\\]]+)\\]");
	private static final RegexReplacer ROUND_BRACKET = new RegexReplacer("\\$\\(([^()]]+)\\)");

	private final Player p;
	private final Participant target;

	public AbilityGUI(Player p, Participant target, Plugin plugin) {
		this.p = p;
		this.target = target;
		Bukkit.getPluginManager().registerEvents(this, plugin);

		values = new TreeMap<>();
		for (AbilityRegistration registration : AbilityList.values()) {
			values.put(registration.getManifest().name(), registration);
		}
	}

	public AbilityGUI(Player player, Plugin plugin) {
		this(player, null, plugin);
	}

	private final Map<String, AbilityRegistration> values;
	private int currentPage = 1;
	private Inventory abilityGUI;

	public void openGUI(int page) {
		int maxPage = ((values.size() - 1) / 36) + 1;
		if (maxPage < page)
			page = 1;
		if (page < 1)
			page = 1;
		abilityGUI = Bukkit.createInventory(null, 54, ChatColor.translateAlternateColorCodes('&', "&cAbilityWar &e능력 목록"));
		currentPage = page;
		int count = 0;

		for (Entry<String, AbilityRegistration> entry : values.entrySet()) {
			if (count / 36 == page - 1) {
				AbilityRegistration registration = entry.getValue();
				AbilityManifest manifest = registration.getManifest();
				ItemStack stack = new ItemStack(Material.IRON_BLOCK);
				ItemMeta meta = stack.getItemMeta();
				meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&b" + manifest.name()));
				StringJoiner joiner = new StringJoiner(ChatColor.WHITE + ", ");
				if (registration.hasFlag(Flag.ACTIVE_SKILL)) joiner.add(ChatColor.GREEN + "액티브");
				if (registration.hasFlag(Flag.TARGET_SKILL)) joiner.add(ChatColor.GOLD + "타겟팅");
				if (registration.hasFlag(Flag.BETA)) joiner.add(ChatColor.DARK_AQUA + "베타");
				List<String> lore = Messager.asList(
						ChatColor.translateAlternateColorCodes('&', "&f등급: " + manifest.rank().getRankName()),
						ChatColor.translateAlternateColorCodes('&', "&f종류: " + manifest.species().getSpeciesName()),
						joiner.toString(),
						"");
				Function<MatchResult, String> valueProvider = new Function<MatchResult, String>() {
					@Override
					public String apply(MatchResult matchResult) {
						Field field = registration.getFields().get(matchResult.group(1));
						if (field != null) {
							if (Modifier.isStatic(field.getModifiers())) {
								try {
									return String.valueOf(ReflectionUtil.setAccessible(field).get(null));
								} catch (IllegalAccessException ignored) {
								}
							}
						}
						return "?";
					}
				};
				for (String explain : manifest.explain()) {
					lore.add(ChatColor.WHITE.toString().concat(ROUND_BRACKET.replaceAll(SQUARE_BRACKET.replaceAll(explain, valueProvider), valueProvider)));
				}
				lore.add("");
				lore.add(ChatColor.translateAlternateColorCodes('&', "&2» &f이 능력을 부여하려면 클릭하세요."));
				meta.setLore(lore);
				stack.setItemMeta(meta);
				abilityGUI.setItem(count % 36, stack);
			}
			count++;
		}

		if (page > 1) {
			abilityGUI.setItem(48, PREVIOUS_PAGE);
		}

		if (page != maxPage) {
			abilityGUI.setItem(50, NEXT_PAGE);
		}

		ItemStack stack = new ItemStack(Material.PAPER, 1);
		ItemMeta meta = stack.getItemMeta();
		meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&6페이지 &e" + page + " &6/ &e" + maxPage));
		stack.setItemMeta(meta);
		abilityGUI.setItem(49, stack);

		p.openInventory(abilityGUI);
	}

	@EventHandler
	private void onInventoryClose(InventoryCloseEvent e) {
		if (e.getInventory().equals(this.abilityGUI)) {
			HandlerList.unregisterAll(this);
		}
	}

	@EventHandler
	private void onInventoryClick(InventoryClickEvent e) {
		if (e.getInventory().equals(abilityGUI)) {
			Player p = (Player) e.getWhoClicked();
			e.setCancelled(true);
			if (e.getCurrentItem() != null && e.getCurrentItem().hasItemMeta() && e.getCurrentItem().getItemMeta().hasDisplayName()) {
				if (e.getCurrentItem().getType().equals(Material.IRON_BLOCK)) {
					AbilityRegistration registration = values.get(ChatColor.stripColor(e.getCurrentItem().getItemMeta().getDisplayName()));
					try {
						if (registration != null) {
							if (AbilityWarThread.isGameTaskRunning()) {
								AbstractGame game = AbilityWarThread.getGame();
								if (target != null) {
									target.setAbility(registration.getAbilityClass());
									Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&', "&e" + p.getName() + "&a님이 &f" + target.getPlayer().getName() + "&a님에게 능력을 임의로 부여하였습니다."));
								} else {
									for (Participant participant : game.getParticipants()) {
										participant.setAbility(registration.getAbilityClass());
									}
									Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&', "&e" + p.getName() + "&a님이 &f전체 유저&a에게 능력을 임의로 부여하였습니다."));
								}
							}
						}
					} catch (SecurityException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
						if (ex.getMessage() != null && !ex.getMessage().isEmpty()) {
							Messager.sendErrorMessage(p, ex.getMessage());
						} else {
							Messager.sendErrorMessage(p, "설정 도중 오류가 발생하였습니다.");
						}
					}
					p.closeInventory();
				} else {
					if (e.getCurrentItem().getItemMeta().getDisplayName().equals(ChatColor.translateAlternateColorCodes('&', "&b이전 페이지"))) {
						openGUI(currentPage - 1);
					} else if (e.getCurrentItem().getItemMeta().getDisplayName().equals(ChatColor.translateAlternateColorCodes('&', "&b다음 페이지"))) {
						openGUI(currentPage + 1);
					}
				}
			}
		}
	}

}