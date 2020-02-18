package daybreak.abilitywar.config.wizard;

import daybreak.abilitywar.config.Configuration;
import daybreak.abilitywar.config.Configuration.Settings;
import daybreak.abilitywar.config.enums.ConfigNodes;
import daybreak.abilitywar.utils.Messager;
import daybreak.abilitywar.utils.library.MaterialX;
import daybreak.abilitywar.utils.library.item.ItemLib;
import daybreak.abilitywar.utils.library.item.ItemLib.ItemColor;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;

public class InvincibilityWizard extends SettingWizard {

	public InvincibilityWizard(Player player, Plugin plugin) {
		super(player, 27, ChatColor.translateAlternateColorCodes('&', "&2&l초반 무적 설정"), plugin);
	}

	@Override
	void openGUI(Inventory gui) {
		ItemStack deco = MaterialX.WHITE_STAINED_GLASS_PANE.parseItem();
		ItemMeta decoMeta = deco.getItemMeta();
		decoMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&f"));
		deco.setItemMeta(decoMeta);

		for (int i = 0; i < 27; i++) {
			if (i == 11) {
				boolean InvincibilityEnable = Settings.InvincibilitySettings.isEnabled();
				ItemColor color = InvincibilityEnable ? ItemColor.LIME : ItemColor.RED;
				ItemStack Inv = ItemLib.WOOL.getItemStack(color);
				ItemMeta InvMeta = Inv.getItemMeta();
				InvMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&b초반 무적"));
				InvMeta.setLore(Messager.asList(ChatColor.translateAlternateColorCodes('&',
						"&7상태 : " + (InvincibilityEnable ? "&a활성화" : "&c비활성화"))));
				Inv.setItemMeta(InvMeta);

				gui.setItem(i, Inv);
			} else if (i == 15) {
				ItemStack Inv = MaterialX.CLOCK.parseItem();
				ItemMeta InvMeta = Inv.getItemMeta();
				InvMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&b초반 무적 시간"));
				InvMeta.setLore(Messager.asList(
						ChatColor.translateAlternateColorCodes('&',
								"&7지속 시간 : &a" + Settings.InvincibilitySettings.getDuration() + "분"),
						" ", ChatColor.translateAlternateColorCodes('&', "&c우클릭         &6» &e+ 1분"),
						ChatColor.translateAlternateColorCodes('&', "&cSHIFT + 우클릭 &6» &e+ 5분"),
						ChatColor.translateAlternateColorCodes('&', "&c좌클릭         &6» &e- 1분"),
						ChatColor.translateAlternateColorCodes('&', "&cSHIFT + 좌클릭 &6» &e- 5분")));
				Inv.setItemMeta(InvMeta);

				gui.setItem(i, Inv);
			} else {
				gui.setItem(i, deco);
			}
		}

		player.openInventory(gui);
	}

	@Override
	void onClick(InventoryClickEvent e, Inventory gui) {
		e.setCancelled(true);
		ItemStack currentItem = e.getCurrentItem();
		if (currentItem != null) {
			if (currentItem.hasItemMeta() && currentItem.getItemMeta().hasDisplayName()) {
				switch (currentItem.getItemMeta().getDisplayName()) {
					case "§b초반 무적":
						Configuration.modifyProperty(ConfigNodes.GAME_INVINCIBILITY_ENABLE,
								!Settings.InvincibilitySettings.isEnabled());
						Show();
						break;
					case "§b초반 무적 시간":
						int duration = Settings.InvincibilitySettings.getDuration();
						switch (e.getClick()) {
							case RIGHT:
								Configuration.modifyProperty(ConfigNodes.GAME_INVINCIBILITY_DURATION, duration + 1);
								break;
							case SHIFT_RIGHT:
								Configuration.modifyProperty(ConfigNodes.GAME_INVINCIBILITY_DURATION, duration + 5);
								break;
							case LEFT:
								Configuration.modifyProperty(ConfigNodes.GAME_INVINCIBILITY_DURATION,
										duration >= 2 ? duration - 1 : 1);
								break;
							case SHIFT_LEFT:
								Configuration.modifyProperty(ConfigNodes.GAME_INVINCIBILITY_DURATION,
										duration >= 6 ? duration - 5 : 1);
								break;
							default:
								break;
						}
						Show();
						break;
				}
			}
		}
	}

}
