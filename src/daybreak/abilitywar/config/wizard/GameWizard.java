package daybreak.abilitywar.config.wizard;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;

import daybreak.abilitywar.config.AbilityWarSettings;
import daybreak.abilitywar.config.AbilityWarSettings.Settings;
import daybreak.abilitywar.config.enums.ConfigNodes;
import daybreak.abilitywar.utils.Messager;
import daybreak.abilitywar.utils.library.item.MaterialLib;

public class GameWizard extends SettingWizard {

	private final ItemStack deco;
	private final ItemStack food;
	private final ItemStack level;
	private final ItemStack durability;
	private final ItemStack firewall;
	private final ItemStack clearWeather;
	private final ItemStack visualEffect;
	private final ItemStack abilityDraw;

	public GameWizard(Player player, Plugin plugin) {
		super(player, 45, ChatColor.translateAlternateColorCodes('&', "&2&l게임 진행 설정"), plugin);
		this.deco = MaterialLib.WHITE_STAINED_GLASS_PANE.getItem();
		ItemMeta decoMeta = deco.getItemMeta();
		decoMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&f"));
		deco.setItemMeta(decoMeta);
		this.food = new ItemStack(Material.COOKED_BEEF, 1);
		ItemMeta foodMeta = food.getItemMeta();
		foodMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&b배고픔 무제한"));
		food.setItemMeta(foodMeta);
		this.level = MaterialLib.EXPERIENCE_BOTTLE.getItem();
		ItemMeta levelMeta = level.getItemMeta();
		levelMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&b초반 지급 레벨"));
		level.setItemMeta(levelMeta);
		this.durability = new ItemStack(Material.IRON_CHESTPLATE);
		ItemMeta durabilityMeta = durability.getItemMeta();
		durabilityMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&b내구도 무한"));
		durability.setItemMeta(durabilityMeta);
		this.firewall = new ItemStack(Material.BARRIER);
		ItemMeta firewallMeta = firewall.getItemMeta();
		firewallMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&b방화벽"));
		firewall.setItemMeta(firewallMeta);
		this.clearWeather = MaterialLib.SNOWBALL.getItem();
		ItemMeta clearWeatherMeta = clearWeather.getItemMeta();
		clearWeatherMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&b맑은 날씨 고정"));
		clearWeather.setItemMeta(clearWeatherMeta);
		this.visualEffect = new ItemStack(Material.BLAZE_POWDER);
		ItemMeta visualEffectMeta = visualEffect.getItemMeta();
		visualEffectMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&b시각 효과"));
		visualEffect.setItemMeta(visualEffectMeta);
		this.abilityDraw = new ItemStack(Material.DISPENSER);
		ItemMeta abilityDrawMeta = abilityDraw.getItemMeta();
		abilityDrawMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&b능력 추첨"));
		abilityDrawMeta
				.setLore(Messager.asList(ChatColor.translateAlternateColorCodes('&', "&a활성화&f하면 게임을 시작할 때 능력을 추첨합니다."),
						"", ChatColor.translateAlternateColorCodes('&',
								"&7상태 : " + (Settings.getDrawAbility() ? "&a활성화" : "&c비활성화"))));
		abilityDraw.setItemMeta(abilityDrawMeta);
	}

	@Override
	void openGUI(Inventory gui) {
		for (int i = 0; i < 45; i++) {
			switch (i) {
			case 12:
				ItemMeta foodMeta = food.getItemMeta();
				foodMeta.setLore(Messager.asList(ChatColor.translateAlternateColorCodes('&',
						"&7상태 : " + (Settings.getNoHunger() ? "&a활성화" : "&c비활성화"))));
				food.setItemMeta(foodMeta);

				gui.setItem(i, food);
				break;
			case 14:
				ItemMeta levelMeta = level.getItemMeta();
				levelMeta.setLore(Messager.asList(
						ChatColor.translateAlternateColorCodes('&',
								"&7초반 지급 레벨 : &a" + Settings.getStartLevel() + "레벨"),
						" ", ChatColor.translateAlternateColorCodes('&', "&c우클릭         &6» &e+ 1레벨"),
						ChatColor.translateAlternateColorCodes('&', "&cSHIFT + 우클릭 &6» &e+ 5레벨"),
						ChatColor.translateAlternateColorCodes('&', "&c좌클릭         &6» &e- 1레벨"),
						ChatColor.translateAlternateColorCodes('&', "&cSHIFT + 좌클릭 &6» &e- 5레벨"),
						ChatColor.translateAlternateColorCodes('&', "&c휠클릭         &6» &e+ 10000레벨"),
						ChatColor.translateAlternateColorCodes('&', "&cQ              &6» &e- 10000레벨")));
				level.setItemMeta(levelMeta);

				gui.setItem(i, level);
				break;
			case 20:
				ItemMeta durabilityMeta = durability.getItemMeta();
				durabilityMeta.setLore(Messager.asList(ChatColor.translateAlternateColorCodes('&',
						"&7상태 : " + (Settings.getInfiniteDurability() ? "&a활성화" : "&c비활성화"))));
				durability.setItemMeta(durabilityMeta);

				gui.setItem(i, durability);
				break;
			case 22:
				ItemMeta firewallMeta = firewall.getItemMeta();
				firewallMeta.setLore(Messager.asList(
						ChatColor.translateAlternateColorCodes('&',
								"&a활성화&f하면 게임이 시작되고 난 후 참여자 또는 관전자가 아닌 유저는 접속할 수 없습니다."),
						ChatColor.translateAlternateColorCodes('&', "&c관리자 권한&f을 가지고 있을 경우 이를 무시하고 접속할 수 있습니다."), "",
						ChatColor.translateAlternateColorCodes('&',
								"&7상태 : " + (Settings.getFirewall() ? "&a활성화" : "&c비활성화"))));
				firewall.setItemMeta(firewallMeta);

				gui.setItem(i, firewall);
				break;
			case 24:
				ItemMeta clearWeatherMeta = clearWeather.getItemMeta();
				clearWeatherMeta.setLore(Messager.asList(ChatColor.translateAlternateColorCodes('&',
						"&7상태 : " + (Settings.getClearWeather() ? "&a활성화" : "&c비활성화"))));
				clearWeather.setItemMeta(clearWeatherMeta);

				gui.setItem(i, clearWeather);
				break;
			case 30:
				ItemMeta visualEffectMeta = visualEffect.getItemMeta();
				visualEffectMeta.setLore(Messager.asList(
						ChatColor.translateAlternateColorCodes('&', "&a활성화&f하면 일부 능력을 사용할 때 파티클 효과가 보여집니다."), "",
						ChatColor.translateAlternateColorCodes('&',
								"&7상태 : " + (Settings.getVisualEffect() ? "&a활성화" : "&c비활성화"))));
				visualEffect.setItemMeta(visualEffectMeta);

				gui.setItem(i, visualEffect);
				break;
			case 32:
				ItemMeta abilityDrawMeta = abilityDraw.getItemMeta();
				abilityDrawMeta.setLore(
						Messager.asList(ChatColor.translateAlternateColorCodes('&', "&a활성화&f하면 게임을 시작할 때 능력을 추첨합니다."),
								"", ChatColor.translateAlternateColorCodes('&',
										"&7상태 : " + (Settings.getDrawAbility() ? "&a활성화" : "&c비활성화"))));
				abilityDraw.setItemMeta(abilityDrawMeta);

				gui.setItem(i, abilityDraw);
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
		if(currentItem != null) {
			if(currentItem.hasItemMeta() && currentItem.getItemMeta().hasDisplayName()) {
				switch (currentItem.getItemMeta().getDisplayName()) {
				case "§b배고픔 무제한":
					AbilityWarSettings.modifyProperty(ConfigNodes.Game_NoHunger, !Settings.getNoHunger());
					Show();
					break;
				case "§b초반 지급 레벨":
					int startLevel = Settings.getStartLevel();
					switch (e.getClick()) {
					case RIGHT:
						AbilityWarSettings.modifyProperty(ConfigNodes.Game_StartLevel, startLevel + 1);
						Show();
						break;
					case SHIFT_RIGHT:
						AbilityWarSettings.modifyProperty(ConfigNodes.Game_StartLevel, startLevel + 5);
						Show();
						break;
					case LEFT:
						AbilityWarSettings.modifyProperty(ConfigNodes.Game_StartLevel, startLevel >= 1 ? startLevel - 1 : 0);
						Show();
						break;
					case SHIFT_LEFT:
						AbilityWarSettings.modifyProperty(ConfigNodes.Game_StartLevel, startLevel >= 5 ? startLevel - 5 : 0);
						Show();
						break;
					case MIDDLE:
						AbilityWarSettings.modifyProperty(ConfigNodes.Game_StartLevel, startLevel + 10000);
						Show();
						break;
					case DROP:
						AbilityWarSettings.modifyProperty(ConfigNodes.Game_StartLevel, startLevel >= 10000 ? startLevel - 10000 : 0);
						Show();
						break;
					default:
						break;
					}
					break;
				case "§b내구도 무한":
					AbilityWarSettings.modifyProperty(ConfigNodes.Game_InfiniteDurability, !Settings.getInfiniteDurability());
					Show();
					break;
				case "§b방화벽":
					AbilityWarSettings.modifyProperty(ConfigNodes.Game_Firewall, !Settings.getFirewall());
					Show();
					break;
				case "§b맑은 날씨 고정":
					AbilityWarSettings.modifyProperty(ConfigNodes.Game_ClearWeather, !Settings.getClearWeather());
					Show();
					break;
				case "§b시각 효과":
					AbilityWarSettings.modifyProperty(ConfigNodes.Game_VisualEffect, !Settings.getVisualEffect());
					Show();
					break;
				case "§b능력 추첨":
					AbilityWarSettings.modifyProperty(ConfigNodes.Game_DrawAbility, !Settings.getDrawAbility());
					Show();
					break;
				}
			}
		}
	}

}
