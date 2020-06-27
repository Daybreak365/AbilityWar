package daybreak.abilitywar.config.wizard;

import daybreak.abilitywar.config.Configuration;
import daybreak.abilitywar.config.Configuration.Settings.DeathSettings;
import daybreak.abilitywar.config.enums.ConfigNodes;
import daybreak.abilitywar.config.enums.OnDeath;
import daybreak.abilitywar.utils.base.Messager;
import daybreak.abilitywar.utils.library.MaterialX;
import java.util.Arrays;
import java.util.List;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;

public class DeathWizard extends SettingWizard {

	private static final ItemStack eliminate;
	private static final ItemStack abilityReveal;
	private static final ItemStack autoRespawn;

	static {
		eliminate = new ItemStack(Material.DIAMOND_SWORD);
		ItemMeta eliminateMeta = eliminate.getItemMeta();
		eliminateMeta.setDisplayName("§b작업");
		eliminate.setItemMeta(eliminateMeta);

		abilityReveal = MaterialX.ENDER_EYE.parseItem();
		ItemMeta abilityRevealMeta = abilityReveal.getItemMeta();
		abilityRevealMeta.setDisplayName("§b능력 공개");
		abilityReveal.setItemMeta(abilityRevealMeta);

		autoRespawn = MaterialX.DIAMOND_CHESTPLATE.parseItem();
		ItemMeta autoRespawnMeta = autoRespawn.getItemMeta();
		autoRespawnMeta.setDisplayName("§b자동 리스폰");
		autoRespawn.setItemMeta(autoRespawnMeta);
	}

	public DeathWizard(Player player, Plugin plugin) {
		super(player, 27, "§2§l플레이어 사망 설정", plugin);
	}

	@Override
	void openGUI(Inventory gui) {
		for (int i = 0; i < 27; i++) {
			switch (i) {
				case 11: {
					ItemMeta meta = eliminate.getItemMeta();
					List<String> lore = Messager.asList("§f게임 진행 중 플레이어가 사망했을 때 어떤 작업을 수행할지 설정합니다.", "");
					OnDeath operation = DeathSettings.getOperation();
					for (OnDeath onDeath : OnDeath.values()) {
						lore.add("");
						lore.add((operation.equals(onDeath) ? "§a" : "§7") + onDeath.name());
						lore.addAll(Arrays.asList(onDeath.getDescription()));
					}
					meta.setLore(lore);
					eliminate.setItemMeta(meta);
					gui.setItem(i, eliminate);
					break;
				}
				case 13: {
					ItemMeta meta = abilityReveal.getItemMeta();
					meta.setLore(Messager.asList(
							"§a활성화§f하면 게임이 시작되고 난 후 사망할 경우 플레이어의 능력을 공개합니다.",
							"",
							"§7상태 : " + (DeathSettings.getAbilityReveal() ? "§a활성화" : "§c비활성화")));
					abilityReveal.setItemMeta(meta);
					gui.setItem(i, abilityReveal);
					break;
				}
				case 15: {
					ItemMeta meta = autoRespawn.getItemMeta();
					meta.setLore(Messager.asList(
							"§a활성화§f하면 게임이 시작되고 난 후 사망할 경우 플레이어를 자동으로 리스폰시킵니다.",
							"",
							"§7상태 : " + (DeathSettings.getAutoRespawn() ? "§a활성화" : "§c비활성화")));
					autoRespawn.setItemMeta(meta);
					gui.setItem(i, autoRespawn);
					break;
				}
				default:
					gui.setItem(i, DECO);
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
					Configuration.modifyProperty(ConfigNodes.GAME_DEATH_ABILITY_REVEAL, !DeathSettings.getAbilityReveal());
					Show();
					break;
				case "§b자동 리스폰":
					Configuration.modifyProperty(ConfigNodes.GAME_DEATH_AUTO_RESPAWN, !DeathSettings.getAutoRespawn());
					Show();
					break;
			}
		}
	}

}
