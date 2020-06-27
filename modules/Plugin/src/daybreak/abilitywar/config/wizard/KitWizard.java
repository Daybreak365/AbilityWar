package daybreak.abilitywar.config.wizard;

import daybreak.abilitywar.ability.AbilityFactory.AbilityRegistration;
import daybreak.abilitywar.config.Configuration;
import daybreak.abilitywar.config.Configuration.Settings;
import daybreak.abilitywar.config.enums.ConfigNodes;
import daybreak.abilitywar.utils.library.MaterialX;
import daybreak.abilitywar.utils.library.item.ItemBuilder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

public class KitWizard extends SettingWizard {

	private static final ItemStack confirm = new ItemBuilder()
			.type(MaterialX.LIME_WOOL)
			.displayName(ChatColor.GREEN + "확인")
			.build();
	private static final ItemStack reset = new ItemBuilder()
			.type(MaterialX.RED_WOOL)
			.displayName(ChatColor.RED + "초기화")
			.build();

	private final AbilityRegistration registration;

	public KitWizard(Player player, Plugin plugin, AbilityRegistration registration) {
		super(player, 45, "§2§l" + registration.getManifest().name() + " 킷 설정", plugin);
		this.registration = registration;
	}

	public KitWizard(Player player, Plugin plugin) {
		super(player, 45, "§2§l게임 킷 설정", plugin);
		this.registration = null;
	}

	@Override
	void openGUI(Inventory gui) {
		for (int c = 36; c <= 44; c++) {
			switch (c) {
				case 39:
					gui.setItem(c, reset);
					break;
				case 40:
					gui.setItem(c, confirm);
					break;
				default:
					gui.setItem(c, DECO);
					break;
			}
		}

		gui.addItem((registration == null ? Settings.getDefaultKit() : Settings.getAbilityKit().getKits(registration.getAbilityClass().getName())).toArray(new ItemStack[0]));

		player.openInventory(gui);
	}

	@Override
	void onClick(InventoryClickEvent e, Inventory gui) {
		Player p = (Player) e.getWhoClicked();
		ItemStack currentItem = e.getCurrentItem();
		if (currentItem != null) {
			if (e.getSlot() >= 36)
				e.setCancelled(true);
			if (currentItem.hasItemMeta() && currentItem.getItemMeta().hasDisplayName()) {
				switch (currentItem.getItemMeta().getDisplayName()) {
					case "§a확인":
						List<ItemStack> stacks = new ArrayList<>();
						for (int i = 0; i <= 35; i++) {
							if (gui.getItem(i) != null && !gui.getItem(i).getType().equals(Material.AIR)) {
								stacks.add(gui.getItem(i));
							}
						}
						if (registration == null) {
							Configuration.modifyProperty(ConfigNodes.GAME_KIT, stacks);
							p.sendMessage("§2게임 킷 §a설정을 마쳤습니다.");
						} else {
							Settings.getAbilityKit().setKits(registration.getAbilityClass().getName(), stacks);
							Configuration.updateProperty(ConfigNodes.GAME_ABILITY_KIT);
							p.sendMessage("§2" + registration.getManifest().name() + " 킷 §a설정을 마쳤습니다.");
						}
						p.closeInventory();
						break;
					case "§c초기화":
						if (registration == null) {
							Configuration.modifyProperty(ConfigNodes.GAME_KIT, Collections.emptyList());
							p.sendMessage("§2게임 킷 §a설정이 초기화되었습니다.");
						} else {
							Settings.getAbilityKit().setKits(registration.getAbilityClass().getName(), Collections.emptyList());
							Configuration.updateProperty(ConfigNodes.GAME_ABILITY_KIT);
							p.sendMessage("§2" + registration.getManifest().name() + " 킷 §a설정이 초기화되었습니다.");
						}
						p.closeInventory();
						break;
				}
			}
		}
	}

}
