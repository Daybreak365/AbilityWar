package daybreak.abilitywar.game.manager.gui;

import daybreak.abilitywar.AbilityWar;
import daybreak.abilitywar.config.Configuration;
import daybreak.abilitywar.config.Configuration.Settings;
import daybreak.abilitywar.config.enums.ConfigNodes;
import daybreak.abilitywar.config.serializable.team.TeamPreset;
import daybreak.abilitywar.config.serializable.team.TeamPreset.DivisionType;
import daybreak.abilitywar.config.serializable.team.TeamPreset.TeamScheme;
import daybreak.abilitywar.utils.base.language.korean.KoreanUtil;
import daybreak.abilitywar.utils.base.language.korean.KoreanUtil.Josa;
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
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

public class TeamPresetGUI implements Listener {

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

	private static final ItemStack NEW_TEAM = new ItemBuilder()
			.type(MaterialX.LIME_WOOL)
			.displayName(ChatColor.GREEN + "팀 추가")
			.build();

	private static final ItemStack QUIT = new ItemBuilder()
			.type(MaterialX.BIRCH_DOOR)
			.displayName(ChatColor.AQUA + "나가기")
			.build();

	private final Player p;
	private TeamPreset editing = null;
	private State state = State.GUI;
	private int playerPage = 1;
	private Inventory gui;
	private String schemeName = "";

	public TeamPresetGUI(Player p, Plugin Plugin) {
		this.p = p;
		Bukkit.getPluginManager().registerEvents(this, Plugin);
	}

	public void openGUI(int page) {
		if (editing == null) {
			int maxPage = ((Settings.getPresetContainer().getPresets().size() - 1) / 36) + 1;
			if (maxPage < page || page < 1) page = 1;
			gui = Bukkit.createInventory(null, 54, "§8§l팀 프리셋 설정");
			playerPage = page;
			int count = 0;

			for (TeamPreset preset : Settings.getPresetContainer().getPresets()) {
				final ItemStack stack = MaterialX.WHITE_WOOL.parseItem();
				ItemMeta im = stack.getItemMeta();
				im.setDisplayName(ChatColor.AQUA + preset.getName());
				List<String> lore = new ArrayList<>(7 + preset.getDivisionType().lore.size() + preset.getSchemes().size());
				lore.add(ChatColor.RED + "삭제" + ChatColor.WHITE + "하려면 우클릭하세요.");
				lore.add(ChatColor.DARK_GREEN + "팀 목록" + ChatColor.WHITE + "을 수정하려면 좌클릭하세요.");
				lore.add(ChatColor.GREEN + "팀원 분배 방식" + ChatColor.WHITE + "을 변경하려면 SHIFT + 좌클릭하세요.");
				lore.add("");
				lore.add(ChatColor.GRAY + "팀원 분배 방식: " + ChatColor.WHITE + preset.getDivisionType().name);
				for (String s : preset.getDivisionType().lore) {
					lore.add(ChatColor.WHITE + s);
				}
				lore.add("");
				lore.add(ChatColor.GRAY + "-------" + ChatColor.WHITE + " 팀 목록 " + ChatColor.GRAY + "-------");
				for (TeamScheme scheme : preset.getSchemes()) {
					lore.add(ChatColor.WHITE + scheme.getName() + " - " + scheme.getDisplayName());
				}
				im.setLore(lore);
				stack.setItemMeta(im);

				if (count / 36 == page - 1) {
					gui.setItem(count % 36, stack);
				}
				count++;
			}

			if (page > 1) gui.setItem(48, PREVIOUS_PAGE);
			if (page != maxPage) gui.setItem(50, NEXT_PAGE);
			gui.setItem(51, NEW_PRESET);

			ItemStack stack = new ItemStack(Material.PAPER, 1);
			ItemMeta meta = stack.getItemMeta();
			meta.setDisplayName("§6페이지 §e" + page + " §6/ §e" + maxPage);
			stack.setItemMeta(meta);
			gui.setItem(49, stack);
		} else {
			int maxPage = ((editing.getSchemes().size() - 1) / 36) + 1;
			if (maxPage < page || page < 1) page = 1;
			gui = Bukkit.createInventory(null, 54, "§8§l" + editing.getName() + " - 팀 목록 설정");
			playerPage = page;
			int count = 0;

			for (TeamScheme scheme : editing.getSchemes()) {
				final ItemStack stack;
				final String displayName = scheme.getDisplayName();
				if (displayName.contains(ChatColor.RED.toString()) || displayName.contains(ChatColor.DARK_RED.toString())) {
					stack = MaterialX.RED_WOOL.parseItem();
				} else if (displayName.contains(ChatColor.GOLD.toString())) {
					stack = MaterialX.ORANGE_WOOL.parseItem();
				} else if (displayName.contains(ChatColor.YELLOW.toString())) {
					stack = MaterialX.YELLOW_WOOL.parseItem();
				} else if (displayName.contains(ChatColor.GREEN.toString())) {
					stack = MaterialX.LIME_WOOL.parseItem();
				} else if (displayName.contains(ChatColor.DARK_GREEN.toString())) {
					stack = MaterialX.GREEN_WOOL.parseItem();
				} else if (displayName.contains(ChatColor.AQUA.toString()) || displayName.contains(ChatColor.DARK_AQUA.toString())) {
					stack = MaterialX.LIGHT_BLUE_WOOL.parseItem();
				} else if (displayName.contains(ChatColor.BLUE.toString()) || displayName.contains(ChatColor.DARK_BLUE.toString())) {
					stack = MaterialX.BLUE_WOOL.parseItem();
				} else if (displayName.contains(ChatColor.LIGHT_PURPLE.toString())) {
					stack = MaterialX.MAGENTA_WOOL.parseItem();
				} else if (displayName.contains(ChatColor.DARK_PURPLE.toString())) {
					stack = MaterialX.PURPLE_WOOL.parseItem();
				} else if (displayName.contains(ChatColor.GRAY.toString())) {
					stack = MaterialX.LIGHT_GRAY_WOOL.parseItem();
				} else if (displayName.contains(ChatColor.DARK_GRAY.toString())) {
					stack = MaterialX.GRAY_WOOL.parseItem();
				} else if (displayName.contains(ChatColor.BLACK.toString())) {
					stack = MaterialX.BLACK_WOOL.parseItem();
				} else {
					stack = MaterialX.WHITE_WOOL.parseItem();
				}
				ItemMeta im = stack.getItemMeta();
				im.setDisplayName(ChatColor.WHITE + scheme.getName());
				im.setLore(Arrays.asList(scheme.getDisplayName(), "", ChatColor.RED + "삭제" + ChatColor.WHITE + "하려면 우클릭하세요."));
				stack.setItemMeta(im);

				if (count / 36 == page - 1) {
					gui.setItem(count % 36, stack);
				}
				count++;
			}

			if (page > 1) gui.setItem(48, PREVIOUS_PAGE);
			if (page != maxPage) gui.setItem(50, NEXT_PAGE);
			gui.setItem(51, NEW_TEAM);
			gui.setItem(53, QUIT);

			ItemStack stack = new ItemStack(Material.PAPER, 1);
			ItemMeta meta = stack.getItemMeta();
			meta.setDisplayName("§6페이지 §e" + page + " §6/ §e" + maxPage);
			stack.setItemMeta(meta);
			gui.setItem(49, stack);
		}
		p.openInventory(gui);
	}

	@EventHandler
	private void onInventoryClose(InventoryCloseEvent e) {
		if (e.getInventory().equals(this.gui) && state == State.GUI) {
			HandlerList.unregisterAll(this);
			Configuration.updateProperty(ConfigNodes.GAME_TEAM_PRESETS);
			try {
				Configuration.update();
			} catch (IOException | InvalidConfigurationException ignored) {
			}
		}
	}

	@EventHandler
	private void onQuit(PlayerQuitEvent e) {
		if (e.getPlayer().getUniqueId().equals(p.getUniqueId())) {
			HandlerList.unregisterAll(this);
			Configuration.updateProperty(ConfigNodes.GAME_TEAM_PRESETS);
			try {
				Configuration.update();
			} catch (IOException | InvalidConfigurationException ignored) {
			}
		}
	}

	@EventHandler
	private void onChat(AsyncPlayerChatEvent e) {
		if (e.getPlayer().getUniqueId().equals(p.getUniqueId())) {
			e.setCancelled(true);
			switch (state) {
				case NEW_PRESET: {
					String name = e.getMessage();
					if (!name.equals("!")) {
						if (!Settings.getPresetContainer().hasPreset(name)) {
							Settings.getPresetContainer().addPreset(new TeamPreset(name, DivisionType.EQUAL));
							this.state = State.GUI;
							new BukkitRunnable() {
								@Override
								public void run() {
									openGUI(1);
								}
							}.runTask(AbilityWar.getPlugin());
						} else {
							p.sendMessage(ChatColor.GREEN + name + ChatColor.WHITE + KoreanUtil.getJosa(name, Josa.은는) + " 이미 존재하는 프리셋 이름입니다.");
						}
					} else {
						this.state = State.GUI;
						new BukkitRunnable() {
							@Override
							public void run() {
								openGUI(1);
							}
						}.runTask(AbilityWar.getPlugin());
					}
					break;
				}
				case NEW_SCHEME_NAME: {
					String name = e.getMessage();
					if (!name.equals("!")) {
						if (!editing.hasScheme(name)) {
							this.schemeName = name;
							this.state = State.NEW_SCHEME_DISPLAY_NAME;
							p.sendMessage(ChatColor.WHITE + "프리셋에 새로 추가할 " + ChatColor.GREEN + "팀" + ChatColor.WHITE + "의 " + ChatColor.YELLOW + "별명" + ChatColor.WHITE + "을 채팅에 입력해주세요. " + ChatColor.DARK_GRAY + "(" + ChatColor.GRAY + "취소하려면 '!'를 입력해주세요." + ChatColor.DARK_GRAY + ")");
							p.sendMessage(ChatColor.DARK_GRAY + "(" + ChatColor.GRAY + "§로 색코드 사용 가능" + ChatColor.DARK_GRAY + ")");
						} else {
							p.sendMessage(ChatColor.GREEN + name + ChatColor.WHITE + KoreanUtil.getJosa(name, Josa.은는) + " 프리셋에 이미 존재하는 팀 이름입니다.");
						}
					} else {
						this.state = State.GUI;
						new BukkitRunnable() {
							@Override
							public void run() {
								openGUI(1);
							}
						}.runTask(AbilityWar.getPlugin());
					}
					break;
				}
				case NEW_SCHEME_DISPLAY_NAME:
					String name = e.getMessage();
					if (!name.equals("!") && !schemeName.equals("")) {
						editing.addScheme(new TeamScheme(schemeName, ChatColor.translateAlternateColorCodes('&', name)));
					}
					this.state = State.GUI;
					new BukkitRunnable() {
						@Override
						public void run() {
							openGUI(1);
						}
					}.runTask(AbilityWar.getPlugin());
					break;
			}
		}
	}

	@EventHandler
	private void onInventoryClick(InventoryClickEvent e) {
		if (e.getInventory().equals(gui)) {
			e.setCancelled(true);
			if (e.getCurrentItem() != null && e.getCurrentItem().hasItemMeta() && e.getCurrentItem().getItemMeta().hasDisplayName()) {
				String displayName = e.getCurrentItem().getItemMeta().getDisplayName();
				if (displayName.equals(ChatColor.AQUA + "이전 페이지")) {
					openGUI(playerPage - 1);
				} else if (displayName.equals(ChatColor.AQUA + "다음 페이지")) {
					openGUI(playerPage + 1);
				} else {
					if (editing == null) {
						if (displayName.equals(ChatColor.GREEN + "프리셋 추가")) {
							this.state = State.NEW_PRESET;
							p.sendMessage(ChatColor.WHITE + "새로 추가할 " + ChatColor.GREEN + "프리셋" + ChatColor.WHITE + "의 " + ChatColor.YELLOW + "이름" + ChatColor.WHITE + "을 채팅에 입력해주세요. " + ChatColor.DARK_GRAY + "(" + ChatColor.GRAY + "취소하려면 '!'를 입력해주세요." + ChatColor.DARK_GRAY + ")");
							p.closeInventory();
						} else if (e.getClick() == ClickType.LEFT) {
							String stripName = ChatColor.stripColor(displayName);
							if (MaterialX.WHITE_WOOL.compareType(e.getCurrentItem())) {
								this.editing = Settings.getPresetContainer().getPreset(stripName);
								openGUI(1);
							}
						} else if (e.getClick() == ClickType.SHIFT_LEFT) {
							String stripName = ChatColor.stripColor(displayName);
							if (MaterialX.WHITE_WOOL.compareType(e.getCurrentItem())) {
								final TeamPreset preset = Settings.getPresetContainer().getPreset(stripName);
								preset.setDivisionType(preset.getDivisionType().next());
								openGUI(1);
							}
						} else if (e.getClick() == ClickType.RIGHT) {
							String stripName = ChatColor.stripColor(displayName);
							if (MaterialX.WHITE_WOOL.compareType(e.getCurrentItem())) {
								Settings.getPresetContainer().removePreset(stripName);
								openGUI(1);
							}
						}
					} else {
						if (displayName.equals(ChatColor.GREEN + "팀 추가")) {
							this.state = State.NEW_SCHEME_NAME;
							p.sendMessage(ChatColor.WHITE + "프리셋에 새로 추가할 " + ChatColor.GREEN + "팀" + ChatColor.WHITE + "의 " + ChatColor.YELLOW + "이름" + ChatColor.WHITE + "을 채팅에 입력해주세요. " + ChatColor.DARK_GRAY + "(" + ChatColor.GRAY + "취소하려면 '!'를 입력해주세요." + ChatColor.DARK_GRAY + ")");
							p.closeInventory();
						} else if (displayName.equals(ChatColor.AQUA + "나가기")) {
							this.editing = null;
							openGUI(1);
						} else if (e.getClick() == ClickType.RIGHT) {
							editing.removeScheme(ChatColor.stripColor(displayName));
							openGUI(1);
						}
					}
				}
			}
		}
	}

	private enum State {
		GUI,
		NEW_PRESET,
		NEW_SCHEME_NAME,
		NEW_SCHEME_DISPLAY_NAME
	}

}
