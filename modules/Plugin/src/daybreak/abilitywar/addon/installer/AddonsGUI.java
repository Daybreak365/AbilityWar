package daybreak.abilitywar.addon.installer;

import daybreak.abilitywar.addon.Addon;
import daybreak.abilitywar.addon.AddonLoader;
import daybreak.abilitywar.addon.installer.info.AddonInfo;
import daybreak.abilitywar.addon.installer.info.AddonInfo.AddonVersion;
import daybreak.abilitywar.addon.installer.info.AddonInfo.Link;
import daybreak.abilitywar.addon.installer.info.Addons;
import daybreak.abilitywar.utils.base.Formatter;
import daybreak.abilitywar.utils.base.minecraft.item.Skulls;
import daybreak.abilitywar.utils.library.MaterialX;
import daybreak.abilitywar.utils.library.item.ItemBuilder;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
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

public class AddonsGUI implements Listener {

	private static final ItemStack PREVIOUS_PAGE = new ItemBuilder()
			.type(Material.ARROW)
			.displayName(ChatColor.AQUA + "이전 페이지")
			.build();

	private static final ItemStack NEXT_PAGE = new ItemBuilder()
			.type(Material.ARROW)
			.displayName(ChatColor.AQUA + "다음 페이지")
			.build();

	private static final ItemStack QUIT = new ItemBuilder()
			.type(MaterialX.SPRUCE_DOOR)
			.displayName(ChatColor.DARK_AQUA + "나가기")
			.build();

	private final Player player;
	private final Addons addons;
	private AddonInfo addonInfo;
	private boolean versionsLookup = false;
	private Inventory gui;

	public AddonsGUI(Player player, Addons addons, Plugin plugin) {
		this.player = player;
		this.addons = addons;
		Bukkit.getPluginManager().registerEvents(this, plugin);
	}

	private int currentPage = 1;

	public void openGUI(int page) {
		if (addonInfo == null) {
			this.gui = Bukkit.createInventory(null, 27, "§0§l추천 애드온 목록");
			final Collection<AddonInfo> addonInfos = addons.getAddonInfos();
			final int maxPage = ((addonInfos.size() - 1) / 18) + 1;
			if (maxPage < page || page < 1) page = 1;
			currentPage = page;
			int count = 0;
			for (AddonInfo addonInfo : addonInfos) {
				final ItemStack stack = Skulls.createSkull(addonInfo.getIcon());
				final ItemMeta meta = stack.getItemMeta();
				meta.setDisplayName("§b" + addonInfo.getDisplayName());
				final Addon addon = AddonLoader.getAddon(addonInfo.getName());
				final List<String> lore = new ArrayList<>();
				if (addon != null) {
					for (String s : addonInfo.getDescription()) {
						lore.add(ChatColor.GRAY + s);
					}
					lore.add("");
					lore.add("§3● §b개발자§f: " + addonInfo.getDeveloper());
					if (addon.getDescription().getVersion().equals(addonInfo.getLatest().getVersion())) {
						lore.add("§2✔ §a애드온이 설치되어 있습니다.");
					} else {
						lore.add("§2✔ §a애드온이 설치되어 있습니다. §8(§7업데이트가 있습니다.§8)");
					}
				} else {
					for (String s : addonInfo.getDescription()) {
						lore.add(ChatColor.GRAY + s);
					}
					lore.add("");
					lore.add("§3● §b개발자§f: " + addonInfo.getDeveloper());
					lore.add("§4✖ §c애드온이 설치되어 있지 않습니다.");
				}
				lore.add("§8● §7추가 정보를 보려면 클릭하세요.");
				meta.setLore(lore);
				stack.setItemMeta(meta);
				if (count / 18 == page - 1) gui.setItem(count % 18, stack);
				count++;
			}

			if (page > 1) gui.setItem(21, PREVIOUS_PAGE);
			if (page != maxPage) gui.setItem(23, NEXT_PAGE);

			final ItemStack stack = new ItemStack(Material.PAPER, 1);
			final ItemMeta meta = stack.getItemMeta();
			meta.setDisplayName("§6페이지 §e" + page + " §6/ §e" + maxPage);
			stack.setItemMeta(meta);
			gui.setItem(22, stack);

			player.openInventory(gui);
		} else {
			if (versionsLookup) {
				this.gui = Bukkit.createInventory(null, 27, "§0§l" + addonInfo.getDisplayName() + " 버전 목록 §0(§8맨 앞이 가장 최신 버전§0)");
				final int maxPage = ((addonInfo.getVersions().size() - 1) / 18) + 1;
				if (maxPage < page || page < 1) page = 1;
				currentPage = page;
				int count = 0;
				for (AddonVersion addonVersion : addonInfo.getVersions()) {
					final ItemStack stack = new ItemStack(Material.BOOK);
					final ItemMeta meta = stack.getItemMeta();
					meta.setDisplayName("§b" + addonVersion.getTag());
					final List<String> lore = new ArrayList<>();
					lore.add("§3● §b버전§f: " + addonVersion.getTag() + " §f(§7v" + addonVersion.getName() + "§f) " + "(§7" + (addonVersion.getFileSize() / 1024) + "KB§f)");
					lore.add("");
					int descCount = 0;
					for (String update : addonVersion.getUpdates()) {
						if (descCount++ >= 5) {
							lore.add("§8...");
							break;
						}
						lore.add("§7" + update);
					}
					lore.add("");
					lore.add("§8● §7이 버전을 설치하려면 클릭하세요.");
					meta.setLore(lore);
					stack.setItemMeta(meta);
					if (count / 18 == page - 1) gui.setItem(count % 18, stack);
					count++;
				}

				gui.setItem(18, QUIT);
				if (page > 1) gui.setItem(21, PREVIOUS_PAGE);
				if (page != maxPage) gui.setItem(23, NEXT_PAGE);

				final ItemStack stack = new ItemStack(Material.PAPER, 1);
				final ItemMeta meta = stack.getItemMeta();
				meta.setDisplayName("§6페이지 §e" + page + " §6/ §e" + maxPage);
				stack.setItemMeta(meta);
				gui.setItem(22, stack);

				player.openInventory(gui);
			} else {
				this.gui = Bukkit.createInventory(null, InventoryType.HOPPER, "§0§l" + addonInfo.getDisplayName());
				{
					final ItemStack stack = Skulls.createCustomSkull("109cde1afc95a474d222554097ed6d391e7cc7ae1f202fdbfd2d6dbc98309370");
					final ItemMeta meta = stack.getItemMeta();
					meta.setDisplayName("§3개발자");
					meta.setLore(Arrays.asList(
							"§f" + addonInfo.getDeveloper()
					));
					stack.setItemMeta(meta);
					gui.setItem(0, stack);
				}
				{
					final ItemStack stack = Skulls.createCustomSkull("be9ae7a4be65fcbaee65181389a2f7d47e2e326db59ea3eb789a92c85ea46");
					final ItemMeta meta = stack.getItemMeta();
					meta.setDisplayName("§3최신 버전 설치");
					final AddonVersion latest = addonInfo.getLatest();
					meta.setLore(Arrays.asList(
							"§3● §b버전§f: " + latest.getTag() + " §f(§7v" + latest.getName() + "§f) " + "(§7" + (latest.getFileSize() / 1024) + "KB§f)",
							"§8● §7설치하려면 클릭하세요."
					));
					stack.setItemMeta(meta);
					gui.setItem(1, stack);
				}
				{
					final ItemStack stack = Skulls.createCustomSkull("b33598437e313329eb141a13e92d9b0349aabe5c6482a5dde7b73753634aba");
					final ItemMeta meta = stack.getItemMeta();
					meta.setDisplayName("§3버전 목록");
					meta.setLore(Collections.singletonList("§8● §7확인하려면 클릭하세요."));
					stack.setItemMeta(meta);
					gui.setItem(2, stack);
				}
				{
					final ItemStack stack = Skulls.createCustomSkull("c69196b330c6b8962f23ad5627fb6ecce472eaf5c9d44f791f6709c7d0f4dece");
					final ItemMeta meta = stack.getItemMeta();
					meta.setDisplayName("§3개발자 링크");
					meta.setLore(Collections.singletonList("§8● §7확인하려면 클릭하세요."));
					stack.setItemMeta(meta);
					gui.setItem(3, stack);
				}
				gui.setItem(4, QUIT);
				player.openInventory(gui);

			}
		}
	}

	@EventHandler
	private void onInventoryClose(final InventoryCloseEvent e) {
		if (e.getInventory().equals(gui)) {
			HandlerList.unregisterAll(this);
		}
	}

	@EventHandler
	private void onQuit(final PlayerQuitEvent e) {
		if (e.getPlayer().getUniqueId().equals(player.getUniqueId())) {
			HandlerList.unregisterAll(this);
		}
	}

	@EventHandler
	private void onInventoryClick(final InventoryClickEvent e) {
		if (e.getInventory().equals(gui)) {
			e.setCancelled(true);
			if (e.getCurrentItem() != null && e.getCurrentItem().hasItemMeta() && e.getCurrentItem().getItemMeta().hasDisplayName()) {
				final String displayName = e.getCurrentItem().getItemMeta().getDisplayName();
				if (addonInfo == null) {
					switch (displayName) {
						case "§b이전 페이지":
							openGUI(currentPage - 1);
							break;
						case "§b다음 페이지":
							openGUI(currentPage + 1);
							break;
						default:
							if (MaterialX.PLAYER_HEAD.compare(e.getCurrentItem())) {
								this.addonInfo = addons.getAddonInfo(ChatColor.stripColor(displayName));
								if (this.addonInfo != null) {
									versionsLookup = false;
									openGUI(1);
								}
							}
							break;
					}
				} else {
					if (versionsLookup) {
						if (displayName.equals("§3나가기")) {
							versionsLookup = false;
							openGUI(1);
						} else {
							final AddonVersion addonVersion = addonInfo.getVersion(ChatColor.stripColor(displayName));
							if (addonVersion != null) {
								player.closeInventory();
								try {
									addonInfo.getVersions().get(addonInfo.getVersions().size() - 1).install();
								} catch (IOException ex) {
									ex.printStackTrace();
								}
							}
						}
					} else {
						switch (displayName) {
							case "§3최신 버전 설치":
								player.closeInventory();
								try {
									addonInfo.getLatest().install();
								} catch (IOException ex) {
									ex.printStackTrace();
								}
								break;
							case "§3버전 목록":
								versionsLookup = true;
								openGUI(1);
								break;
							case "§3개발자 링크":
								player.closeInventory();
								player.sendMessage(Formatter.formatTitle(32, ChatColor.DARK_AQUA, ChatColor.WHITE, "개발자 링크"));
								final List<Link> links = addonInfo.getDeveloperLinks();
								for (Link link : links) {
									player.sendMessage("§3" + link.getName() + "§f: " + link.getLink());
								}
								player.sendMessage("§3--------------------------------");
								break;
							case "§3나가기":
								addonInfo = null;
								openGUI(1);
								break;
						}
					}
				}
			}
		}
	}

}
