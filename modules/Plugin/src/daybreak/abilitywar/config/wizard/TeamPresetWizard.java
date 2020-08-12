package daybreak.abilitywar.config.wizard;

import daybreak.abilitywar.AbilityWar;
import daybreak.abilitywar.config.Configuration;
import daybreak.abilitywar.config.Configuration.Settings;
import daybreak.abilitywar.config.enums.ConfigNodes;
import daybreak.abilitywar.config.serializable.SpawnLocation;
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

public class TeamPresetWizard implements Listener {

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
			.type(MaterialX.SPRUCE_DOOR)
			.displayName(ChatColor.AQUA + "나가기")
			.build();

	private final Player player;
	private TeamPreset editing = null;
	private State state = State.GUI;
	private int playerPage = 1;
	private Inventory gui;
	private String schemeName = "";

	public TeamPresetWizard(Player player, Plugin Plugin) {
		this.player = player;
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
				final ItemStack stack = MaterialX.WHITE_WOOL.createItem();
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
					stack = MaterialX.RED_WOOL.createItem();
				} else if (displayName.contains(ChatColor.GOLD.toString())) {
					stack = MaterialX.ORANGE_WOOL.createItem();
				} else if (displayName.contains(ChatColor.YELLOW.toString())) {
					stack = MaterialX.YELLOW_WOOL.createItem();
				} else if (displayName.contains(ChatColor.GREEN.toString())) {
					stack = MaterialX.LIME_WOOL.createItem();
				} else if (displayName.contains(ChatColor.DARK_GREEN.toString())) {
					stack = MaterialX.GREEN_WOOL.createItem();
				} else if (displayName.contains(ChatColor.AQUA.toString()) || displayName.contains(ChatColor.DARK_AQUA.toString())) {
					stack = MaterialX.LIGHT_BLUE_WOOL.createItem();
				} else if (displayName.contains(ChatColor.BLUE.toString()) || displayName.contains(ChatColor.DARK_BLUE.toString())) {
					stack = MaterialX.BLUE_WOOL.createItem();
				} else if (displayName.contains(ChatColor.LIGHT_PURPLE.toString())) {
					stack = MaterialX.MAGENTA_WOOL.createItem();
				} else if (displayName.contains(ChatColor.DARK_PURPLE.toString())) {
					stack = MaterialX.PURPLE_WOOL.createItem();
				} else if (displayName.contains(ChatColor.GRAY.toString())) {
					stack = MaterialX.LIGHT_GRAY_WOOL.createItem();
				} else if (displayName.contains(ChatColor.DARK_GRAY.toString())) {
					stack = MaterialX.GRAY_WOOL.createItem();
				} else if (displayName.contains(ChatColor.BLACK.toString())) {
					stack = MaterialX.BLACK_WOOL.createItem();
				} else {
					stack = MaterialX.WHITE_WOOL.createItem();
				}
				final ItemMeta meta = stack.getItemMeta();
				meta.setDisplayName(ChatColor.WHITE + scheme.getName());
				final SpawnLocation spawnLocation = scheme.getSpawn();
				meta.setLore(Arrays.asList(
						scheme.getDisplayName(), "", "§6» " + ChatColor.RED + "삭제" + ChatColor.WHITE + "하려면 §b우클릭§f하세요.",
						"",
						"§6» §f스폰 위치를 내 위치로 §a설정§f하려면 §b좌클릭§f하세요.",
						"§6» §f스폰 위치를 §c초기화§f하려면 §bSHIFT + 좌클릭§f하세요.", "",
						"§3현재 팀 스폰 위치",
						"§b월드 §7: §f" + spawnLocation.world,
						"§bX §7: §f" + spawnLocation.x,
						"§bY §7: §f" + spawnLocation.y,
						"§bZ §7: §f" + spawnLocation.z
				));
				stack.setItemMeta(meta);

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
		player.openInventory(gui);
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
		if (e.getPlayer().getUniqueId().equals(player.getUniqueId())) {
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
		if (e.getPlayer().getUniqueId().equals(player.getUniqueId())) {
			e.setCancelled(true);
			switch (state) {
				case NEW_PRESET: {
					final String name = e.getMessage();
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
						} else player.sendMessage(ChatColor.GREEN + name + ChatColor.WHITE + KoreanUtil.getJosa(name, Josa.은는) + " 이미 존재하는 프리셋 이름입니다.");
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
					final String name = e.getMessage();
					if (!name.equals("!")) {
						if (!editing.hasScheme(name)) {
							if (name.length() <= 12) {
								this.schemeName = name;
								this.state = State.NEW_SCHEME_DISPLAY_NAME;
								player.sendMessage(ChatColor.WHITE + "프리셋에 새로 추가할 " + ChatColor.GREEN + "팀" + ChatColor.WHITE + "의 " + ChatColor.YELLOW + "별명" + ChatColor.WHITE + "을 채팅에 입력해주세요. " + ChatColor.DARK_GRAY + "(" + ChatColor.GRAY + "취소하려면 '!'를 입력해주세요." + ChatColor.DARK_GRAY + ")");
								player.sendMessage(ChatColor.DARK_GRAY + "(" + ChatColor.GRAY + "§로 색코드 사용 가능" + ChatColor.DARK_GRAY + ")");
							} else player.sendMessage(ChatColor.WHITE + "프리셋 이름은 " + ChatColor.GREEN + "13 글자 " + ChatColor.WHITE + "이상으로 할 수 없습니다.");
						} else {
							player.sendMessage(ChatColor.GREEN + name + ChatColor.WHITE + KoreanUtil.getJosa(name, Josa.은는) + " 프리셋에 이미 존재하는 팀 이름입니다.");
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
					final String name = e.getMessage();
					if (!name.equals("!") && !schemeName.isEmpty()) {
						if (name.length() <= 12) {
							editing.addScheme(new TeamScheme(schemeName, ChatColor.translateAlternateColorCodes('&', name)));
							this.state = State.GUI;
							new BukkitRunnable() {
								@Override
								public void run() {
									openGUI(1);
								}
							}.runTask(AbilityWar.getPlugin());
						} else {
							player.sendMessage(ChatColor.WHITE + "프리셋 별명은 " + ChatColor.GREEN + "13 글자 " + ChatColor.WHITE + "이상으로 할 수 없습니다.");
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
							player.sendMessage(ChatColor.WHITE + "새로 추가할 " + ChatColor.GREEN + "프리셋" + ChatColor.WHITE + "의 " + ChatColor.YELLOW + "이름" + ChatColor.WHITE + "을 채팅에 입력해주세요. " + ChatColor.DARK_GRAY + "(" + ChatColor.GRAY + "취소하려면 '!'를 입력해주세요." + ChatColor.DARK_GRAY + ")");
							player.closeInventory();
						} else if (e.getClick() == ClickType.LEFT) {
							String stripName = ChatColor.stripColor(displayName);
							if (MaterialX.WHITE_WOOL.compare(e.getCurrentItem())) {
								this.editing = Settings.getPresetContainer().getPreset(stripName);
								openGUI(1);
							}
						} else if (e.getClick() == ClickType.SHIFT_LEFT) {
							String stripName = ChatColor.stripColor(displayName);
							if (MaterialX.WHITE_WOOL.compare(e.getCurrentItem())) {
								final TeamPreset preset = Settings.getPresetContainer().getPreset(stripName);
								preset.setDivisionType(preset.getDivisionType().next());
								openGUI(1);
							}
						} else if (e.getClick() == ClickType.RIGHT) {
							String stripName = ChatColor.stripColor(displayName);
							if (MaterialX.WHITE_WOOL.compare(e.getCurrentItem())) {
								Settings.getPresetContainer().removePreset(stripName);
								openGUI(1);
							}
						}
					} else {
						if (displayName.equals(ChatColor.GREEN + "팀 추가")) {
							this.state = State.NEW_SCHEME_NAME;
							player.sendMessage(ChatColor.WHITE + "프리셋에 새로 추가할 " + ChatColor.GREEN + "팀" + ChatColor.WHITE + "의 " + ChatColor.YELLOW + "이름" + ChatColor.WHITE + "을 채팅에 입력해주세요. " + ChatColor.DARK_GRAY + "(" + ChatColor.GRAY + "취소하려면 '!'를 입력해주세요." + ChatColor.DARK_GRAY + ")");
							player.closeInventory();
						} else if (displayName.equals(ChatColor.AQUA + "나가기")) {
							this.editing = null;
							openGUI(1);
						} else if (e.getClick() == ClickType.RIGHT) {
							editing.removeScheme(ChatColor.stripColor(displayName));
							openGUI(1);
						} else if (e.getClick() == ClickType.LEFT) {
							editing.getScheme(ChatColor.stripColor(displayName)).setSpawn(player.getLocation());
							openGUI(playerPage);
						} else if (e.getClick() == ClickType.SHIFT_LEFT) {
							editing.getScheme(ChatColor.stripColor(displayName)).setSpawn(null);
							openGUI(playerPage);
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
