package daybreak.abilitywar.game.list.oneability;

import daybreak.abilitywar.AbilityWar;
import daybreak.abilitywar.ability.AbilityBase;
import daybreak.abilitywar.config.Configuration.Settings;
import daybreak.abilitywar.game.Game;
import daybreak.abilitywar.game.GameManager;
import daybreak.abilitywar.game.GameManifest;
import daybreak.abilitywar.game.event.GameCreditEvent;
import daybreak.abilitywar.game.manager.AbilityList;
import daybreak.abilitywar.game.manager.object.AbilitySelect;
import daybreak.abilitywar.game.manager.object.DefaultKitHandler;
import daybreak.abilitywar.game.manager.object.InfiniteDurability;
import daybreak.abilitywar.game.script.manager.ScriptManager;
import daybreak.abilitywar.utils.base.Messager;
import daybreak.abilitywar.utils.base.logging.Logger;
import daybreak.abilitywar.utils.base.minecraft.PlayerCollector;
import daybreak.abilitywar.utils.library.SoundLib;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import javax.naming.OperationNotSupportedException;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;

/**
 * 게임 관리 클래스
 *
 * @author Daybreak 새벽
 */
@GameManifest(name = "단일전", description = {
		"§f하나의 능력, 다양한 전략.",
		"§f모두 같은 능력으로 펼치는 능력자 전쟁!"
})
public class OneAbility extends Game implements DefaultKitHandler {

	private static final Logger logger = Logger.getLogger(OneAbility.class);

	public OneAbility() {
		super(PlayerCollector.EVERY_PLAYER_EXCLUDING_SPECTATORS());
		setRestricted(Settings.InvincibilitySettings.isEnabled());
	}

	@Override
	public AbilitySelect newAbilitySelect() {
		return new AbilitySelect(this, getParticipants(), 2) {

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
				abilities = AbilitySelectStrategy.EVERY_ABILITY_EXCLUDING_BLACKLISTED.getAbilities();
				if (abilities.size() > 0) {
					Class<? extends AbilityBase> abilityClass = abilities.get(random.nextInt(abilities.size()));
					for (Participant participant : getParticipants()) {
						try {
							participant.setAbility(abilityClass);
							abilities.remove(abilityClass);

							participant.getPlayer().sendMessage("§a능력이 할당되었습니다. §e/aw check§f로 확인 할 수 있습니다.");
							if (participant.equals(selector)) {
								participant.getPlayer().sendMessage(new String[]{
										"§e/aw yes §f명령어를 사용하여 능력을 확정합니다.",
										"§e/aw no §f명령어를 사용하여 능력을 변경합니다."
								});
							}
						} catch (IllegalAccessException | SecurityException | InstantiationException | IllegalArgumentException | InvocationTargetException e) {
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
								abilities.remove(abilityClass);
							} catch (IllegalAccessException | SecurityException | InstantiationException | IllegalArgumentException | InvocationTargetException e) {
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
				lines.add("§6==========================");

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
						"§9디스코드 §7: §f새벽§7#5908"
				);

				GameCreditEvent event = new GameCreditEvent();
				Bukkit.getPluginManager().callEvent(event);
				lines.addAll(event.getCreditList());

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
				for (String line : Messager.asList(
						"§e■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■",
						"§f                     §c단일전                ",
						"§f                    게임 시작                ",
						"§e■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■")) {
					Bukkit.broadcastMessage(line);
				}

				giveDefaultKit(getParticipants());

				if (Settings.getSpawnEnable()) {
					Location spawn = Settings.getSpawnLocation();
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
					attachObserver(new InfiniteDurability());
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
