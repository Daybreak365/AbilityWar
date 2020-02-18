package daybreak.abilitywar.config.wizard;

import daybreak.abilitywar.config.Configuration;
import daybreak.abilitywar.config.Configuration.Settings.DeathSettings;
import daybreak.abilitywar.config.enums.ConfigNodes;
import daybreak.abilitywar.config.enums.OnDeath;
import daybreak.abilitywar.utils.Messager;
import daybreak.abilitywar.utils.library.MaterialX;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;

import java.util.List;

public class DeathWizard extends SettingWizard {

	private static ItemStack deco;
	private static ItemStack eliminate;
	private static ItemStack abilityReveal;
	private static ItemStack abilityRemoval;
	private static ItemStack itemDrop;

	static {
		deco = MaterialX.WHITE_STAINED_GLASS_PANE.parseItem();
		ItemMeta decoMeta = deco.getItemMeta();
		decoMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&f"));
		deco.setItemMeta(decoMeta);
		eliminate = new ItemStack(Material.DIAMOND_SWORD);
		ItemMeta eliminateMeta = eliminate.getItemMeta();
		eliminateMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&b작업"));
		eliminate.setItemMeta(eliminateMeta);
		abilityReveal = MaterialX.ENDER_EYE.parseItem();
		ItemMeta abilityRevealMeta = abilityReveal.getItemMeta();
		abilityRevealMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&b능력 공개"));
		abilityReveal.setItemMeta(abilityRevealMeta);
		abilityRemoval = MaterialX.BEDROCK.parseItem();
		ItemMeta abilityRemovalMeta = abilityRemoval.getItemMeta();
		abilityRemovalMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&b능력 삭제"));
		abilityRemoval.setItemMeta(abilityRemovalMeta);
		itemDrop = new ItemStack(Material.CHEST);
		ItemMeta itemDropMeta = itemDrop.getItemMeta();
		itemDropMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&b아이템 드롭"));
		itemDrop.setItemMeta(itemDropMeta);
	}

	public DeathWizard(Player player, Plugin plugin) {
		super(player, 27, ChatColor.translateAlternateColorCodes('&', "&2&l플레이어 사망 설정"), plugin);
	}

	@Override
	void openGUI(Inventory gui) {
		for (int i = 0; i < 27; i++) {
			switch (i) {
				case 10:
					ItemMeta eliminateMeta = eliminate.getItemMeta();
					List<String> lore = Messager.asList(
							ChatColor.translateAlternateColorCodes('&', "&f게임 진행 중 플레이어가 사망했을 때 어떤 작업을 수행할지 설정합니다."), "");
					OnDeath operation = DeathSettings.getOperation();
					for (OnDeath od : OnDeath.values()) {
						lore.add(ChatColor.translateAlternateColorCodes('&',
								(operation.equals(od) ? "&a" : "&7") + od.name() + "&f: " + od.getDescription()));
					}
					eliminateMeta.setLore(lore);
					eliminate.setItemMeta(eliminateMeta);
					gui.setItem(i, eliminate);
					break;
				case 12:
					ItemMeta abilityRevealMeta = abilityReveal.getItemMeta();
					abilityRevealMeta.setLore(Messager.asList(
							ChatColor.translateAlternateColorCodes('&', "&a활성화&f하면 게임이 시작되고 난 후 사망할 경우 플레이어의 능력을 공개합니다."),
							"", ChatColor.translateAlternateColorCodes('&',
									"&7상태 : " + (DeathSettings.getAbilityReveal() ? "&a활성화" : "&c비활성화"))));
					abilityReveal.setItemMeta(abilityRevealMeta);
					gui.setItem(i, abilityReveal);
					break;
				case 14:
					ItemMeta abilityRemovalMeta = abilityRemoval.getItemMeta();
					abilityRemovalMeta.setLore(Messager.asList(
							ChatColor.translateAlternateColorCodes('&', "&a활성화&f하면 게임이 시작되고 난 후 사망할 경우 플레이어의 능력이 삭제됩니다."),
							"", ChatColor.translateAlternateColorCodes('&',
									"&7상태 : " + (DeathSettings.getAbilityRemoval() ? "&a활성화" : "&c비활성화"))));
					abilityRemoval.setItemMeta(abilityRemovalMeta);
					gui.setItem(i, abilityRemoval);
					break;
				default:
					gui.setItem(i, deco);
					break;
			}
		}

		player.openInventory(gui);
	}

	@Override
	void onClick(InventoryClickEvent e, Inventory gui) {
		e.setCancelled(true);
		ItemStack currentItem = e.getCurrentItem();
		if (currentItem != null) {
			switch (currentItem.getItemMeta().getDisplayName()) {
				case "§b작업":
					DeathSettings.nextOperation();
					Show();
					break;
				case "§b능력 공개":
					Configuration.modifyProperty(ConfigNodes.GAME_DEATH_ABILITY_REVEAL,
							!DeathSettings.getAbilityReveal());
					Show();
					break;
				case "§b능력 삭제":
					Configuration.modifyProperty(ConfigNodes.GAME_DEATH_ABILITY_REMOVAL,
							!DeathSettings.getAbilityRemoval());
					Show();
					break;
			}
		}
	}

}
