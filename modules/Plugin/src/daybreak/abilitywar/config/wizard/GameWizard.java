package daybreak.abilitywar.config.wizard;

import daybreak.abilitywar.config.Configuration;
import daybreak.abilitywar.config.Configuration.Settings;
import daybreak.abilitywar.config.enums.ConfigNodes;
import daybreak.abilitywar.config.enums.CooldownDecrease;
import daybreak.abilitywar.utils.base.Messager;
import daybreak.abilitywar.utils.library.MaterialX;
import java.util.List;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;

public class GameWizard extends SettingWizard {

	private final ItemStack food, wreck, level, durability, firewall, clearWeather, visualEffect, abilityDraw, maxHealth;

	public GameWizard(Player player, Plugin plugin) {
		super(player, 45, "§2§l게임 진행 설정", plugin);
		this.food = new ItemStack(Material.COOKED_BEEF, 1);
		{
			ItemMeta meta = food.getItemMeta();
			meta.setDisplayName("§b배고픔 무제한");
			food.setItemMeta(meta);
		}
		this.wreck = new ItemStack(Material.NETHER_STAR, 1);
		{
			ItemMeta meta = wreck.getItemMeta();
			meta.setDisplayName("§bWRECK");
			wreck.setItemMeta(meta);
		}
		this.level = MaterialX.EXPERIENCE_BOTTLE.parseItem();
		{
			ItemMeta meta = level.getItemMeta();
			meta.setDisplayName("§b초반 지급 레벨");
			level.setItemMeta(meta);
		}
		this.durability = new ItemStack(Material.IRON_CHESTPLATE);
		{
			ItemMeta meta = durability.getItemMeta();
			meta.setDisplayName("§b내구도 무한");
			durability.setItemMeta(meta);
		}
		this.firewall = new ItemStack(Material.BARRIER);
		{
			ItemMeta meta = firewall.getItemMeta();
			meta.setDisplayName("§b방화벽");
			firewall.setItemMeta(meta);
		}
		this.clearWeather = MaterialX.SNOWBALL.parseItem();
		{
			ItemMeta meta = clearWeather.getItemMeta();
			meta.setDisplayName("§b맑은 날씨 고정");
			clearWeather.setItemMeta(meta);
		}
		this.visualEffect = new ItemStack(Material.BLAZE_POWDER);
		{
			ItemMeta meta = visualEffect.getItemMeta();
			meta.setDisplayName("§b시각 효과");
			visualEffect.setItemMeta(meta);
		}
		this.abilityDraw = new ItemStack(Material.DISPENSER);
		{
			ItemMeta meta = abilityDraw.getItemMeta();
			meta.setDisplayName("§b능력 추첨");
			meta.setLore(Messager.asList("§a활성화§f하면 게임을 시작할 때 능력을 추첨합니다.",
					"", "§7상태 : " + (Settings.getDrawAbility() ? "§a활성화" : "§c비활성화")));
			abilityDraw.setItemMeta(meta);
		}
		this.maxHealth = MaterialX.GOLDEN_APPLE.parseItem();
		{
			ItemMeta meta = maxHealth.getItemMeta();
			meta.setDisplayName("§b기본 최대 체력");
			meta.setLore(Messager.asList("§a활성화§f하면 게임을 시작할 때 모든 플레이어의 최대 체력을 설정합니다.",
					"", "§7상태 : " + (Settings.isDefaultMaxHealthEnabled() ? "§a활성화" : "§c비활성화"),
					"§fSHIFT + 우클릭으로 §a활성화§f/§c비활성화 §f여부를 변경하세요.", "",
					"§7기본 최대 체력 : §f" + Settings.getDefaultMaxHealth(),
					"§c우클릭         §6» §e+ 1",
					"§c좌클릭         §6» §e- 1"));
			maxHealth.setItemMeta(meta);
		}
	}

	@Override
	void openGUI(Inventory gui) {
		for (int i = 0; i < 45; i++) {
			switch (i) {
				case 4: {
					ItemMeta wreckMeta = wreck.getItemMeta();
					List<String> lore = Messager.asList("§7상태 : " + (Settings.isWRECKEnabled() ? "§a활성화" : "§c비활성화"));
					lore.add("");
					CooldownDecrease cooldownDecrease = Settings.getCooldownDecrease();
					for (CooldownDecrease value : CooldownDecrease.values()) {
						lore.add((value.equals(cooldownDecrease) ? ChatColor.GREEN : ChatColor.DARK_GRAY) + value.getDisplayName() + " " + ChatColor.GRAY + value.getLore());
					}
					lore.add("");
					lore.add("§c좌클릭§f으로 활성화/비활성화");
					lore.add("§c우클릭§f으로 쿨타임 감소율 변경");
					wreckMeta.setLore(lore);
					wreck.setItemMeta(wreckMeta);
					gui.setItem(i, wreck);
				}
				break;
				case 12: {
					ItemMeta foodMeta = food.getItemMeta();
					foodMeta.setLore(Messager.asList("§7상태 : " + (Settings.getNoHunger() ? "§a활성화" : "§c비활성화")));
					food.setItemMeta(foodMeta);

					gui.setItem(i, food);
				}
				break;
				case 14: {
					ItemMeta levelMeta = level.getItemMeta();
					levelMeta.setLore(Messager.asList(
							"§7초반 지급 레벨 : §a" + Settings.getStartLevel() + "레벨",
							" ", "§c우클릭         §6» §e+ 1레벨",
							"§cSHIFT + 우클릭 §6» §e+ 5레벨",
							"§c좌클릭         §6» §e- 1레벨",
							"§cSHIFT + 좌클릭 §6» §e- 5레벨",
							"§c휠클릭         §6» §e+ 10000레벨",
							"§cQ              §6» §e- 10000레벨"));
					level.setItemMeta(levelMeta);

					gui.setItem(i, level);
				}
				break;
				case 20: {
					ItemMeta durabilityMeta = durability.getItemMeta();
					durabilityMeta.setLore(Messager.asList("§7상태 : " + (Settings.getInfiniteDurability() ? "§a활성화" : "§c비활성화")));
					durability.setItemMeta(durabilityMeta);

					gui.setItem(i, durability);
				}
				break;
				case 22: {
					ItemMeta firewallMeta = firewall.getItemMeta();
					firewallMeta.setLore(Messager.asList(
							"§a활성화§f하면 게임이 시작되고 난 후 참여자 또는 관전자가 아닌 유저는 접속할 수 없습니다.",
							"§c관리자 권한§f을 가지고 있을 경우 이를 무시하고 접속할 수 있습니다.", "",
							"§7상태 : " + (Settings.getFirewall() ? "§a활성화" : "§c비활성화")));
					firewall.setItemMeta(firewallMeta);

					gui.setItem(i, firewall);
				}
				break;
				case 24: {
					ItemMeta clearWeatherMeta = clearWeather.getItemMeta();
					clearWeatherMeta.setLore(Messager.asList("§7상태 : " + (Settings.getClearWeather() ? "§a활성화" : "§c비활성화")));
					clearWeather.setItemMeta(clearWeatherMeta);

					gui.setItem(i, clearWeather);
				}
				break;
				case 30: {
					ItemMeta visualEffectMeta = visualEffect.getItemMeta();
					visualEffectMeta.setLore(Messager.asList(
							"§a활성화§f하면 일부 능력을 사용할 때 파티클 효과가 보여집니다.", "",
							"§7상태 : " + (Settings.getVisualEffect() ? "§a활성화" : "§c비활성화")));
					visualEffect.setItemMeta(visualEffectMeta);

					gui.setItem(i, visualEffect);
				}
				break;
				case 32: {
					ItemMeta abilityDrawMeta = abilityDraw.getItemMeta();
					abilityDrawMeta.setLore(
							Messager.asList("§a활성화§f하면 게임을 시작할 때 능력을 추첨합니다.",
									"", "§7상태 : " + (Settings.getDrawAbility() ? "§a활성화" : "§c비활성화")));
					abilityDraw.setItemMeta(abilityDrawMeta);

					gui.setItem(i, abilityDraw);
				}
				break;
				case 40: {
					ItemMeta meta = maxHealth.getItemMeta();
					meta.setLore(Messager.asList("§a활성화§f하면 게임을 시작할 때 모든 플레이어의 최대 체력을 설정합니다.",
							"", "§7상태 : " + (Settings.isDefaultMaxHealthEnabled() ? "§a활성화" : "§c비활성화"),
							"§7SHIFT + 우클릭§f으로 §a활성화§f/§c비활성화 §f여부를 변경하세요.", "",
							"§7기본 최대 체력 : §f" + Settings.getDefaultMaxHealth(),
							"§c우클릭         §6» §e+ 1",
							"§c좌클릭         §6» §e- 1"));
					maxHealth.setItemMeta(meta);
					gui.setItem(i, maxHealth);
				}
				break;
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
			if (currentItem.hasItemMeta() && currentItem.getItemMeta().hasDisplayName()) {
				switch (currentItem.getItemMeta().getDisplayName()) {
					case "§b배고픔 무제한":
						Configuration.modifyProperty(ConfigNodes.GAME_NO_HUNGER, !Settings.getNoHunger());
						Show();
						break;
					case "§bWRECK":
						if (e.getClick() == ClickType.LEFT) {
							Configuration.modifyProperty(ConfigNodes.GAME_WRECK_ENABLE, !Settings.isWRECKEnabled());
							Show();
						} else if (e.getClick() == ClickType.RIGHT) {
							Configuration.modifyProperty(ConfigNodes.GAME_WRECK_DECREASE, Settings.getCooldownDecrease().next().name());
							Show();
						}
						break;
					case "§b초반 지급 레벨":
						int startLevel = Settings.getStartLevel();
						switch (e.getClick()) {
							case RIGHT:
								Configuration.modifyProperty(ConfigNodes.GAME_START_LEVEL, startLevel + 1);
								Show();
								break;
							case SHIFT_RIGHT:
								Configuration.modifyProperty(ConfigNodes.GAME_START_LEVEL, startLevel + 5);
								Show();
								break;
							case LEFT:
								Configuration.modifyProperty(ConfigNodes.GAME_START_LEVEL, startLevel >= 1 ? startLevel - 1 : 0);
								Show();
								break;
							case SHIFT_LEFT:
								Configuration.modifyProperty(ConfigNodes.GAME_START_LEVEL, startLevel >= 5 ? startLevel - 5 : 0);
								Show();
								break;
							case MIDDLE:
								Configuration.modifyProperty(ConfigNodes.GAME_START_LEVEL, startLevel + 10000);
								Show();
								break;
							case DROP:
								Configuration.modifyProperty(ConfigNodes.GAME_START_LEVEL, startLevel >= 10000 ? startLevel - 10000 : 0);
								Show();
								break;
							default:
								break;
						}
						break;
					case "§b내구도 무한":
						Configuration.modifyProperty(ConfigNodes.GAME_INFINITE_DURABILITY, !Settings.getInfiniteDurability());
						Show();
						break;
					case "§b방화벽":
						Configuration.modifyProperty(ConfigNodes.GAME_FIREWALL, !Settings.getFirewall());
						Show();
						break;
					case "§b맑은 날씨 고정":
						Configuration.modifyProperty(ConfigNodes.GAME_CLEAR_WEATHER, !Settings.getClearWeather());
						Show();
						break;
					case "§b시각 효과":
						Configuration.modifyProperty(ConfigNodes.GAME_VISUAL_EFFECT, !Settings.getVisualEffect());
						Show();
						break;
					case "§b능력 추첨":
						Configuration.modifyProperty(ConfigNodes.GAME_DRAW_ABILITY, !Settings.getDrawAbility());
						Show();
						break;
					case "§b기본 최대 체력":
						switch (e.getClick()) {
							case RIGHT:
								Configuration.modifyProperty(ConfigNodes.GAME_DEFAULT_MAX_HEALTH_VALUE, Settings.getDefaultMaxHealth() + 1);
								Show();
								break;
							case LEFT:
								Configuration.modifyProperty(ConfigNodes.GAME_DEFAULT_MAX_HEALTH_VALUE, Settings.getDefaultMaxHealth() >= 2 ? Settings.getDefaultMaxHealth() - 1 : 1);
								Show();
								break;
							case SHIFT_LEFT:
								Configuration.modifyProperty(ConfigNodes.GAME_DEFAULT_MAX_HEALTH_ENABLE, !Settings.isDefaultMaxHealthEnabled());
								Show();
								break;
						}
						break;
				}
			}
		}
	}

}
