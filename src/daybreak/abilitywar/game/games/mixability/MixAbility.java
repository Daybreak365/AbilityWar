package daybreak.abilitywar.game.games.mixability;

import daybreak.abilitywar.AbilityWar;
import daybreak.abilitywar.ability.AbilityBase;
import daybreak.abilitywar.config.Configuration;
import daybreak.abilitywar.game.events.GameCreditEvent;
import daybreak.abilitywar.game.games.mode.AbstractGame;
import daybreak.abilitywar.game.games.mode.GameManifest;
import daybreak.abilitywar.game.games.standard.Game;
import daybreak.abilitywar.game.manager.object.AbilitySelect;
import daybreak.abilitywar.game.manager.object.DeathManager;
import daybreak.abilitywar.game.manager.object.DefaultKitHandler;
import daybreak.abilitywar.game.manager.object.InfiniteDurability;
import daybreak.abilitywar.game.script.ScriptManager;
import daybreak.abilitywar.utils.Messager;
import daybreak.abilitywar.utils.PlayerCollector;
import daybreak.abilitywar.utils.library.SoundLib;
import daybreak.abilitywar.utils.message.KoreanUtil;
import daybreak.abilitywar.utils.thread.AbilityWarThread;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import javax.naming.OperationNotSupportedException;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.List;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

@GameManifest(Name = "믹스 능력자 전쟁 (BETA)", Description = {"§f두가지의 능력을 섞어서 사용하는 게임 모드입니다."})
public class MixAbility extends Game implements DefaultKitHandler {

	private static final Logger logger = Logger.getLogger(MixAbility.class.getName());

	public MixAbility() {
		super(PlayerCollector.EVERY_PLAYER_EXCLUDING_SPECTATORS());
	}

	private boolean invincible = Configuration.Settings.InvincibilitySettings.isEnabled();

	@Override
	protected void progressGame(int seconds) {
		switch (seconds) {
			case 1:
				List<String> lines = Messager.asList(ChatColor.translateAlternateColorCodes('&', "&6==== &e게임 참여자 목록 &6===="));
				int count = 0;
				for (Participant p : getParticipants()) {
					count++;
					lines.add(ChatColor.translateAlternateColorCodes('&', "&a" + count + ". &f" + p.getPlayer().getName()));
				}
				lines.add(ChatColor.translateAlternateColorCodes('&', "&e총 인원수 : " + count + "명"));
				lines.add(ChatColor.translateAlternateColorCodes('&', "&6=========================="));

				for (String line : lines) {
					Bukkit.broadcastMessage(line);
				}

				if (getParticipants().size() < 1) {
					AbilityWarThread.StopGame();
					Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&', "&c최소 참가자 수를 충족하지 못하여 게임을 중지합니다. &8(&71명&8)"));
				} else {
					for (Participant participant : getParticipants()) {
						try {
							participant.setAbility(Mix.class);
						} catch (InvocationTargetException | NoSuchMethodException | InstantiationException | IllegalAccessException ignored) {
							logger.log(Level.SEVERE, participant.getPlayer().getName() + "님에게 " + Mix.class.getName() + " 능력을 부여하는 도중 오류가 발생하였습니다.");
						}
					}
				}
				break;
			case 3:
				lines = Messager.asList(
						ChatColor.translateAlternateColorCodes('&', "&cAbilityWar &f- &6능력자 전쟁"),
						ChatColor.translateAlternateColorCodes('&', "&e버전 &7: &f" + AbilityWar.getPlugin().getDescription().getVersion()),
						ChatColor.translateAlternateColorCodes('&', "&b개발자 &7: &fDaybreak 새벽"),
						ChatColor.translateAlternateColorCodes('&', "&9디스코드 &7: &f새벽&7#5908")
				);

				GameCreditEvent event = new GameCreditEvent();
				Bukkit.getPluginManager().callEvent(event);
				lines.addAll(event.getCreditList());

				for (String line : lines) {
					Bukkit.broadcastMessage(line);
				}
				break;
			case 5:
				if (Configuration.Settings.getDrawAbility()) {
					try {
						startAbilitySelect();
					} catch (OperationNotSupportedException ignored) {
					}
				}
				break;
			case 7:
				Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&', "&e잠시 후 게임이 시작됩니다."));
				break;
			case 9:
				Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&', "&e게임이 &c5&e초 후에 시작됩니다."));
				SoundLib.BLOCK_NOTE_BLOCK_HARP.broadcastSound();
				break;
			case 10:
				Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&', "&e게임이 &c4&e초 후에 시작됩니다."));
				SoundLib.BLOCK_NOTE_BLOCK_HARP.broadcastSound();
				break;
			case 11:
				Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&', "&e게임이 &c3&e초 후에 시작됩니다."));
				SoundLib.BLOCK_NOTE_BLOCK_HARP.broadcastSound();
				break;
			case 12:
				Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&', "&e게임이 &c2&e초 후에 시작됩니다."));
				SoundLib.BLOCK_NOTE_BLOCK_HARP.broadcastSound();
				break;
			case 13:
				Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&', "&e게임이 &c1&e초 후에 시작됩니다."));
				SoundLib.BLOCK_NOTE_BLOCK_HARP.broadcastSound();
				break;
			case 14:
				for (String line : Messager.asList(
						ChatColor.translateAlternateColorCodes('&', "&e■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■"),
						ChatColor.translateAlternateColorCodes('&', "&f             &cMixAbility &f- &6믹스 능력자  "),
						ChatColor.translateAlternateColorCodes('&', "&f                    게임 시작                "),
						ChatColor.translateAlternateColorCodes('&', "&e■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■"))) {
					Bukkit.broadcastMessage(line);
				}

				giveDefaultKit(getParticipants());

				if (Configuration.Settings.getSpawnEnable()) {
					Location spawn = Configuration.Settings.getSpawnLocation();
					for (Participant participant : getParticipants()) {
						participant.getPlayer().teleport(spawn);
					}
				}

				if (Configuration.Settings.getNoHunger()) {
					new TimerBase() {
						@Override
						public void onStart() {
							Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&', "&a배고픔 무제한이 적용됩니다."));
						}

						@Override
						public void onProcess(int count) {
							for (Participant participant : getParticipants()) {
								participant.getPlayer().setFoodLevel(19);
							}
						}
					}.setPeriod(1).startTimer();
				} else {
					Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&', "&4배고픔 무제한&c이 적용되지 않습니다."));
				}

				if (Configuration.Settings.getInfiniteDurability()) {
					attachObserver(new InfiniteDurability());
				} else {
					Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&', "&4내구도 무제한&c이 적용되지 않습니다."));
				}

				if (Configuration.Settings.getClearWeather()) {
					for (World w : Bukkit.getWorlds()) {
						w.setStorm(false);
					}
				}

				if (invincible) {
					getInvincibility().Start(false);
				} else {
					Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&', "&4초반 무적&c이 적용되지 않습니다."));
					setRestricted(false);
				}

				ScriptManager.RunAll(this);

				startGame();
				break;
		}
	}

	@Override
	public void executeCommand(CommandType commandType, Player player, String[] args, Plugin plugin) {
		switch (commandType) {
			case ABI:
				if (args[0].equalsIgnoreCase("@a")) {
					MixAbilityGUI gui = new MixAbilityGUI(player, plugin);
					gui.openAbilityGUI(1);
				} else {
					Player targetPlayer = Bukkit.getPlayerExact(args[0]);
					if (targetPlayer != null) {
						AbstractGame game = AbilityWarThread.getGame();
						if (game.isParticipating(targetPlayer)) {
							AbstractGame.Participant target = game.getParticipant(targetPlayer);
							MixAbilityGUI gui = new MixAbilityGUI(player, target, plugin);
							gui.openAbilityGUI(1);
						} else {
							Messager.sendErrorMessage(player, targetPlayer.getName() + "님은 탈락했거나 게임에 참여하지 않았습니다.");
						}
					} else {
						Messager.sendErrorMessage(player, args[0] + "은(는) 존재하지 않는 플레이어입니다.");
					}
				}
				break;
			case ABLIST:
				player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&2===== &a능력자 목록 &2====="));
				int count = 0;
				for (AbstractGame.Participant participant : AbilityWarThread.getGame().getParticipants()) {
					Mix mix = (Mix) participant.getAbility();
					if (mix.hasAbility()) {
						count++;
						player.sendMessage(ChatColor.translateAlternateColorCodes('&',
								"&e" + count + ". &f" + participant.getPlayer().getName() + " &7: &c" + mix.getFirst().getName() + " &f+ &c" + mix.getSecond().getName()));
					}
				}
				if (count == 0) {
					player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&f능력자가 발견되지 않았습니다."));
				}

				player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&2========================"));

				Bukkit.broadcastMessage(
						ChatColor.translateAlternateColorCodes('&', "&f" + player.getName() + "&a님이 플레이어들의 능력을 확인하였습니다."));
				break;
		}
	}

	@Override
	public AbilitySelect newAbilitySelect() {
		return new AbilitySelect(this, getParticipants(), 1) {

			private List<Class<? extends AbilityBase>> abilities;

			@Override
			protected void drawAbility(Collection<Participant> selectors) {
				abilities = AbilitySelectStrategy.EVERY_ABILITY_EXCLUDING_BLACKLISTED.getAbilities();
				if (getSelectors().size() <= abilities.size()) {
					Random random = new Random();

					for (Participant participant : selectors) {
						Player p = participant.getPlayer();

						Class<? extends AbilityBase> abilityClass = abilities.get(random.nextInt(abilities.size()));
						Class<? extends AbilityBase> secondAbilityClass = abilities.get(random.nextInt(abilities.size()));
						try {
							((Mix) participant.getAbility()).setAbility(abilityClass, secondAbilityClass);

							p.sendMessage(new String[]{
									ChatColor.translateAlternateColorCodes('&', "&a능력이 할당되었습니다. &e/aw check&f로 확인 할 수 있습니다."),
									ChatColor.translateAlternateColorCodes('&', "&e/aw yes &f명령어를 사용하여 능력을 확정합니다."),
									ChatColor.translateAlternateColorCodes('&', "&e/aw no &f명령어를 사용하여 능력을 변경합니다.")
							});
						} catch (IllegalAccessException | NoSuchMethodException | SecurityException |
								InstantiationException | IllegalArgumentException | InvocationTargetException e) {
							Messager.sendConsoleErrorMessage(
									ChatColor.translateAlternateColorCodes('&', "&e" + p.getName() + "&f님에게 능력을 할당하는 도중 오류가 발생하였습니다."),
									ChatColor.translateAlternateColorCodes('&', "&f문제가 발생한 능력: &b" + abilityClass.getName()));
						}
					}
				} else {
					Messager.broadcastErrorMessage("사용 가능한 능력의 수가 참가자의 수보다 적어 게임을 종료합니다.");
					AbilityWarThread.StopGame();
					Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&', "&7게임이 초기화되었습니다."));
				}
			}

			@Override
			protected boolean changeAbility(Participant participant) {
				Player p = participant.getPlayer();

				if (abilities.size() > 0) {
					Random random = new Random();

					if (participant.hasAbility()) {
						Class<? extends AbilityBase> abilityClass = abilities.get(random.nextInt(abilities.size()));
						Class<? extends AbilityBase> secondAbilityClass = abilities.get(random.nextInt(abilities.size()));
						try {
							((Mix) participant.getAbility()).setAbility(abilityClass, secondAbilityClass);
							return true;
						} catch (Exception e) {
							Messager.sendConsoleErrorMessage(ChatColor.translateAlternateColorCodes('&', "&e" + p.getName() + "&f님의 능력을 변경하는 도중 오류가 발생하였습니다."));
							Messager.sendConsoleErrorMessage(ChatColor.translateAlternateColorCodes('&', "&f문제가 발생한 능력: &b" + abilityClass.getName()));
						}
					}
				} else {
					Messager.sendErrorMessage(p, "능력을 변경할 수 없습니다.");
				}

				return false;
			}
		};
	}

	@Override
	public DeathManager newDeathManager() {
		return new DeathManager(this) {
			@Override
			protected String AbilityReveal(Participant victim) {
				Mix mix = (Mix) victim.getAbility();
				if (mix.hasAbility()) {
					String name = mix.getFirst().getName() + " + " + mix.getSecond().getName();
					return ChatColor.translateAlternateColorCodes('&',
							"&f[&c능력&f] &c" + victim.getPlayer().getName() + "&f님의 능력은 &e" + name + "&f" + KoreanUtil.getNeededJosa(name, KoreanUtil.Josa.이었였) + "습니다.");
				} else {
					return ChatColor.translateAlternateColorCodes('&',
							"&f[&c능력&f] &c" + victim.getPlayer().getName() + "&f님은 능력이 없습니다.");
				}
			}
		};
	}

}
