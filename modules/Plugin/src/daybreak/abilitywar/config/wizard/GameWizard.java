package daybreak.abilitywar.config.wizard;

import daybreak.abilitywar.config.Configuration;
import daybreak.abilitywar.config.Configuration.Settings;
import daybreak.abilitywar.config.Configuration.Settings.AprilSettings;
import daybreak.abilitywar.config.enums.ConfigNodes;
import daybreak.abilitywar.config.enums.CooldownDecrease;
import daybreak.abilitywar.utils.base.Messager;
import daybreak.abilitywar.utils.base.minecraft.item.builder.CustomSkullBuilder;
import daybreak.abilitywar.utils.base.minecraft.item.builder.ItemBuilder;
import daybreak.abilitywar.utils.library.MaterialX;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class GameWizard extends SettingWizard {

	private final ItemStack BETA_ABILITY = new ItemBuilder(MaterialX.REDSTONE).displayName("§b베타 능력 사용 여부").build(),
			AUTO_SKIP = new CustomSkullBuilder("dd993b8c13588919b9f8b42db065d5adfe78af182815b4e6f0f91ba683deac9").displayName("§b자동 스킵").build(),
			WRECK = new ItemBuilder(MaterialX.NETHER_STAR).displayName("§bWRECK").build(),
			SHIELD_COOLDOWN = new ItemBuilder(MaterialX.SHIELD).displayName("§b방패 쿨타임").build(),
			BOW_COOLDOWN = new ItemBuilder(MaterialX.SPECTRAL_ARROW).displayName("§b활 쿨타임").build(),
			ARROW_DAMAGE = new ItemBuilder(MaterialX.BOW).displayName("§b화살 거리 비례 대미지").build(),
			FOOD = new ItemBuilder(MaterialX.COOKED_BEEF).displayName("§b배고픔 무제한").build(),
			LEVEL = new ItemBuilder(MaterialX.EXPERIENCE_BOTTLE).displayName("§b초반 지급 레벨").build(),
			DURABILITY = new ItemBuilder(MaterialX.IRON_CHESTPLATE).displayName("§b내구도 무한").build(),
			ZEROTICK = new ItemBuilder(MaterialX.DIAMOND_SWORD).displayName("§b제로틱").build(),
			FIREWALL = new ItemBuilder(MaterialX.BARRIER).displayName("§b방화벽").build(),
			TEAMGAME = new ItemBuilder(MaterialX.CAKE).displayName("§b팀 게임").build(),
			CLEAR_WEATHER = new ItemBuilder(MaterialX.SNOWBALL).displayName("§b맑은 날씨 고정").build(),
			VISUALEFFECT = new ItemBuilder(MaterialX.BLAZE_POWDER).displayName("§b시각 효과").build(),
			DURATION_TIMER_BEHAVIOR = new ItemBuilder(MaterialX.CLOCK).displayName("§b지속 타이머 작업").build(),
			ABILITY_DRAW = new ItemBuilder(MaterialX.DISPENSER).displayName("§b능력 추첨").build(),
			MAXHEALTH = new ItemBuilder(MaterialX.GOLDEN_APPLE).displayName("§b기본 최대 체력").build(),
			ABILITY_CHANGE_COUNT = new CustomSkullBuilder("c9327a4573977cd378f06d1f832e907f5f66dbe5ea88ff1c3689ad958b7e7").displayName("§b능력 변경 횟수").build();

	public GameWizard(Player player, Plugin plugin) {
		super(player, 45, "§2§l게임 진행 설정", plugin);
	}

	@Override
	void openGUI(Inventory gui) {
		for (int i = 0; i < 45; i++) {
			switch (i) {
				case 2: {
					final ItemMeta meta = AUTO_SKIP.getItemMeta();
					meta.setLore(Arrays.asList("§a활성화 §f하면 능력 추첨을 일정 시간이 지난 후 자동으로 스킵합니다.",
							"", "§7상태 : " + (Settings.isAutoSkipEnabled() ? "§a활성화" : "§c비활성화"),
							"§7SHIFT + 좌클릭§f으로 §a활성화§f/§c비활성화 §f여부를 변경하세요.", "",
							"§f" + Settings.getAutoSkipTime() + "초 §7후에 자동으로 스킵합니다.",
							"§c우클릭         §6» §e+ 1초",
							"§c좌클릭         §6» §e- 1초"));
					AUTO_SKIP.setItemMeta(meta);
					gui.setItem(i, AUTO_SKIP);
				}
				break;
				case 4: {
					ItemMeta wreckMeta = WRECK.getItemMeta();
					List<String> lore = Messager.asList("§7상태 : " + (Settings.isWreckEnabled() ? "§a활성화" : "§c비활성화"));
					lore.add("");
					CooldownDecrease cooldownDecrease = Settings.getCooldownDecrease();
					for (CooldownDecrease value : CooldownDecrease.values()) {
						lore.add((value.equals(cooldownDecrease) ? ChatColor.GREEN : ChatColor.DARK_GRAY) + value.getDisplayName() + " " + ChatColor.GRAY + value.getLore());
					}
					lore.add("");
					lore.add("§c좌클릭§f으로 활성화/비활성화");
					lore.add("§c우클릭§f으로 쿨타임 감소율 변경");
					wreckMeta.setLore(lore);
					WRECK.setItemMeta(wreckMeta);
					gui.setItem(i, WRECK);
				}
				break;
				case 6: {
					final ItemMeta meta = SHIELD_COOLDOWN.getItemMeta();
					meta.setLore(Collections.singletonList("§7상태 : " + (Settings.isShieldCooldownEnabled() ? "§a활성화" : "§c비활성화")));
					SHIELD_COOLDOWN.setItemMeta(meta);
					gui.setItem(i, SHIELD_COOLDOWN);
				}
				break;
				case 12: {
					ItemMeta foodMeta = FOOD.getItemMeta();
					foodMeta.setLore(Messager.asList("§7상태 : " + (Settings.getNoHunger() ? "§a활성화" : "§c비활성화")));
					FOOD.setItemMeta(foodMeta);

					gui.setItem(i, FOOD);
				}
				break;
				case 13: {
					final ItemMeta meta = ARROW_DAMAGE.getItemMeta();
					meta.setLore(Messager.asList(
							"§a활성화 §f하면 게임이 진행되는 동안 화살의 대미지가 거리 비례 대미지로 변경됩니다.",
							"§f가까울수록 대미지가 감소하며, 대미지가 원 대미지보다 증가하지는 않습니다.", "",
							"§7상태 : " + (Settings.isArrowDamageDistanceProportional() ? "§a활성화" : "§c비활성화"))
					);
					ARROW_DAMAGE.setItemMeta(meta);

					gui.setItem(i, ARROW_DAMAGE);
				}
				break;
				case 14: {
					ItemMeta levelMeta = LEVEL.getItemMeta();
					levelMeta.setLore(Messager.asList(
							"§7초반 지급 레벨 : §a" + Settings.getStartLevel() + "레벨",
							" ", "§c우클릭         §6» §e+ 1레벨",
							"§cSHIFT + 우클릭 §6» §e+ 5레벨",
							"§c좌클릭         §6» §e- 1레벨",
							"§cSHIFT + 좌클릭 §6» §e- 5레벨",
							"§c휠클릭         §6» §e+ 10000레벨",
							"§cQ              §6» §e- 10000레벨"));
					LEVEL.setItemMeta(levelMeta);

					gui.setItem(i, LEVEL);
				}
				break;
				case 18: {
					if (AprilSettings.isEnabled()) {
						final ItemMeta meta = BETA_ABILITY.getItemMeta();
						meta.setLore(Arrays.asList("§a활성화 §f하면 능력 추첨시 베타 능력이 추첨됩니다.",
								"", "§7상태 : " + (Settings.isUsingBetaAbility() ? "§a활성화" : "§c비활성화")));
						BETA_ABILITY.setItemMeta(meta);
						gui.setItem(i, BETA_ABILITY);
					}
				}
				break;
				case 20: {
					ItemMeta durabilityMeta = DURABILITY.getItemMeta();
					durabilityMeta.setLore(Messager.asList("§7상태 : " + (Settings.getInfiniteDurability() ? "§a활성화" : "§c비활성화")));
					DURABILITY.setItemMeta(durabilityMeta);

					gui.setItem(i, DURABILITY);
				}
				break;
				case 21: {
					ItemMeta meta = ZEROTICK.getItemMeta();
					meta.setLore(Messager.asList(
							"§a활성화 §f하면 게임 중 공격 딜레이 없이 타격할 수 있습니다.",
							"§7상태 : " + (Settings.isZeroTickEnabled() ? "§a활성화" : "§c비활성화")
					));
					ZEROTICK.setItemMeta(meta);

					gui.setItem(i, ZEROTICK);
				}
				break;
				case 22: {
					ItemMeta firewallMeta = FIREWALL.getItemMeta();
					firewallMeta.setLore(Messager.asList(
							"§a활성화 §f하면 게임이 시작되고 난 후 참여자 또는 관전자가 아닌 유저는 접속할 수 없습니다.",
							"§c관리자 권한§f을 가지고 있을 경우 이를 무시하고 접속할 수 있습니다.", "",
							"§7상태 : " + (Settings.getFirewall() ? "§a활성화" : "§c비활성화")));
					FIREWALL.setItemMeta(firewallMeta);

					gui.setItem(i, FIREWALL);
				}
				break;
				case 23: {
					ItemMeta meta = TEAMGAME.getItemMeta();
					meta.setLore(Messager.asList(
							"§a활성화 §f하면 일부 게임을 팀 게임으로 플레이할 수 있습니다.",
							"", "§7상태 : " + (Settings.isTeamGameEnabled() ? "§a활성화" : "§c비활성화")));
					TEAMGAME.setItemMeta(meta);
					gui.setItem(i, TEAMGAME);
				}
				break;
				case 24: {
					ItemMeta clearWeatherMeta = CLEAR_WEATHER.getItemMeta();
					clearWeatherMeta.setLore(Messager.asList("§7상태 : " + (Settings.getClearWeather() ? "§a활성화" : "§c비활성화")));
					CLEAR_WEATHER.setItemMeta(clearWeatherMeta);

					gui.setItem(i, CLEAR_WEATHER);
				}
				break;
				case 30: {
					ItemMeta visualEffectMeta = VISUALEFFECT.getItemMeta();
					visualEffectMeta.setLore(Messager.asList(
							"§a활성화 §f하면 일부 능력을 사용할 때 파티클 효과가 보여집니다.",
							"§4주의§f: §c비활성화를 추천하지 않습니다.", "",
							"§7상태 : " + (Settings.getVisualEffect() ? "§a활성화" : "§c비활성화")));
					VISUALEFFECT.setItemMeta(visualEffectMeta);

					gui.setItem(i, VISUALEFFECT);
				}
				break;
				case 31: {
					final ItemMeta meta = DURATION_TIMER_BEHAVIOR.getItemMeta();
					final boolean behavior = Settings.getDurationTimerBehavior();
					meta.setLore(Arrays.asList(
							"§f게임 중 능력이 비활성화 되었을 때 지속 시간 타이머를 어떻게",
							"§f처리할지 설정합니다. §6타이머 종료§f를 선택하면 능력 비활성화시",
							"§f타이머가 초기화되며, §6타이머 일시 정지 §e/ §6타이머 재개§f를 선택하면",
							"§f능력이 다시 활성화되었을 때 능력이 이어서 발동됩니다.", "",
							(behavior ? "§7" : "§a") + "타미어 종료",
							(behavior ? "§a" : "§7") + "타이머 일시 정지 / 타이머 재개"
					));
					DURATION_TIMER_BEHAVIOR.setItemMeta(meta);
					gui.setItem(i, DURATION_TIMER_BEHAVIOR);
				}
				break;
				case 32: {
					ItemMeta abilityDrawMeta = ABILITY_DRAW.getItemMeta();
					abilityDrawMeta.setLore(
							Messager.asList("§a활성화 §f하면 게임을 시작할 때 능력을 추첨합니다.",
									"", "§7상태 : " + (Settings.getDrawAbility() ? "§a활성화" : "§c비활성화")));
					ABILITY_DRAW.setItemMeta(abilityDrawMeta);

					gui.setItem(i, ABILITY_DRAW);
				}
				break;
				case 38: {
					final ItemMeta meta = BOW_COOLDOWN.getItemMeta();
					meta.setLore(Arrays.asList(
							"§7상태 : " + (Settings.isBowCooldownEnabled() ? "§a활성화" : "§c비활성화"),
							"§a활성화§f하면 게임 중 10칸 이내에서 활로 대미지를 입힌 경우 대상과의",
							"§f거리에 비례하여 활에 쿨타임이 생깁니다. 활을 이용한 카이팅 플레이를",
							"§f예방할 수 있습니다."
					));
					BOW_COOLDOWN.setItemMeta(meta);
					gui.setItem(i, BOW_COOLDOWN);
				}
				break;
				case 40: {
					ItemMeta meta = MAXHEALTH.getItemMeta();
					meta.setLore(Messager.asList("§a활성화 §f하면 게임을 시작할 때 모든 플레이어의 최대 체력을 설정합니다.",
							"", "§7상태 : " + (Settings.isDefaultMaxHealthEnabled() ? "§a활성화" : "§c비활성화"),
							"§7SHIFT + 좌클릭§f으로 §a활성화§f/§c비활성화 §f여부를 변경하세요.", "",
							"§7기본 최대 체력 : §f" + Settings.getDefaultMaxHealth(),
							"§c우클릭         §6» §e+ 1",
							"§c좌클릭         §6» §e- 1"));
					MAXHEALTH.setItemMeta(meta);
					gui.setItem(i, MAXHEALTH);
				}
				break;
				case 42: {
					final ItemMeta meta = ABILITY_CHANGE_COUNT.getItemMeta();
					meta.setLore(Arrays.asList("§f능력 추첨 시 능력 변경 가능 횟수를 설정합니다.",
							"§f이 설정은 일부 게임 모드에서는 적용되지 않을 수 있으며,",
							"§f해당 게임 모드에서는 모드별 설정을 이용해주세요.",
							"", "§7능력을 §f" + Settings.getAbilityChangeCount() + "회 §7변경할 수 있습니다.", "",
							"§c우클릭         §6» §e+ 1회",
							"§c좌클릭         §6» §e- 1회"));
					ABILITY_CHANGE_COUNT.setItemMeta(meta);
					gui.setItem(i, ABILITY_CHANGE_COUNT);
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
					case "§b베타 능력 사용 여부":
						Configuration.modifyProperty(ConfigNodes.GAME_USE_BETA_ABILITY, !Settings.isUsingBetaAbility());
						show();
						break;
					case "§b자동 스킵":
						switch (e.getClick()) {
							case RIGHT:
								Configuration.modifyProperty(ConfigNodes.GAME_DRAW_AUTOSKIP_TIME, Settings.getAutoSkipTime() + 1);
								show();
								break;
							case LEFT:
								Configuration.modifyProperty(ConfigNodes.GAME_DRAW_AUTOSKIP_TIME, Settings.getAutoSkipTime() >= 2 ? Settings.getAutoSkipTime() - 1 : 1);
								show();
								break;
							case SHIFT_LEFT:
								Configuration.modifyProperty(ConfigNodes.GAME_DRAW_AUTOSKIP_ENABLED, !Settings.isAutoSkipEnabled());
								show();
								break;
						}
						break;
					case "§bWRECK":
						if (e.getClick() == ClickType.LEFT) {
							Configuration.modifyProperty(ConfigNodes.GAME_WRECK_ENABLE, !Settings.isWreckEnabled());
							show();
						} else if (e.getClick() == ClickType.RIGHT) {
							Configuration.modifyProperty(ConfigNodes.GAME_WRECK_DECREASE, Settings.getCooldownDecrease().next().name());
							show();
						}
						break;
					case "§b방패 쿨타임":
						Configuration.modifyProperty(ConfigNodes.GAME_SHIELD_COOLDOWN, !Settings.isShieldCooldownEnabled());
						show();
						break;
					case "§b배고픔 무제한":
						Configuration.modifyProperty(ConfigNodes.GAME_NO_HUNGER, !Settings.getNoHunger());
						show();
						break;
					case "§b화살 거리 비례 대미지":
						Configuration.modifyProperty(ConfigNodes.GAME_ARROW_DISTANCE_PROPORTIONAL_DAMAGE, !Settings.isArrowDamageDistanceProportional());
						show();
						break;
					case "§b초반 지급 레벨":
						int startLevel = Settings.getStartLevel();
						switch (e.getClick()) {
							case RIGHT:
								Configuration.modifyProperty(ConfigNodes.GAME_START_LEVEL, startLevel + 1);
								show();
								break;
							case SHIFT_RIGHT:
								Configuration.modifyProperty(ConfigNodes.GAME_START_LEVEL, startLevel + 5);
								show();
								break;
							case LEFT:
								Configuration.modifyProperty(ConfigNodes.GAME_START_LEVEL, startLevel >= 1 ? startLevel - 1 : 0);
								show();
								break;
							case SHIFT_LEFT:
								Configuration.modifyProperty(ConfigNodes.GAME_START_LEVEL, startLevel >= 5 ? startLevel - 5 : 0);
								show();
								break;
							case MIDDLE:
								Configuration.modifyProperty(ConfigNodes.GAME_START_LEVEL, startLevel + 10000);
								show();
								break;
							case DROP:
								Configuration.modifyProperty(ConfigNodes.GAME_START_LEVEL, startLevel >= 10000 ? startLevel - 10000 : 0);
								show();
								break;
							default:
								break;
						}
						break;
					case "§b내구도 무한":
						Configuration.modifyProperty(ConfigNodes.GAME_INFINITE_DURABILITY, !Settings.getInfiniteDurability());
						show();
						break;
					case "§b제로틱":
						Configuration.modifyProperty(ConfigNodes.GAME_ZEROTICK, !Settings.isZeroTickEnabled());
						show();
						break;
					case "§b방화벽":
						Configuration.modifyProperty(ConfigNodes.GAME_FIREWALL, !Settings.getFirewall());
						show();
						break;
					case "§b팀 게임":
						Configuration.modifyProperty(ConfigNodes.GAME_TEAMGAME, !Settings.isTeamGameEnabled());
						show();
						break;
					case "§b맑은 날씨 고정":
						Configuration.modifyProperty(ConfigNodes.GAME_CLEAR_WEATHER, !Settings.getClearWeather());
						show();
						break;
					case "§b시각 효과":
						Configuration.modifyProperty(ConfigNodes.GAME_VISUAL_EFFECT, !Settings.getVisualEffect());
						show();
						break;
					case "§b지속 타이머 작업":
						Configuration.modifyProperty(ConfigNodes.GAME_DURATION_TIMER_BEHAVIOR, !Settings.getDurationTimerBehavior());
						show();
						break;
					case "§b능력 추첨":
						Configuration.modifyProperty(ConfigNodes.GAME_DRAW_ABILITY, !Settings.getDrawAbility());
						show();
						break;
					case "§b활 쿨타임":
						Configuration.modifyProperty(ConfigNodes.GAME_BOW_COOLDOWN, !Settings.isBowCooldownEnabled());
						show();
						break;
					case "§b기본 최대 체력":
						switch (e.getClick()) {
							case RIGHT:
								Configuration.modifyProperty(ConfigNodes.GAME_DEFAULT_MAX_HEALTH_VALUE, Settings.getDefaultMaxHealth() + 1);
								show();
								break;
							case LEFT:
								Configuration.modifyProperty(ConfigNodes.GAME_DEFAULT_MAX_HEALTH_VALUE, Settings.getDefaultMaxHealth() >= 2 ? Settings.getDefaultMaxHealth() - 1 : 1);
								show();
								break;
							case SHIFT_LEFT:
								Configuration.modifyProperty(ConfigNodes.GAME_DEFAULT_MAX_HEALTH_ENABLE, !Settings.isDefaultMaxHealthEnabled());
								show();
								break;
						}
						break;
					case "§b능력 변경 횟수":
						switch (e.getClick()) {
							case RIGHT:
								Configuration.modifyProperty(ConfigNodes.GAME_ABILITY_CHANGE_COUNT, Settings.getAbilityChangeCount() + 1);
								show();
								break;
							case LEFT:
								Configuration.modifyProperty(ConfigNodes.GAME_ABILITY_CHANGE_COUNT, Math.max(0, Settings.getAbilityChangeCount() - 1));
								show();
								break;
						}
						break;
				}
			}
		}
	}

}
