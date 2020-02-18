package daybreak.abilitywar.config.wizard;

import daybreak.abilitywar.config.Configuration;
import daybreak.abilitywar.config.Configuration.Settings;
import daybreak.abilitywar.config.enums.ConfigNodes;
import daybreak.abilitywar.utils.Messager;
import daybreak.abilitywar.utils.library.MaterialX;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;

import java.util.Arrays;

public class SpawnWizard extends SettingWizard {

	public SpawnWizard(Player player, Plugin plugin) {
		super(player, 27, ChatColor.translateAlternateColorCodes('&', "&2&l스폰 설정"), plugin);
	}

	@Override
	void openGUI(Inventory gui) {
		ItemStack deco = MaterialX.WHITE_STAINED_GLASS_PANE.parseItem();
		ItemMeta decoMeta = deco.getItemMeta();
		decoMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&f"));
		deco.setItemMeta(decoMeta);

		for (Integer i = 0; i < 27; i++) {
			if (i.equals(11)) {
				ItemStack spawn = new ItemStack(Material.COOKED_BEEF, 1);
				ItemMeta spawnMeta = spawn.getItemMeta();
				spawnMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&b스폰 이동"));
				spawnMeta.setLore(
						Messager.asList(ChatColor.translateAlternateColorCodes('&', "&f게임이 시작되면 &b스폰&f으로 이동합니다."),
								ChatColor.translateAlternateColorCodes('&',
										"&7상태 : " + (Settings.getSpawnEnable() ? "&a활성화" : "&c비활성화"))));
				spawn.setItemMeta(spawnMeta);

				gui.setItem(i, spawn);
			} else if (i.equals(15)) {
				ItemStack spawn = new ItemStack(Material.COMPASS, 1);
				ItemMeta spawnMeta = spawn.getItemMeta();
				spawnMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&b스폰 설정"));

				Location spawnLocation = Settings.getSpawnLocation();
				double X = spawnLocation.getX();
				double Y = spawnLocation.getY();
				double Z = spawnLocation.getZ();

				spawnMeta.setLore(Arrays.asList(
						ChatColor.translateAlternateColorCodes('&', "&f당신이 현재 서 있는 &a위치&f를 &b스폰&f으로 설정합니다."),
						ChatColor.translateAlternateColorCodes('&', "&6» &f스폰 위치를 변경하려면 클릭하세요."), "",
						ChatColor.translateAlternateColorCodes('&', "&3현재 스폰 위치"),
						ChatColor.translateAlternateColorCodes('&', "&b월드 &7: &f" + spawnLocation.getWorld().getName()),
						ChatColor.translateAlternateColorCodes('&', "&bX &7: &f" + X),
						ChatColor.translateAlternateColorCodes('&', "&bY &7: &f" + Y),
						ChatColor.translateAlternateColorCodes('&', "&bZ &7: &f" + Z),
						ChatColor.translateAlternateColorCodes('&', "&6» &f이 위치로 이동하려면 SHIFT + 좌클릭하세요.")));
				spawn.setItemMeta(spawnMeta);

				gui.setItem(i, spawn);
			} else {
				gui.setItem(i, deco);
			}
		}

		player.openInventory(gui);
	}

	@Override
	void onClick(InventoryClickEvent e, Inventory gui) {
		e.setCancelled(true);
		Player p = (Player) e.getWhoClicked();
		ItemStack currentItem = e.getCurrentItem();
		if (currentItem != null) {
			if (currentItem.hasItemMeta() && currentItem.getItemMeta().hasDisplayName()) {
				switch (currentItem.getItemMeta().getDisplayName()) {
					case "§b스폰 이동":
						Configuration.modifyProperty(ConfigNodes.GAME_SPAWN_ENABLE, !Settings.getSpawnEnable());
						Show();
						break;
					case "§b스폰 설정":
						p.closeInventory();
						if (e.getClick().equals(ClickType.SHIFT_LEFT)) {
							p.teleport(Settings.getSpawnLocation());
							p.sendMessage(ChatColor.translateAlternateColorCodes('&', "&a스폰 &f위치로 이동되었습니다."));
						} else {
							Configuration.modifyProperty(ConfigNodes.GAME_SPAWN_LOCATION, p.getLocation());
							p.sendMessage(ChatColor.translateAlternateColorCodes('&', "&a게임 스폰이 변경되었습니다."));
						}
						break;
				}
			}
		}
	}

}
