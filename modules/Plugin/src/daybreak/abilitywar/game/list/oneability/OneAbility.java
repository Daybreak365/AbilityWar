package daybreak.abilitywar.game.list.oneability;

import com.google.common.base.Strings;
import daybreak.abilitywar.AbilityWar;
import daybreak.abilitywar.ability.AbilityBase;
import daybreak.abilitywar.ability.AbilityFactory.AbilityRegistration.Tip;
import daybreak.abilitywar.config.Configuration.Settings;
import daybreak.abilitywar.config.game.GameSettings;
import daybreak.abilitywar.game.Game;
import daybreak.abilitywar.game.GameManager;
import daybreak.abilitywar.game.GameManifest;
import daybreak.abilitywar.game.event.GameCreditEvent;
import daybreak.abilitywar.game.manager.AbilityList;
import daybreak.abilitywar.game.manager.object.AbilitySelect;
import daybreak.abilitywar.game.manager.object.DefaultKitHandler;
import daybreak.abilitywar.game.module.InfiniteDurability;
import daybreak.abilitywar.game.script.manager.ScriptManager;
import daybreak.abilitywar.utils.base.Messager;
import daybreak.abilitywar.utils.base.Seasons;
import daybreak.abilitywar.utils.base.logging.Logger;
import daybreak.abilitywar.utils.base.minecraft.PlayerCollector;
import daybreak.abilitywar.utils.library.SoundLib;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

import javax.naming.OperationNotSupportedException;
import java.util.*;


@GameManifest(name = "단일전", description = {
		"§f하나의 능력, 다양한 전략.",
		"§f모두 같은 능력으로 펼치는 능력자 전쟁!"
})
public class OneAbility extends Game implements DefaultKitHandler {

	private static final GameSettings.Setting<Integer> ABILITY_CHANGE_COUNT = gameSettings.new Setting<Integer>(OneAbility.class, "change-count", 2, "# 능력 변경 횟수") {
		@Override
		public boolean condition(Integer value) {
			return value >= 0;
		}
	};

	private static final Logger logger = Logger.getLogger(OneAbility.class);

	public OneAbility() {
		super(PlayerCollector.EVERY_PLAYER_EXCLUDING_SPECTATORS());
		setRestricted(Settings.InvincibilitySettings.isEnabled());
	}

	@Override
	public AbilitySelect newAbilitySelect() {
		return new AbilitySelect(this, getParticipants(), ABILITY_CHANGE_COUNT.getValue()) {

			private Participant selector;
			private List<Class<? extends AbilityBase>> abilities;

			@Override
			protected Collection<? extends Participant> filterSelectors(Collection<? extends Participant> selectors) {
				Random random = new Random();
				return Collections.singletonList(new ArrayList<>(selectors).get(random.nextInt(selectors.size())));
			}

			@Override
			protected void drawAbility(Collection<? extends Participant> selectors) {
				Random random = new Random();
				this.selector = new ArrayList<>(getSelectors()).get(0);
				Bukkit.broadcastMessage("§e" + selector.getPlayer().getName() + "§f님이 능력을 선택합니다!");
				abilities = AbilityCollector.EVERY_ABILITY_EXCLUDING_BLACKLISTED.collect(OneAbility.this.getClass());
				if (abilities.size() > 0) {
					Class<? extends AbilityBase> abilityClass = abilities.get(random.nextInt(abilities.size()));
					for (Participant participant : getParticipants()) {
						try {
							participant.setAbility(abilityClass);
							final AbilityBase ability = participant.getAbility();
							abilities.remove(abilityClass);

							final Player player = participant.getPlayer();
							player.sendMessage("§a능력이 할당되었습니다. §e/aw check§f로 확인하세요.");
							if (participant.equals(selector) && !hasDecided(participant)) {
								player.sendMessage("§e/aw yes §f명령어로 능력을 확정하거나, §e/aw no §f명령어로 능력을 변경하세요.");
							}
							final Tip tip = ability.getRegistration().getTip();
							if (tip != null) {
								player.sendMessage("§e/aw abtip§f으로 능력 팁을 확인하세요.");
							}
							if (ability.hasSummarize()) {
								player.sendMessage("§e/aw sum§f으로 능력 요약을 확인하세요.");
							}
						} catch (SecurityException | IllegalArgumentException | ReflectiveOperationException e) {
							logger.error(ChatColor.YELLOW + participant.getPlayer().getName() + ChatColor.WHITE + "님에게 능력을 할당하는 도중 오류가 발생하였습니다.");
							logger.error("문제가 발생한 능력: " + ChatColor.AQUA + abilityClass.getName());
						}
					}
				} else {
					Messager.broadcastErrorMessage("사용 가능한 능력이 없습니다.");
					GameManager.stopGame();
				}
			}

			@Override
			protected boolean changeAbility(Participant participant) {
				if (participant.equals(selector)) {
					if (abilities.size() > 0) {
						Random random = new Random();

						Class<? extends AbilityBase> abilityClass = abilities.get(random.nextInt(abilities.size()));
						for (Participant part : getParticipants()) {
							try {
								part.setAbility(abilityClass);
								part.getPlayer().sendMessage("§a능력이 변경되었습니다. §e/aw check§f로 확인하세요.");
								abilities.remove(abilityClass);
							} catch (SecurityException | IllegalArgumentException | ReflectiveOperationException e) {
								logger.error(ChatColor.YELLOW + part.getPlayer().getName() + ChatColor.WHITE + "님에게 능력을 할당하는 도중 오류가 발생하였습니다.");
								logger.error("문제가 발생한 능력: " + ChatColor.AQUA + abilityClass.getName());
							}
						}
						return true;
					} else {
						Messager.sendErrorMessage(participant.getPlayer(), "능력을 변경할 수 없습니다.");
					}
				} else Messager.sendErrorMessage(participant.getPlayer(), "당신은 능력을 변경할 수 없습니다.");
				return false;
			}
		};
	}

	@Override
	protected void progressGame(int seconds) {
		switch (seconds) {
			case 1:
				List<String> lines = Messager.asList("§6==== §e게임 참여자 목록 §6====");
				int count = 0;
				for (Participant p : getParticipants()) {
					count++;
					lines.add("§a" + count + ". §f" + p.getPlayer().getName());
				}
				lines.add("§e총 인원수 : " + count + "명");
				lines.add("§6===========================");

				for (String line : lines) {
					Bukkit.broadcastMessage(line);
				}

				if (getParticipants().size() < 1) {
					stop();
					Bukkit.broadcastMessage("§c최소 참가자 수를 충족하지 못하여 게임을 중지합니다. §8(§71명§8)");
				}
				break;
			case 3:
				lines = Messager.asList(
						"§cOneAbility §f- §6단일전",
						"§e버전 §7: §f" + AbilityWar.getPlugin().getDescription().getVersion(),
						"§b개발자 §7: §fDaybreak 새벽",
						"§9디스코드 §7: §fsaebyeog"
				);

				GameCreditEvent event = new GameCreditEvent(this);
				Bukkit.getPluginManager().callEvent(event);
				lines.addAll(event.getCredits());

				for (String line : lines) {
					Bukkit.broadcastMessage(line);
				}
				break;
			case 5:
				if (Settings.getDrawAbility()) {
					for (String line : Messager.asList(
							"§f플러그인에 총 §b" + AbilityList.nameValues().size() + "개§f의 능력이 등록되어 있습니다.",
							"§7능력을 무작위로 할당합니다...")) {
						Bukkit.broadcastMessage(line);
					}
					try {
						startAbilitySelect();
					} catch (OperationNotSupportedException ignored) {
					}
				}
				break;
			case 6:
				if (Settings.getDrawAbility()) {
					Bukkit.broadcastMessage("§f모든 참가자가 능력을 §b확정§f했습니다.");
				} else {
					Bukkit.broadcastMessage("§f능력자 게임 설정에 따라 §b능력§f을 추첨하지 않습니다.");
				}
				break;
			case 8:
				Bukkit.broadcastMessage("§e잠시 후 게임이 시작됩니다.");
				break;
			case 10:
				Bukkit.broadcastMessage("§e게임이 §c5§e초 후에 시작됩니다.");
				SoundLib.BLOCK_NOTE_BLOCK_HARP.broadcastSound();
				break;
			case 11:
				Bukkit.broadcastMessage("§e게임이 §c4§e초 후에 시작됩니다.");
				SoundLib.BLOCK_NOTE_BLOCK_HARP.broadcastSound();
				break;
			case 12:
				Bukkit.broadcastMessage("§e게임이 §c3§e초 후에 시작됩니다.");
				SoundLib.BLOCK_NOTE_BLOCK_HARP.broadcastSound();
				break;
			case 13:
				Bukkit.broadcastMessage("§e게임이 §c2§e초 후에 시작됩니다.");
				SoundLib.BLOCK_NOTE_BLOCK_HARP.broadcastSound();
				break;
			case 14:
				Bukkit.broadcastMessage("§e게임이 §c1§e초 후에 시작됩니다.");
				SoundLib.BLOCK_NOTE_BLOCK_HARP.broadcastSound();
				break;
			case 15:
				if (Seasons.isChristmas()) {
					final String blocks = Strings.repeat("§c■§2■", 22);
					Bukkit.broadcastMessage(blocks);
					Bukkit.broadcastMessage("§f                     §c단일전                ");
					Bukkit.broadcastMessage("§f                   게임 시작                ");
					Bukkit.broadcastMessage(blocks);
				} else {
					for (String line : Messager.asList(
							"§e■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■",
							"§f                     §c단일전                ",
							"§f                    게임 시작                ",
							"§e■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■")) {
						Bukkit.broadcastMessage(line);
					}
				}

				giveDefaultKit(getParticipants());

				if (Settings.getSpawnEnable()) {
					Location spawn = Settings.getSpawnLocation().toBukkitLocation();
					for (Participant participant : getParticipants()) {
						participant.getPlayer().teleport(spawn);
					}
				}

				if (Settings.getNoHunger()) {
					Bukkit.broadcastMessage("§2배고픔 무제한§a이 적용됩니다.");
				} else {
					Bukkit.broadcastMessage("§4배고픔 무제한§c이 적용되지 않습니다.");
				}

				if (Settings.getInfiniteDurability()) {
					addModule(new InfiniteDurability());
				} else {
					Bukkit.broadcastMessage("§4내구도 무제한§c이 적용되지 않습니다.");
				}

				if (Settings.getClearWeather()) {
					for (World world : Bukkit.getWorlds()) world.setStorm(false);
				}

				if (isRestricted()) {
					getInvincibility().start(false);
				} else {
					Bukkit.broadcastMessage("§4초반 무적§c이 적용되지 않습니다.");
					setRestricted(false);
				}

				ScriptManager.runAll(this);

				startGame();
				break;
		}
	}

}
