package daybreak.abilitywar.config.wizard;

import daybreak.abilitywar.config.Configuration;
import daybreak.abilitywar.config.Configuration.Settings;
import daybreak.abilitywar.config.enums.ConfigNodes;
import daybreak.abilitywar.utils.library.MaterialLib;
import daybreak.abilitywar.utils.library.item.ItemLib;
import daybreak.abilitywar.utils.library.item.ItemLib.ItemColor;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.Arrays;

public class KitWizard extends SettingWizard {

	private final ItemStack confirm;
	private final ItemStack reset;
	private final ItemStack deco;

	public KitWizard(Player player, Plugin plugin) {
		super(player, 45, ChatColor.translateAlternateColorCodes('&', "&2&l게임 킷 설정"), plugin);
		this.confirm = ItemLib.WOOL.getItemStack(ItemColor.LIME);
		ItemMeta confirmMeta = confirm.getItemMeta();
		confirmMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&a확인"));
		confirm.setItemMeta(confirmMeta);
		this.reset = ItemLib.WOOL.getItemStack(ItemColor.RED);
		ItemMeta resetMeta = reset.getItemMeta();
		resetMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&c초기화"));
		reset.setItemMeta(resetMeta);
		this.deco = MaterialLib.WHITE_STAINED_GLASS_PANE.getItem();
		ItemMeta decoMeta = deco.getItemMeta();
		decoMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&f"));
		deco.setItemMeta(decoMeta);
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
					gui.setItem(c, deco);
					break;
			}
		}

		for (ItemStack is : Settings.getDefaultKit())
			gui.addItem(is);

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
						ArrayList<ItemStack> itemstacks = new ArrayList<ItemStack>();
						for (int i = 0; i <= 35; i++) {
							if (gui.getItem(i) != null && !gui.getItem(i).getType().equals(Material.AIR)) {
								itemstacks.add(gui.getItem(i));
							}
						}
						Configuration.modifyProperty(ConfigNodes.GAME_KIT, itemstacks);
						p.closeInventory();
						p.sendMessage(ChatColor.translateAlternateColorCodes('&', "&2게임 킷 &a설정을 마쳤습니다."));
						break;
					case "§c초기화":
						Configuration.modifyProperty(ConfigNodes.GAME_KIT, Arrays.asList());
						p.closeInventory();
						p.sendMessage(ChatColor.translateAlternateColorCodes('&', "&2게임 킷 &a설정이 초기화되었습니다."));
						break;
				}
			}
		}
	}

}
