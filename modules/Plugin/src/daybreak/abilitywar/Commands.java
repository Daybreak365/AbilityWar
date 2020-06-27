package daybreak.abilitywar;

import daybreak.abilitywar.Command.Condition;
import daybreak.abilitywar.ability.AbilityBase.CooldownTimer;
import daybreak.abilitywar.ability.AbilityBase.DurationTimer;
import daybreak.abilitywar.ability.AbilityFactory;
import daybreak.abilitywar.config.Configuration;
import daybreak.abilitywar.config.Configuration.Settings;
import daybreak.abilitywar.config.ability.wizard.AbilitySettingWizard;
import daybreak.abilitywar.config.enums.ConfigNodes;
import daybreak.abilitywar.config.wizard.DeathWizard;
import daybreak.abilitywar.config.wizard.GameWizard;
import daybreak.abilitywar.config.wizard.InvincibilityWizard;
import daybreak.abilitywar.config.wizard.KitWizard;
import daybreak.abilitywar.config.wizard.SpawnWizard;
import daybreak.abilitywar.game.AbstractGame;
import daybreak.abilitywar.game.AbstractGame.Participant;
import daybreak.abilitywar.game.GameManager;
import daybreak.abilitywar.game.interfaces.TeamGame;
import daybreak.abilitywar.game.interfaces.TeamGame.Team;
import daybreak.abilitywar.game.manager.GameFactory;
import daybreak.abilitywar.game.manager.GameFactory.GameRegistration;
import daybreak.abilitywar.game.manager.SpectatorManager;
import daybreak.abilitywar.game.manager.gui.AbilityListGUI;
import daybreak.abilitywar.game.manager.gui.BlackListGUI;
import daybreak.abilitywar.game.manager.gui.GameModeGUI;
import daybreak.abilitywar.game.manager.gui.InstallGUI;
import daybreak.abilitywar.game.manager.gui.SpecialThanksGUI;
import daybreak.abilitywar.game.manager.gui.SpectatorGUI;
import daybreak.abilitywar.game.manager.gui.TeamPresetGUI;
import daybreak.abilitywar.game.manager.object.AbilitySelect;
import daybreak.abilitywar.game.manager.object.CommandHandler;
import daybreak.abilitywar.game.manager.object.DefaultKitHandler;
import daybreak.abilitywar.game.manager.object.Invincibility;
import daybreak.abilitywar.game.script.AbstractScript;
import daybreak.abilitywar.game.script.ScriptWizard;
import daybreak.abilitywar.game.script.manager.ScriptManager;
import daybreak.abilitywar.utils.base.Formatter;
import daybreak.abilitywar.utils.base.Messager;
import daybreak.abilitywar.utils.base.TimeUtil;
import daybreak.abilitywar.utils.base.language.korean.KoreanUtil;
import daybreak.abilitywar.utils.base.language.korean.KoreanUtil.Josa;
import daybreak.abilitywar.utils.base.logging.Logger;
import daybreak.abilitywar.utils.base.math.NumberUtil;
import daybreak.abilitywar.utils.library.SoundLib;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

public class Commands implements CommandExecutor, TabCompleter {

	private static final Logger logger = Logger.getLogger(Commands.class);

	private final Command mainCommand;

	Commands(AbilityWar plugin) {
		this.mainCommand = new Command() {
			@Override
			protected boolean onCommand(CommandSender sender, String command, String[] args) {
				if (args.length == 0) {
					sender.sendMessage(Formatter.formatTitle(ChatColor.GOLD, ChatColor.YELLOW, "능력자 전쟁"));
					sender.sendMessage("§e버전 §7: §f" + plugin.getDescription().getVersion());
					sender.sendMessage("§b개발자 §7: §fDaybreak 새벽");
					sender.sendMessage("§9디스코드 §7: §f새벽§7#5908");
					sender.sendMessage("§3§o/" + command + " help §7§o로 명령어 도움말을 확인하세요.");
					return true;
				}
				return false;
			}
		};
		mainCommand.addSubCommand("help", new Command() {
			@Override
			protected boolean onCommand(CommandSender sender, String command, String[] args) {
				if (args.length > 0) {
					if (NumberUtil.isInt(args[0])) {
						sendCommandHelp(sender, command, Integer.parseInt(args[0]));
					} else {
						Messager.sendErrorMessage(sender, "존재하지 않는 페이지입니다.");
					}
				} else {
					sendCommandHelp(sender, command, 1);
				}
				return true;
			}

			private void sendCommandHelp(CommandSender sender, String command, int page) {
				final int allPage = 2;
				switch (page) {
					case 1:
						sender.sendMessage(new String[]{Formatter.formatTitle(ChatColor.GOLD, ChatColor.YELLOW, "능력자 전쟁"),
								"§b/" + command + " help <페이지> §7로 더 많은 명령어를 확인하세요! ( §b" + page + " 페이지 §7/ §b" + allPage + " 페이지 §7)",
								Formatter.formatCommand(command, "start", "능력자 전쟁을 시작시킵니다.", true),
								Formatter.formatCommand(command, "stop", "능력자 전쟁을 중지시킵니다.", true),
								Formatter.formatCommand(command, "check", "자신의 능력을 확인합니다.", false),
								Formatter.formatCommand(command, "yes", "자신의 능력을 확정합니다.", false),
								Formatter.formatCommand(command, "no", "자신의 능력을 변경합니다.", false),
								Formatter.formatCommand(command, "abilities", "능력자 전쟁 능력 목록을 확인합니다.", false),
								Formatter.formatCommand(command, "team", "팀 게임의 명령어 목록을 확인합니다.", false),
								Formatter.formatCommand(command, "specialthanks", "능력자 전쟁 플러그인에 기여한 사람들을 확인합니다.", false)});
						break;
					case 2:
						sender.sendMessage(new String[]{Formatter.formatTitle(ChatColor.GOLD, ChatColor.YELLOW, "능력자 전쟁"),
								"§b/" + command + " help <페이지> §7로 더 많은 명령어를 확인하세요! ( §b" + page + " 페이지 §7/ §b" + allPage + " 페이지 §7)",
								Formatter.formatCommand(command, "skip", "모든 유저의 능력을 강제로 확정합니다.", true),
								Formatter.formatCommand(command, "anew", "모든 유저의 능력을 새로 뽑습니다.", true),
								Formatter.formatCommand(command, "config", "능력자 전쟁 콘피그 명령어를 확인합니다.", true),
								Formatter.formatCommand(command, "util", "능력자 전쟁 유틸 명령어를 확인합니다.", true),
								Formatter.formatCommand(command, "script", "능력자 전쟁 스크립트 편집을 시작합니다.", true),
								Formatter.formatCommand(command, "gamemode", "게임 모드 설정 GUI를 엽니다.", true),
								Formatter.formatCommand(command, "gamemode [모드]", "게임 모드를 [모드]로 변경합니다.", true),
								Formatter.formatCommand(command, "install", "새로운 버전의 다운로드를 시도합니다.", true)});
						break;
					default:
						Messager.sendErrorMessage(sender, "존재하지 않는 페이지입니다.");
						break;
				}
			}

		});
		mainCommand.addSubCommand("start", new Command(Condition.OP) {
			@Override
			protected boolean onCommand(CommandSender sender, String command, String[] args) {
				if (!GameManager.isGameRunning()) {
					try {
						if (GameManager.startGame(args)) {
							Bukkit.broadcastMessage("§f관리자 §e" + sender.getName() + "§f님이 게임을 시작시켰습니다.");
						}
					} catch (IllegalArgumentException e) {
						Messager.sendErrorMessage(sender, e.getMessage());
					}
				} else {
					Messager.sendErrorMessage(sender, "게임이 이미 진행되고 있습니다.");
				}
				return true;
			}
		});
		mainCommand.addSubCommand("stop", new Command(Condition.OP) {
			@Override
			protected boolean onCommand(CommandSender sender, String command, String[] args) {
				if (GameManager.isGameRunning()) {
					Bukkit.broadcastMessage("§f관리자 §e" + sender.getName() + "§f님이 게임을 중지시켰습니다.");
					GameManager.stopGame();
				} else {
					Messager.sendErrorMessage(sender, "게임이 진행되고 있지 않습니다.");
				}
				return true;
			}
		});
		mainCommand.addSubCommand("check", new Command(Condition.PLAYER) {
			@Override
			protected boolean onCommand(CommandSender sender, String command, String[] args) {
				Player player = (Player) sender;
				if (GameManager.isGameRunning()) {
					AbstractGame game = GameManager.getGame();
					if (game.isParticipating(player)) {
						Participant participant = game.getParticipant(player);
						if (participant.hasAbility()) {
							for (String line : Formatter.formatAbilityInfo(participant.getAbility())) {
								player.sendMessage(line);
							}
						} else {
							Messager.sendErrorMessage(sender, "능력이 할당되지 않았습니다.");
						}
					} else {
						Messager.sendErrorMessage(sender, "게임에 참가하고 있지 않습니다.");
					}
				} else {
					Messager.sendErrorMessage(sender, "게임이 진행되고 있지 않습니다.");
				}
				return true;
			}
		});
		mainCommand.addSubCommand("yes", new Command(Condition.PLAYER) {
			@Override
			protected boolean onCommand(CommandSender sender, String command, String[] args) {
				Player player = (Player) sender;
				if (GameManager.isGameOf(AbilitySelect.Handler.class)) {
					AbstractGame game = GameManager.getGame();
					AbilitySelect abilitySelect = ((AbilitySelect.Handler) game).getAbilitySelect();
					if (abilitySelect != null) {
						if (game.isParticipating(player)) {
							Participant participant = game.getParticipant(player);
							if (abilitySelect.isStarted() && !abilitySelect.isEnded()) {
								if (abilitySelect.isSelector(participant)) {
									if (!abilitySelect.hasDecided(participant)) {
										abilitySelect.decideAbility(participant);
									} else player.sendMessage("§c이미 능력 선택을 마치셨습니다.");
								} else Messager.sendErrorMessage(sender, "당신은 능력을 선택하고 있지 않습니다.");
							} else Messager.sendErrorMessage(sender, "능력을 선택하고 있지 않습니다.");
						} else Messager.sendErrorMessage(sender, "게임에 참가하고 있지 않습니다.");
					} else Messager.sendErrorMessage(sender, "능력 선택을 할 수 없는 게임입니다.");
				} else Messager.sendErrorMessage(sender, "게임이 진행되고 있지 않거나 능력 선택을 할 수 없는 게임입니다.");
				return true;
			}
		});
		mainCommand.addSubCommand("no", new Command(Condition.PLAYER) {
			@Override
			protected boolean onCommand(CommandSender sender, String command, String[] args) {
				Player player = (Player) sender;
				if (GameManager.isGameOf(AbilitySelect.Handler.class)) {
					AbstractGame game = GameManager.getGame();
					AbilitySelect abilitySelect = ((AbilitySelect.Handler) game).getAbilitySelect();
					if (abilitySelect != null) {
						if (game.isParticipating(player)) {
							Participant participant = game.getParticipant(player);
							if (abilitySelect.isStarted() && !abilitySelect.isEnded()) {
								if (abilitySelect.isSelector(participant)) {
									if (!abilitySelect.hasDecided(participant)) {
										abilitySelect.alterAbility(participant);
									} else player.sendMessage("§c이미 능력 선택을 마치셨습니다.");
								} else Messager.sendErrorMessage(sender, "당신은 능력을 선택하고 있지 않습니다.");
							} else Messager.sendErrorMessage(sender, "능력을 선택하고 있지 않습니다.");
						} else Messager.sendErrorMessage(sender, "게임에 참가하고 있지 않습니다.");
					} else Messager.sendErrorMessage(sender, "능력 선택을 할 수 없는 게임입니다.");
				} else Messager.sendErrorMessage(sender, "게임이 진행되고 있지 않거나 능력 선택을 할 수 없는 게임입니다.");
				return true;
			}
		});
		mainCommand.addSubCommand("abilities", new Command(Condition.PLAYER) {
			@Override
			protected boolean onCommand(CommandSender sender, String command, String[] args) {
				new AbilityListGUI((Player) sender, AbilityWar.getPlugin()).openGUI(1);
				return true;
			}
		});
		mainCommand.addSubCommand("skip", new Command(Condition.OP) {
			@Override
			protected boolean onCommand(CommandSender sender, String command, String[] args) {
				if (GameManager.isGameOf(AbilitySelect.Handler.class)) {
					AbilitySelect abilitySelect = ((AbilitySelect.Handler) GameManager.getGame()).getAbilitySelect();
					if (abilitySelect != null) {
						if (abilitySelect.isStarted() && !abilitySelect.isEnded()) {
							abilitySelect.Skip(sender.getName());
						} else {
							Messager.sendErrorMessage(sender, "능력을 선택하고 있지 않습니다.");
						}
					} else Messager.sendErrorMessage(sender, "능력 선택을 할 수 없는 게임입니다.");
				} else Messager.sendErrorMessage(sender, "게임이 진행되고 있지 않거나 능력 선택을 할 수 없는 게임입니다.");
				return true;
			}
		});
		mainCommand.addSubCommand("anew", new Command(Condition.OP) {
			@Override
			protected boolean onCommand(CommandSender sender, String command, String[] args) {
				if (GameManager.isGameOf(AbilitySelect.Handler.class)) {
					AbilitySelect abilitySelect = ((AbilitySelect.Handler) GameManager.getGame()).getAbilitySelect();
					if (abilitySelect != null) {
						abilitySelect.reset();
						Bukkit.broadcastMessage("§f관리자 §e" + sender.getName() + "§f님이 모든 참가자의 능력을 재추첨했습니다.");
					} else Messager.sendErrorMessage(sender, "능력 선택을 할 수 없는 게임입니다.");
				} else Messager.sendErrorMessage(sender, "게임이 진행되고 있지 않거나 능력 선택을 할 수 없는 게임입니다.");
				return true;
			}
		});
		final Command configCommand = new Command(Condition.OP, Condition.PLAYER) {
			{
				addSubCommand("kit", new Command() {
					@Override
					protected boolean onCommand(CommandSender sender, String command, String[] args) {
						Player player = (Player) sender;
						if (args.length > 0) {
							String name = String.join(" ", args);
							if (AbilityFactory.isRegistered(name)) {
								new KitWizard(player, plugin, AbilityFactory.getRegistration(AbilityFactory.getByName(name))).Show();
							} else {
								player.sendMessage(ChatColor.RED + name + KoreanUtil.getJosa(name, Josa.은는) + " 존재하지 않는 능력입니다.");
							}
						} else {
							new KitWizard(player, plugin).Show();
						}
						return true;
					}
				});
				addSubCommand("spawn", new Command() {
					@Override
					protected boolean onCommand(CommandSender sender, String command, String[] args) {
						new SpawnWizard((Player) sender, plugin).Show();
						return true;
					}
				});
				addSubCommand("inv", new Command() {
					@Override
					protected boolean onCommand(CommandSender sender, String command, String[] args) {
						new InvincibilityWizard((Player) sender, plugin).Show();
						return true;
					}
				});
				addSubCommand("game", new Command() {
					@Override
					protected boolean onCommand(CommandSender sender, String command, String[] args) {
						new GameWizard((Player) sender, plugin).Show();
						return true;
					}
				});
				addSubCommand("death", new Command() {
					@Override
					protected boolean onCommand(CommandSender sender, String command, String[] args) {
						new DeathWizard((Player) sender, plugin).Show();
						return true;
					}
				});
				addSubCommand("ability", new Command() {
					@Override
					protected boolean onCommand(CommandSender sender, String command, String[] args) {
						new AbilitySettingWizard((Player) sender, plugin).openGUI(1);
						return true;
					}
				});
				addSubCommand("teampreset", new Command() {
					@Override
					protected boolean onCommand(CommandSender sender, String command, String[] args) {
						new TeamPresetGUI((Player) sender, plugin).openGUI(1);
						return true;
					}
				});
				addSubCommand("blacklist", new Command() {
					@Override
					protected boolean onCommand(CommandSender sender, String command, String[] args) {
						if (args.length == 0) {
							if (sender instanceof Player) {
								new BlackListGUI((Player) sender, plugin).openGUI(1);
							} else
								Messager.sendErrorMessage(sender, "사용법 §7: §f/" + command + " config blacklist [대상]");
						} else {
							String name = String.join(" ", args);
							if (AbilityFactory.isRegistered(name)) {
								if (Settings.toggleBlacklist(name)) sender.sendMessage("§c블랙리스트에 추가됨§7: §f" + name);
								else sender.sendMessage("§a블랙리스트에서 제거됨§7: §f" + name);
							} else
								Messager.sendErrorMessage(sender, name + KoreanUtil.getJosa(name, Josa.은는) + " 존재하지 않는 능력입니다.");
						}
						return true;
					}
				});
			}

			@Override
			protected boolean onCommand(CommandSender sender, String command, String[] args) {
				Player player = (Player) sender;
				if (args.length > 0) {
					if (NumberUtil.isInt(args[0])) {
						sendConfigCommandHelp(player, command, Integer.parseInt(args[0]));
					} else
						Messager.sendErrorMessage(player, "'§4" + args[0] + "§c'" + KoreanUtil.getJosa(args[0], Josa.은는) + " 존재하지 않는 콘피그입니다.");
				} else sendConfigCommandHelp(player, command, 1);
				return true;
			}

			private void sendConfigCommandHelp(CommandSender sender, String label, int page) {
				final int allPage = 2;
				switch (page) {
					case 1:
						sender.sendMessage(new String[]{Formatter.formatTitle(ChatColor.GOLD, ChatColor.YELLOW, "능력자 전쟁 콘피그"),
								"§b/" + label + " config <페이지> §7로 더 많은 명령어를 확인하세요! ( §b" + page + " 페이지 §7/ §b" + allPage + " 페이지 §7)",
								Formatter.formatCommand(label + " config", "kit", "능력자 전쟁 기본템을 설정합니다.", true),
								Formatter.formatCommand(label + " config", "kit <능력 이름>", "능력별 기본템을 설정합니다.", true),
								Formatter.formatCommand(label + " config", "spawn", "능력자 전쟁 스폰을 설정합니다.", true),
								Formatter.formatCommand(label + " config", "inv", "초반 무적을 설정합니다.", true),
								Formatter.formatCommand(label + " config", "game", "게임의 전반적인 부분들을 설정합니다.", true),
								Formatter.formatCommand(label + " config", "death", "플레이어 사망에 관련된 콘피그를 설정합니다.", true)});
						break;
					case 2:
						sender.sendMessage(new String[]{Formatter.formatTitle(ChatColor.GOLD, ChatColor.YELLOW, "능력자 전쟁 콘피그"),
								"§b/" + label + " config <페이지> §7로 더 많은 명령어를 확인하세요! ( §b" + page + " 페이지 §7/ §b" + allPage + " 페이지 §7)",
								Formatter.formatCommand(label + " config", "ability", "능력별 설정을 변경합니다.", true),
								Formatter.formatCommand(label + " config", "teampreset", "팀 프리셋 설정 GUI를 엽니다.", true),
								Formatter.formatCommand(label + " config", "blacklist", "능력 블랙리스트 설정 GUI를 엽니다.", true),
								Formatter.formatCommand(label + " config", "blacklist [능력]", "[능력] 능력의 블랙 상태를 토글합니다.", true)});
						break;
					default:
						Messager.sendErrorMessage(sender, "존재하지 않는 페이지입니다.");
						break;
				}
			}
		};
		mainCommand.addSubCommand("config", configCommand);
		final Command utilCommand = new Command(Condition.OP) {
			{
				addSubCommand("abi", new Command() {
					@Override
					protected boolean onCommand(CommandSender sender, String command, String[] args) {
						if (GameManager.isGameRunning()) {
							GameManager.getGame().executeCommand(CommandHandler.CommandType.ABI, sender, command, args, plugin);
						} else Messager.sendErrorMessage(sender, "게임이 진행되고 있지 않습니다.");
						return true;
					}
				});
				addSubCommand("inv", new Command() {
					@Override
					protected boolean onCommand(CommandSender sender, String command, String[] args) {
						if (GameManager.isGameOf(Invincibility.Handler.class)) {
							if (GameManager.getGame().isGameStarted()) {
								Invincibility invincibility = ((Invincibility.Handler) GameManager.getGame()).getInvincibility();
								if (invincibility.isEnabled()) {
									invincibility.stop();
									Bukkit.broadcastMessage("§f" + sender.getName() + "§a님이 무적 상태를 §f비활성화§a하셨습니다.");
								} else {
									if (args.length >= 1) {
										if (NumberUtil.isInt(args[0])) {
											int duration = Integer.parseInt(args[0]);
											invincibility.start(duration);
											Bukkit.broadcastMessage("§f" + sender.getName() + "§a님이 무적 상태를 §f" + TimeUtil.parseTimeAsString(duration) + "§a간 §f활성화§a하셨습니다.");
										} else {
											Messager.sendErrorMessage(sender, "시간은 자연수로 입력되어야 합니다.");
										}
									} else {
										invincibility.start(true);
										Bukkit.broadcastMessage("§f" + sender.getName() + "§a님이 무적 상태를 §f활성화§a하셨습니다.");
									}
								}
							} else Messager.sendErrorMessage(sender, "§c게임이 아직 시작되지 않았습니다.");
						} else Messager.sendErrorMessage(sender, "§c게임이 진행되고 있지 않거나 무적 기능을 사용할 수 있는 게임이 아닙니다.");
						return true;
					}
				});
				addSubCommand("spec", new Command() {
					@Override
					protected boolean onCommand(CommandSender sender, String command, String[] args) {
						if (args.length == 0) {
							if (sender instanceof Player) {
								SpectatorGUI gui = new SpectatorGUI((Player) sender, plugin);
								gui.openGUI(1);
							} else Messager.sendErrorMessage(sender, "사용법 §7: §f/" + command + " util spec [대상]");
						} else {
							for (String arg : args) {
								if (SpectatorManager.isSpectator(arg)) {
									SpectatorManager.removeSpectator(arg);
									sender.sendMessage("§f" + arg + "§a님은 지금부터 게임에 참여할 수 있습니다.");
								} else {
									Player target = Bukkit.getPlayerExact(arg);
									if (target != null) {
										SpectatorManager.addSpectator(target.getName());
										sender.sendMessage("§f" + target.getName() + "§c님은 더 이상 게임에 참여하지 않습니다.");
									} else
										Messager.sendErrorMessage(sender, arg + KoreanUtil.getJosa(arg, Josa.은는) + " 존재하지 않는 플레이어입니다.");
								}
							}
						}
						return true;
					}
				});
				addSubCommand("ablist", new Command() {
					@Override
					protected boolean onCommand(CommandSender sender, String command, String[] args) {
						if (GameManager.isGameRunning()) {
							GameManager.getGame().executeCommand(CommandHandler.CommandType.ABLIST, sender, command, args, plugin);
						} else {
							Messager.sendErrorMessage(sender, "§c게임이 진행되고 있지 않습니다.");
						}
						return true;
					}
				});
				addSubCommand("blacklist", new Command() {
					@Override
					protected boolean onCommand(CommandSender sender, String command, String[] args) {
						Messager.sendErrorMessage(sender, "'§4/" + command + " config blacklist§c' §c명령어를 사용해주세요.");
						return true;
					}
				});
				addSubCommand("resetcool", new Command() {
					@Override
					protected boolean onCommand(CommandSender sender, String command, String[] args) {
						if (GameManager.isGameRunning()) {
							GameManager.getGame().stopTimers(CooldownTimer.class);
							Bukkit.broadcastMessage("§f" + sender.getName() + "§a님이 능력 쿨타임을 모두 초기화하였습니다.");
						} else Messager.sendErrorMessage(sender, "게임이 진행되고 있지 않습니다.");
						return true;
					}
				});
				addSubCommand("resetduration", new Command() {
					@Override
					protected boolean onCommand(CommandSender sender, String command, String[] args) {
						if (GameManager.isGameRunning()) {
							GameManager.getGame().stopTimers(DurationTimer.class);
							Bukkit.broadcastMessage("§f" + sender.getName() + "§a님이 능력 지속시간을 모두 초기화하였습니다.");
						} else Messager.sendErrorMessage(sender, "게임이 진행되고 있지 않습니다.");
						return true;
					}
				});
				addSubCommand("kit", new Command() {
					@Override
					protected boolean onCommand(CommandSender sender, String command, String[] args) {
						if (GameManager.isGameOf(DefaultKitHandler.class)) {
							if (args.length == 0)
								Messager.sendErrorMessage(sender, "사용법 §7: §f/" + command + " util kit <대상/@a>");
							else {
								AbstractGame game = GameManager.getGame();
								DefaultKitHandler handler = (DefaultKitHandler) game;
								if (args[0].equalsIgnoreCase("@a")) {
									Bukkit.broadcastMessage("§f" + sender.getName() + "§a님이 §f모든 참가자§a에게 기본템을 다시 지급하였습니다.");
									handler.giveDefaultKit(game.getParticipants());
								} else {
									Player target = Bukkit.getPlayerExact(args[0]);
									if (target != null) {
										if (GameManager.getGame().isParticipating(target)) {
											handler.giveDefaultKit(GameManager.getGame().getParticipant(target));
											SoundLib.ENTITY_EXPERIENCE_ORB_PICKUP.playSound(target);
											Bukkit.broadcastMessage("§f" + sender.getName() + "§a님이 §f" + target.getName() + "§a님에게 기본템을 다시 지급하였습니다.");
										} else
											Messager.sendErrorMessage(sender, target.getName() + "님은 탈락했거나 게임에 참가하지 않았습니다.");
									} else
										Messager.sendErrorMessage(sender, args[0] + KoreanUtil.getJosa(args[0], Josa.은는) + " 존재하지 않는 플레이어입니다.");
								}
							}
						} else Messager.sendErrorMessage(sender, "게임이 진행되고 있지 않거나 기본 킷을 지급할 수 있는 게임이 아닙니다.");
						return true;
					}
				});
				addSubCommand("team", new Command() {
					{
						addSubCommand("list", new Command() {
							@Override
							protected boolean onCommand(CommandSender sender, String command, String[] args) {
								if (GameManager.isGameOf(TeamGame.class)) {
									ArrayList<Team> teams = new ArrayList<>(((TeamGame) GameManager.getGame()).getTeams());
									final int maxPage = (teams.size() / 8) + (teams.size() % 8 == 0 ? 0 : 1);
									int page = 1;
									if (args.length >= 1) {
										if (NumberUtil.isInt(args[0])) {
											page = Integer.parseInt(args[0]);
										} else {
											Messager.sendErrorMessage(sender, "페이지는 자연수로 입력되어야 합니다.");
										}
									}

									if (page > 0 && page <= maxPage) {
										sender.sendMessage("§6===== §e팀 목록 §f(" + page + "페이지 / " + maxPage + "페이지) §6=====");
										for (int i = (page * 8) - 8; i < (page * 8); i++) {
											if (i >= teams.size()) {
												break;
											}
											TeamGame.Team team = teams.get(i);
											sender.sendMessage(ChatColor.YELLOW + "● " + ChatColor.WHITE + team.getName() + ChatColor.WHITE + " (" + team.getDisplayName() + ChatColor.WHITE + ")");
										}
										sender.sendMessage("§6========================================");
									} else {
										Messager.sendErrorMessage(sender, "존재하지 않는 페이지입니다.");
									}
								} else Messager.sendErrorMessage(sender, "게임이 진행되고 있지 않거나 팀 기능을 사용할 수 있는 게임이 아닙니다.");
								return true;
							}
						});
						addSubCommand("set", new Command() {
							@Override
							protected boolean onCommand(CommandSender sender, String command, String[] args) {
								if (GameManager.isGameOf(TeamGame.class)) {
									AbstractGame game = GameManager.getGame();
									TeamGame teamGame = (TeamGame) game;
									if (args.length >= 2) {
										Player targetPlayer = Bukkit.getPlayerExact(args[0]);
										if (targetPlayer != null) {
											if (game.isParticipating(targetPlayer)) {
												Participant target = game.getParticipant(targetPlayer);
												if (teamGame.teamExists(args[1])) {
													TeamGame.Team team = teamGame.getTeam(args[1]);
													if (!teamGame.hasTeam(target) || !teamGame.getTeam(target).equals(team)) {
														teamGame.setTeam(target, team);
														sender.sendMessage("§e" + targetPlayer.getName() + "§f님의 팀을 " + team.getName() + " §f(" + team.getDisplayName() + "§f)" + KoreanUtil.getJosa(team.getName(), Josa.으로로) + " 설정하였습니다.");
													} else
														sender.sendMessage("§e" + targetPlayer.getName() + "§f님은 이미 " + team.getName() + " §f(" + team.getDisplayName() + "§f) 팀의 일원입니다.");
												} else
													Messager.sendErrorMessage(sender, args[1] + KoreanUtil.getJosa(args[1], Josa.은는) + " 존재하지 않는 팀입니다.");
											} else
												Messager.sendErrorMessage(sender, targetPlayer.getName() + "님은 탈락했거나 게임에 참가하지 않았습니다.");
										} else
											Messager.sendErrorMessage(sender, args[0] + KoreanUtil.getJosa(args[0], Josa.은는) + " 존재하지 않는 플레이어입니다.");
									} else
										Messager.sendErrorMessage(sender, "사용법 §7: §f/" + command + " util team set <대상> <팀 이름>");
								} else Messager.sendErrorMessage(sender, "게임이 진행되고 있지 않거나 팀 기능을 사용할 수 있는 게임이 아닙니다.");
								return true;
							}
						});
						addSubCommand("new", new Command() {
							@Override
							protected boolean onCommand(CommandSender sender, String command, String[] args) {
								if (GameManager.isGameOf(TeamGame.class)) {
									if (args.length >= 2) {
										try {
											TeamGame.Team team = ((TeamGame) GameManager.getGame()).newTeam(args[0], ChatColor.translateAlternateColorCodes('&', args[1]));
											Bukkit.broadcastMessage("§e" + sender.getName() + "§f님이 " + team.getName() + "§f(" + team.getDisplayName() + "§f) 팀을 새로 만들었습니다.");
										} catch (IllegalStateException | IllegalArgumentException e) {
											Messager.sendErrorMessage(sender, e.getMessage());
										}
									} else
										Messager.sendErrorMessage(sender, "사용법 §7: §f/" + command + " util team new <팀 이름> <팀 별명>");
								} else Messager.sendErrorMessage(sender, "게임이 진행되고 있지 않거나 팀 기능을 사용할 수 있는 게임이 아닙니다.");
								return true;
							}
						});
						addSubCommand("remove", new Command() {
							@Override
							protected boolean onCommand(CommandSender sender, String command, String[] args) {
								if (GameManager.isGameOf(TeamGame.class)) {
									if (args.length >= 1) {
										TeamGame teamGame = (TeamGame) GameManager.getGame();
										if (teamGame.teamExists(args[0])) {
											TeamGame.Team team = teamGame.getTeam(args[0]);
											teamGame.removeTeam(team);
											Bukkit.broadcastMessage("§e" + sender.getName() + "§f님이 " + team.getName() + "§f(" + team.getDisplayName() + "§f) 팀을 삭제했습니다.");
										} else Messager.sendErrorMessage(sender, args[0] + "은(는) 존재하지 않는 팀입니다.");
									} else
										Messager.sendErrorMessage(sender, "사용법 §7: §f/" + command + " util team remove <팀 이름>");
								} else Messager.sendErrorMessage(sender, "게임이 진행되고 있지 않거나 팀 기능을 사용할 수 있는 게임이 아닙니다.");
								return true;
							}
						});
						addSubCommand("divide", new Command() {
							@Override
							protected boolean onCommand(CommandSender sender, String command, String[] args) {
								if (GameManager.isGameOf(TeamGame.class)) {
									AbstractGame game = GameManager.getGame();
									TeamGame teamGame = (TeamGame) GameManager.getGame();
									for (Participant participant : game.getParticipants()) {
										teamGame.setTeam(participant, null);
									}
									Bukkit.broadcastMessage("§e" + sender.getName() + "§f님이 모든 참가자를 각각 하나의 팀으로 나누었습니다.");
								} else Messager.sendErrorMessage(sender, "게임이 진행되고 있지 않거나 팀 기능을 사용할 수 있는 게임이 아닙니다.");
								return true;
							}
						});
					}

					@Override
					protected boolean onCommand(CommandSender sender, String command, String[] args) {
						if (GameManager.isGameOf(TeamGame.class)) {
							if (args.length > 0) {
								if (NumberUtil.isInt(args[0])) {
									sendTeamUtilCommandHelp(sender, command, Integer.parseInt(args[0]));
								} else
									Messager.sendErrorMessage(sender, "'§4" + args[0] + "§c'" + KoreanUtil.getJosa(args[0], Josa.은는) + " 존재하지 않는 유틸입니다.");
							} else sendTeamUtilCommandHelp(sender, command, 1);
						} else Messager.sendErrorMessage(sender, "게임이 진행되고 있지 않거나 팀 기능을 사용할 수 있는 게임이 아닙니다.");
						return true;
					}

					private void sendTeamUtilCommandHelp(CommandSender sender, String label, int page) {
						final int allPage = 1;
						if (page == 1) {
							sender.sendMessage(new String[]{
									Formatter.formatTitle(ChatColor.GOLD, ChatColor.YELLOW, "능력자 전쟁 팀 유틸"),
									"§b/" + label + " util team <페이지> §7로 더 많은 명령어를 확인하세요! ( §b" + page + " 페이지 §7/ §b" + allPage + " 페이지 §7)",
									Formatter.formatCommand(label + " util team", "list [페이지]", "팀 목록을 확인합니다.", true),
									Formatter.formatCommand(label + " util team", "set <대상> <팀 이름>", "대상의 팀을 설정합니다.", true),
									Formatter.formatCommand(label + " util team", "new <팀 이름> <팀 별명>", "새로운 팀을 만듭니다.", true),
									Formatter.formatCommand(label + " util team", "remove <팀 이름>", "팀을 삭제합니다.", true),
									Formatter.formatCommand(label + " util team", "divide", "모든 플레이어를 각각 하나의 팀으로 나눕니다.", true)
							});
						} else Messager.sendErrorMessage(sender, "존재하지 않는 페이지입니다.");
					}

				});
			}

			@Override
			protected boolean onCommand(CommandSender sender, String command, String[] args) {
				if (args.length > 0) {
					if (NumberUtil.isInt(args[0])) {
						sendUtilCommandHelp(sender, command, Integer.parseInt(args[0]));
					} else
						Messager.sendErrorMessage(sender, "'§4" + args[0] + "§c'" + KoreanUtil.getJosa(args[0], Josa.은는) + " 존재하지 않는 유틸입니다.");
				} else sendUtilCommandHelp(sender, command, 1);
				return true;
			}

			private void sendUtilCommandHelp(CommandSender sender, String label, int page) {
				final int allPage = 2;
				switch (page) {
					case 1:
						sender.sendMessage(new String[]{Formatter.formatTitle(ChatColor.GOLD, ChatColor.YELLOW, "능력자 전쟁 유틸"),
								"§b/" + label + " util <페이지> §7로 더 많은 명령어를 확인하세요! ( §b" + page + " 페이지 §7/ §b" + allPage + " 페이지 §7)",
								Formatter.formatCommand(label + " util", "abi <대상/@a>", "능력 부여 GUI를 엽니다.", true),
								Formatter.formatCommand(label + " util", "abi <대상/@a> [능력]", "대상에게 [능력] 능력을 부여합니다.", true),
								Formatter.formatCommand(label + " util", "inv [시간(초)]", "무적 상태를 토글합니다.", true),
								Formatter.formatCommand(label + " util", "spec", "관전자 설정 GUI를 엽니다.", true),
								Formatter.formatCommand(label + " util", "spec [대상]", "[대상]의 관전 상태를 토글합니다.", true),
								Formatter.formatCommand(label + " util", "ablist", "능력자 목록을 확인합니다.", true)});
						break;
					case 2:
						sender.sendMessage(new String[]{Formatter.formatTitle(ChatColor.GOLD, ChatColor.YELLOW, "능력자 전쟁 유틸"),
								"§b/" + label + " util <페이지> §7로 더 많은 명령어를 확인하세요! ( §b" + page + " 페이지 §7/ §b" + allPage + " 페이지 §7)",
								Formatter.formatCommand(label + " util", "resetcool", "참가자들의 능력 쿨타임을 초기화시킵니다.", true),
								Formatter.formatCommand(label + " util", "resetduration", "참가자들의 능력 지속시간을 초기화시킵니다.", true),
								Formatter.formatCommand(label + " util", "kit <대상/@a>", "대상에게 기본템을 다시 지급합니다.", true),
								Formatter.formatCommand(label + " util", "team", "팀 유틸 명령어를 확인합니다.", true)});
						break;
					default:
						Messager.sendErrorMessage(sender, "존재하지 않는 페이지입니다.");
						break;
				}
			}
		};
		mainCommand.addSubCommand("util", utilCommand);
		mainCommand.addSubCommand("script", new Command(Condition.PLAYER, Condition.OP) {
			private final Pattern SCRIPT_NAME_PATTERN = Pattern.compile("^[가-힣a-zA-Z0-9_]+$");

			@Override
			protected boolean onCommand(CommandSender sender, String command, String[] args) {
				if (args.length > 1) {
					try {
						Class<? extends AbstractScript> scriptClass = ScriptManager.getScriptClass(args[0]);
						if (scriptClass != null) {
							if (SCRIPT_NAME_PATTERN.matcher(args[1]).find() && args[1].length() <= 10) {
								File file = new File("plugins/" + plugin.getName() + "/Script/" + args[1] + ".yml");
								if (!file.exists()) {
									new ScriptWizard((Player) sender, plugin, scriptClass, args[1]).openGUI(1);
								} else {
									Messager.sendErrorMessage(sender, "§e" + args[1] + ".yml §f스크립트 파일이 이미 존재합니다.");
								}
							} else {
								Messager.sendErrorMessage(sender, "§c'§4" + args[1] + "§c'" + KoreanUtil.getJosa(args[1], Josa.은는) + " 사용할 수 없는 이름입니다.");
							}
						} else {
							Messager.sendErrorMessage(sender, "§c'§4" + args[0] + "§c'" + KoreanUtil.getJosa(args[0], Josa.은는) + " 존재하지 않는 스크립트 유형입니다.");
						}
					} catch (IllegalArgumentException ex) {
						Messager.sendErrorMessage(sender, "사용할 수 없는 스크립트 유형입니다.");
						if (ex.getMessage() != null && !ex.getMessage().isEmpty()) {
							logger.error(ex.getMessage());
						}
					}
				} else Messager.sendErrorMessage(sender, "사용법 §7: §f/" + command + " script <유형> <이름>");
				return true;
			}
		});
		mainCommand.addSubCommand("gamemode", new Command(Condition.OP) {
			@Override
			protected boolean onCommand(CommandSender sender, String command, String[] args) {
				if (args.length == 0) {
					if (sender instanceof Player) {
						new GameModeGUI((Player) sender, plugin).openGUI(1);
					} else Messager.sendErrorMessage(sender, "사용법 §7: §f/" + command + " gamemode [게임 모드]");
				} else {
					String name = String.join(" ", args);
					if (GameFactory.isRegistered(name)) {
						GameRegistration registration = GameFactory.getRegistration(GameFactory.getByName(name));
						Configuration.modifyProperty(ConfigNodes.GAME_MODE, registration.getGameClass().getName());
						sender.sendMessage("§2게임 모드§a가 §f" + registration.getManifest().name() + "§a" + KoreanUtil.getJosa(registration.getManifest().name(), Josa.으로로) + " 변경되었습니다.");
					} else
						Messager.sendErrorMessage(sender, name + KoreanUtil.getJosa(name, Josa.은는) + " 존재하지 않는 게임 모드입니다.");
				}
				return true;
			}
		});
		mainCommand.addSubCommand("install", new Command(Condition.OP, Condition.PLAYER) {
			@Override
			protected boolean onCommand(CommandSender sender, String command, String[] args) {
				try {
					new InstallGUI((Player) sender, plugin, plugin.getInstaller()).openGUI(1);
				} catch (IllegalStateException e) {
					Messager.sendErrorMessage(sender, "아직 버전 목록이 불러와지지 않았습니다.");
				}
				return true;
			}
		});
		mainCommand.addSubCommand("team", new Command(Condition.PLAYER) {
			{
				addSubCommand("chat", new Command() {
					@Override
					protected boolean onCommand(CommandSender sender, String command, String[] args) {
						Player player = (Player) sender;
						if (GameManager.isGameOf(TeamGame.class)) {
							AbstractGame game = GameManager.getGame();
							if (game.isParticipating(player)) {
								Participant participant = game.getParticipant(player);
								if (participant.attributes().TEAM_CHAT.setValue(!participant.attributes().TEAM_CHAT.getValue())) {
									player.sendMessage("§5[§d팀§5] §f팀 채팅이 비활성화되었습니다.");
								} else {
									player.sendMessage("§5[§d팀§5] §f팀 채팅이 활성화되었습니다.");
								}
							} else Messager.sendErrorMessage(sender, "게임에 참가하고 있지 않습니다.");
						} else Messager.sendErrorMessage(player, "게임이 진행되고 있지 않거나 팀 기능을 사용할 수 있는 게임이 아닙니다.");
						return true;
					}
				});
				addSubCommand("info", new Command() {
					@Override
					protected boolean onCommand(CommandSender sender, String command, String[] args) {
						Player player = (Player) sender;
						if (GameManager.isGameOf(TeamGame.class)) {
							AbstractGame game = GameManager.getGame();
							TeamGame teamGame = (TeamGame) game;
							if (game.isParticipating(player)) {
								Participant participant = game.getParticipant(player);
								if (teamGame.hasTeam(participant)) {
									for (String str : Formatter.formatTeamInfo(teamGame, teamGame.getTeam(participant))) {
										player.sendMessage(str);
									}
								} else Messager.sendErrorMessage(player, "팀에 소속되지 않았습니다.");
							} else Messager.sendErrorMessage(sender, "게임에 참가하고 있지 않습니다.");
						} else Messager.sendErrorMessage(player, "게임이 진행되고 있지 않거나 팀 기능을 사용할 수 있는 게임이 아닙니다.");
						return true;
					}
				});
			}

			@Override
			protected boolean onCommand(CommandSender sender, String command, String[] args) {
				Player p = (Player) sender;
				if (GameManager.isGameOf(TeamGame.class)) {
					AbstractGame game = GameManager.getGame();
					if (game.isParticipating(p)) {
						if (args.length > 0) {
							if (NumberUtil.isInt(args[0])) {
								sendTeamCommandHelp(sender, command, Integer.parseInt(args[0]));
							} else
								Messager.sendErrorMessage(sender, "'§4" + args[0] + "§c'" + KoreanUtil.getJosa(args[0], Josa.은는) + " 존재하지 않는 팀 명령어입니다.");
						} else sendTeamCommandHelp(sender, command, 1);
					} else Messager.sendErrorMessage(sender, "게임에 참가하고 있지 않습니다.");
				} else Messager.sendErrorMessage(p, "게임이 진행되고 있지 않거나 팀 기능을 사용할 수 있는 게임이 아닙니다.");
				return true;
			}

			private void sendTeamCommandHelp(CommandSender sender, String label, int page) {
				final int allPage = 1;
				if (page == 1) {
					sender.sendMessage(new String[]{
							Formatter.formatTitle(ChatColor.GOLD, ChatColor.YELLOW, "능력자 전쟁 팀 명령어"),
							"§b/" + label + " team <페이지> §7로 더 많은 명령어를 확인하세요! ( §b" + page + " 페이지 §7/ §b" + allPage + " 페이지 §7)",
							Formatter.formatCommand(label + " team", "chat", "팀 채팅을 토글합니다.", false),
							Formatter.formatCommand(label + " team", "info", "속해있는 팀 정보를 확인합니다.", false)
					});
				} else Messager.sendErrorMessage(sender, "존재하지 않는 페이지입니다.");
			}

		});

		mainCommand.addSubCommand("specialthanks", new Command(Condition.PLAYER) {
			@Override
			protected boolean onCommand(CommandSender sender, String command, String[] args) {
				new SpecialThanksGUI((Player) sender, plugin).openGUI(1);
				return true;
			}
		});
	}

	@Override
	public boolean onCommand(CommandSender sender, org.bukkit.command.Command cmd, String label, String[] args) {
		mainCommand.handleCommand(sender, label, args);
		return true;
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, org.bukkit.command.Command cmd, String label, String[] args) {
		if (label.equalsIgnoreCase("abilitywar") || label.equalsIgnoreCase("ability") || label.equalsIgnoreCase("aw")
				|| label.equalsIgnoreCase("va") || label.equalsIgnoreCase("능력자")) {
			switch (args.length) {
				case 1:
					List<String> subCommands = Messager.asList("start", "stop", "check", "yes", "no", "abilities", "skip", "anew",
							"config", "util", "script", "gamemode", "install", "team", "specialthanks");
					if (args[0].isEmpty()) {
						return subCommands;
					} else {
						subCommands.removeIf(command -> !command.toLowerCase().startsWith(args[0].toLowerCase()));
						return subCommands;
					}
				case 2:
					if (args[0].equalsIgnoreCase("config")) {
						List<String> configs = Messager.asList("kit", "spawn", "inv", "game", "death", "ability", "teampreset", "blacklist");
						if (args[1].isEmpty()) {
							return configs;
						} else {
							configs.removeIf(config -> !config.toLowerCase().startsWith(args[1].toLowerCase()));
							return configs;
						}
					} else if (args[0].equalsIgnoreCase("util")) {
						List<String> utils = Messager.asList("abi", "spec", "ablist", "resetcool",
								"resetduration", "kit", "inv", "team");
						if (args[1].isEmpty()) {
							return utils;
						} else {
							utils.removeIf(util -> !util.toLowerCase().startsWith(args[1].toLowerCase()));
							return utils;
						}
					} else if (args[0].equalsIgnoreCase("script")) {
						List<String> scripts = new ArrayList<>(ScriptManager.getRegisteredScriptNames());
						if (args[1].isEmpty()) {
							return scripts;
						} else {
							scripts.removeIf(script -> !script.toLowerCase().startsWith(args[1].toLowerCase()));
							return scripts;
						}
					} else if (args[0].equalsIgnoreCase("team")) {
						List<String> commands = Messager.asList("chat", "info");
						if (args[1].isEmpty()) {
							return commands;
						} else {
							commands.removeIf(util -> !util.toLowerCase().startsWith(args[1].toLowerCase()));
							return commands;
						}
					}
					break;
				case 3:
					if (args[0].equalsIgnoreCase("util")) {
						if (args[1].equalsIgnoreCase("abi") || args[1].equalsIgnoreCase("kit")) {
							List<String> players = Bukkit.getOnlinePlayers().stream().map(Player::getName).collect(Collectors.toList());
							players.add("@a");
							players.sort(String::compareToIgnoreCase);
							if (args[2].isEmpty()) {
								return players;
							} else {
								players.removeIf(name -> !name.toLowerCase().startsWith(args[2].toLowerCase()));
								return players;
							}
						} else if (args[1].equalsIgnoreCase("team")) {
							List<String> teamUtils = Messager.asList("list", "set", "new", "remove", "divide");
							if (args[2].isEmpty()) {
								return teamUtils;
							} else {
								teamUtils.removeIf(util -> !util.toLowerCase().startsWith(args[2].toLowerCase()));
								return teamUtils;
							}
						}
					}
					break;
				case 4:
					if (args[0].equalsIgnoreCase("util")) {
						if (args[1].equalsIgnoreCase("team")) {
							if (args[2].equalsIgnoreCase("set")) {
								List<String> players = Bukkit.getOnlinePlayers().stream().map(Player::getName).collect(Collectors.toList());
								players.sort(String::compareToIgnoreCase);
								if (args[3].isEmpty()) {
									return players;
								} else {
									players.removeIf(name -> !name.toLowerCase().startsWith(args[3].toLowerCase()));
									return players;
								}
							} else if (args[2].equalsIgnoreCase("remove")) {
								if (GameManager.isGameOf(TeamGame.class)) {
									TeamGame teamGame = (TeamGame) GameManager.getGame();
									return teamGame.getTeams().stream().map(TeamGame.Team::getName).collect(Collectors.toList());
								}
							}
						}
					}
					break;
				case 5:
					if (args[0].equalsIgnoreCase("util")) {
						if (args[1].equalsIgnoreCase("team") && args[2].equalsIgnoreCase("set")) {
							if (GameManager.isGameOf(TeamGame.class)) {
								TeamGame teamGame = (TeamGame) GameManager.getGame();
								return teamGame.getTeams().stream().map(TeamGame.Team::getName).collect(Collectors.toList());
							}
						}
					}
			}
		}

		return null;
	}

}
