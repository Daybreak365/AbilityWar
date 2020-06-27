package daybreak.abilitywar.config.ability.wizard;

import daybreak.abilitywar.ability.AbilityFactory;
import daybreak.abilitywar.ability.AbilityFactory.AbilityRegistration;
import daybreak.abilitywar.config.ability.AbilitySettings.SettingObject;
import daybreak.abilitywar.config.ability.wizard.setter.Setter;
import daybreak.abilitywar.utils.library.MaterialX;
import java.util.Map;
import java.util.TreeMap;
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

public class AbilitySettingWizard implements Listener {

	private final Player player;
	private final Map<String, AbilityRegistration> abilities;
	private AbilityRegistration currentAbility = null;

	public AbilitySettingWizard(Player player, Plugin plugin) {
		this.player = player;
		Map<String, AbilityRegistration> abilities = new TreeMap<>();
		for (AbilityRegistration registration : AbilityFactory.getRegistrations()) {
			if (registration.getSettingObjects().size() > 0) {
				abilities.put(registration.getManifest().name(), registration);
			}
		}
		this.abilities = abilities;
		Bukkit.getPluginManager().registerEvents(this, plugin);
	}

	private int currentPage;
	private Inventory gui;

	public void openGUI(int page) {
		if (currentAbility == null) {
			this.gui = Bukkit.createInventory(null, 54, "능력 목록");
			int maxPage = ((abilities.size() - 1) / 36) + 1;
			if (maxPage < page || page < 1) page = 1;
			this.currentPage = page;

			int count = 0;
			for (String ability : abilities.keySet()) {
				if (count / 36 == page - 1) {
					ItemStack stack = new ItemStack(Material.BOOK);
					ItemMeta meta = stack.getItemMeta();
					meta.setDisplayName("§b" + ability);
					stack.setItemMeta(meta);

					gui.setItem(count % 36, stack);
				}
				count++;
			}

			if (page > 1) {
				ItemStack stack = new ItemStack(Material.ARROW, 1);
				ItemMeta meta = stack.getItemMeta();
				meta.setDisplayName(ChatColor.AQUA + "이전 페이지");
				stack.setItemMeta(meta);
				gui.setItem(48, stack);
			}

			if (page != maxPage) {
				ItemStack stack = new ItemStack(Material.ARROW, 1);
				ItemMeta meta = stack.getItemMeta();
				meta.setDisplayName(ChatColor.AQUA + "다음 페이지");
				stack.setItemMeta(meta);
				gui.setItem(50, stack);
			}

			ItemStack stack = new ItemStack(Material.PAPER, 1);
			ItemMeta meta = stack.getItemMeta();
			meta.setDisplayName("§6페이지 §e" + page + " §6/ §e" + maxPage);
			stack.setItemMeta(meta);
			gui.setItem(49, stack);

			player.openInventory(gui);
		} else {
			this.gui = Bukkit.createInventory(null, 27, currentAbility.getManifest().name());

			int maxPage = ((currentAbility.getSettingObjects().size() - 1) / 18) + 1;
			if (maxPage < page || page < 1) page = 1;
			this.currentPage = page;

			int count = 0;
			for (SettingObject<?> settingObject : currentAbility.getSettingObjects().values()) {
				if (count / 18 == page - 1) {
					gui.setItem(count % 18, Setter.getInstance(settingObject).getItem(settingObject));
				}
				count++;
			}

			if (page > 1) {
				ItemStack stack = new ItemStack(Material.ARROW, 1);
				ItemMeta meta = stack.getItemMeta();
				meta.setDisplayName(ChatColor.AQUA + "이전 페이지");
				stack.setItemMeta(meta);
				gui.setItem(21, stack);
			}

			if (page != maxPage) {
				ItemStack stack = new ItemStack(Material.ARROW, 1);
				ItemMeta meta = stack.getItemMeta();
				meta.setDisplayName(ChatColor.AQUA + "다음 페이지");
				stack.setItemMeta(meta);
				gui.setItem(23, stack);
			}

			ItemStack stack = new ItemStack(Material.PAPER, 1);
			ItemMeta meta = stack.getItemMeta();
			meta.setDisplayName("§6페이지 §e" + page + " §6/ §e" + maxPage);
			stack.setItemMeta(meta);
			gui.setItem(22, stack);

			ItemStack quit = MaterialX.OAK_DOOR.parseItem();
			ItemMeta quitMeta = stack.getItemMeta();
			quitMeta.setDisplayName("§c나가기");
			quit.setItemMeta(quitMeta);
			gui.setItem(26, quit);

			player.openInventory(gui);
		}
	}

	@EventHandler
	private void onInventoryClose(InventoryCloseEvent e) {
		if (e.getInventory().equals(this.gui)) {
			HandlerList.unregisterAll(this);
		}
	}

	@EventHandler
	private void onInventoryClick(InventoryClickEvent e) {
		if (e.getInventory().equals(gui)) {
			e.setCancelled(true);

			ItemStack clicked = e.getCurrentItem();
			if (clicked != null && !clicked.getType().equals(Material.AIR) && clicked.hasItemMeta() && clicked.getItemMeta().hasDisplayName()) {
				String stripName = ChatColor.stripColor(clicked.getItemMeta().getDisplayName());
				if (clicked.getType().equals(Material.ARROW)) {
					if (stripName.equals("이전 페이지")) {
						openGUI(currentPage - 1);
						return;
					} else if (stripName.equals("다음 페이지")) {
						openGUI(currentPage + 1);
						return;
					}
				}
				if (currentAbility == null) {
					if (abilities.containsKey(stripName)) {
						this.currentAbility = abilities.get(stripName);
						openGUI(1);
					}
				} else {
					if (MaterialX.OAK_DOOR.compareType(clicked) && stripName.equals("나가기")) {
						currentAbility = null;
						openGUI(1);
					} else {
						SettingObject<?> settingObject = currentAbility.getSettingObjects().get(stripName);
						if (settingObject != null) {
							if (Setter.getInstance(settingObject).onClick(settingObject, e.getClick()))
								openGUI(currentPage);
						}
					}
				}
			}
		}
	}

}
