package daybreak.abilitywar;

import daybreak.abilitywar.Command.Condition;
import daybreak.abilitywar.ability.AbilityFactory;
import daybreak.abilitywar.config.ability.wizard.AbilitySettingWizard;
import daybreak.abilitywar.config.wizard.DeathWizard;
import daybreak.abilitywar.config.wizard.GameWizard;
import daybreak.abilitywar.config.wizard.InvincibilityWizard;
import daybreak.abilitywar.config.wizard.KitWizard;
import daybreak.abilitywar.config.wizard.SpawnWizard;
import daybreak.abilitywar.game.AbstractGame;
import daybreak.abilitywar.game.AbstractGame.Participant;
import daybreak.abilitywar.game.GameManager;
import daybreak.abilitywar.game.manager.gui.AbilityListGUI;
import daybreak.abilitywar.game.manager.gui.TeamPresetGUI;
import daybreak.abilitywar.game.manager.object.AbilitySelect;
import daybreak.abilitywar.game.manager.object.CommandHandler;
import daybreak.abilitywar.utils.base.Formatter;
import daybreak.abilitywar.utils.base.Messager;
import daybreak.abilitywar.utils.base.language.korean.KoreanUtil;
import daybreak.abilitywar.utils.base.language.korean.KoreanUtil.Josa;
import daybreak.abilitywar.utils.base.logging.Logger;
import daybreak.abilitywar.utils.base.math.NumberUtil;
import java.util.Arrays;
import java.util.List;
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
								Formatter.formatCommand(command, "gamemode", "능력자 전쟁 게임 모드를 설정합니다.", true),
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
					Messager.sendErrorMessage(sender, "능력자 전쟁이 진행되고 있지 않습니다.");
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
				System.out.println(Arrays.toString(args));
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
								Formatter.formatCommand(label + " config", "teampreset", "팀 프리셋 설정 GUI를 엽니다.", true)});
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
				addSubCommand("abi", new Command(Condition.PLAYER) {
					@Override
					protected boolean onCommand(CommandSender sender, String command, String[] args) {
						if (GameManager.isGameRunning()) {
							if (args.length < 1) {
								Messager.sendErrorMessage(sender, "사용법 §7: §f/" + command + " util abi <대상/@a>");
							} else
								GameManager.getGame().executeCommand(CommandHandler.CommandType.ABI, (Player) sender, args, plugin);
						} else Messager.sendErrorMessage(sender, "능력자 전쟁이 진행되고 있지 않습니다.");
						return true;
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
								Formatter.formatCommand(label + " util", "abi <대상/@a>", "대상에게 능력을 임의로 부여합니다.", true),
								Formatter.formatCommand(label + " util", "inv [시간(초)]", "무적 상태를 토글합니다.", true),
								Formatter.formatCommand(label + " util", "spec", "관전자 설정 GUI를 띄웁니다.", true),
								Formatter.formatCommand(label + " util", "ablist", "능력자 목록을 확인합니다.", true),
								Formatter.formatCommand(label + " util", "blacklist", "능력 블랙리스트 설정 GUI를 띄웁니다.", true)});
						break;
					case 2:
						sender.sendMessage(new String[]{Formatter.formatTitle(ChatColor.GOLD, ChatColor.YELLOW, "능력자 전쟁 유틸"),
								"§b/" + label + " util <페이지> §7로 더 많은 명령어를 확인하세요! ( §b" + page + " 페이지 §7/ §b" + allPage + " 페이지 §7)",
								Formatter.formatCommand(label + " util", "resetcool", "플레이어들의 능력 쿨타임을 초기화시킵니다.", true),
								Formatter.formatCommand(label + " util", "resetduration", "플레이어들의 능력 지속시간을 초기화시킵니다.", true),
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
	}

	@Override
	public boolean onCommand(CommandSender sender, org.bukkit.command.Command cmd, String label, String[] args) {
		mainCommand.handle(sender, label, args);
		return true;
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, org.bukkit.command.Command cmd, String label, String[] args) {
		return null;
	}

}
