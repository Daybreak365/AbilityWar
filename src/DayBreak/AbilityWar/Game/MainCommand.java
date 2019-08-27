package DayBreak.AbilityWar.Game;

import java.io.File;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import DayBreak.AbilityWar.AbilityWar;
import DayBreak.AbilityWar.Ability.AbilityBase;
import DayBreak.AbilityWar.Ability.Timer.CooldownTimer;
import DayBreak.AbilityWar.Ability.Timer.DurationTimer;
import DayBreak.AbilityWar.Config.AbilitySettings;
import DayBreak.AbilityWar.Config.AbilityWarSettings;
import DayBreak.AbilityWar.Config.SettingWizard;
import DayBreak.AbilityWar.Game.Games.Mode.AbstractGame;
import DayBreak.AbilityWar.Game.Games.Mode.AbstractGame.AbilitySelect;
import DayBreak.AbilityWar.Game.Games.Mode.AbstractGame.Participant;
import DayBreak.AbilityWar.Game.Games.Mode.GameMode;
import DayBreak.AbilityWar.Game.Manager.Invincibility;
import DayBreak.AbilityWar.Game.Manager.GUI.AbilityGUI;
import DayBreak.AbilityWar.Game.Manager.GUI.BlackListGUI;
import DayBreak.AbilityWar.Game.Manager.GUI.GameModeGUI;
import DayBreak.AbilityWar.Game.Manager.GUI.SpecialThanksGUI;
import DayBreak.AbilityWar.Game.Manager.GUI.SpectatorGUI;
import DayBreak.AbilityWar.Game.Script.Script;
import DayBreak.AbilityWar.Game.Script.ScriptException;
import DayBreak.AbilityWar.Game.Script.ScriptWizard;
import DayBreak.AbilityWar.Game.Script.Objects.AbstractScript;
import DayBreak.AbilityWar.Utils.Messager;
import DayBreak.AbilityWar.Utils.Library.SoundLib;
import DayBreak.AbilityWar.Utils.Math.NumberUtil;
import DayBreak.AbilityWar.Utils.Thread.AbilityWarThread;

/**
 * 메인 명령어
 * @author DayBreak 새벽
 */
public class MainCommand implements CommandExecutor, TabCompleter {
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		parseCommand(sender, label, args);
		return true;
	}
	
	private void parseCommand(CommandSender sender, String label, String[] split) {
		if(split.length == 0) {
			Messager.sendStringList(sender, Messager.getStringList(
					Messager.formatTitle(ChatColor.GOLD, ChatColor.YELLOW, "능력자 전쟁"),
					ChatColor.translateAlternateColorCodes('&', "&e버전 &7: &f" + AbilityWar.getPlugin().getDescription().getVersion()),
					ChatColor.translateAlternateColorCodes('&', "&b개발자 &7: &fDayBreak 새벽"),
					ChatColor.translateAlternateColorCodes('&', "&9디스코드 &7: &fDayBreak&7#5908"),
					ChatColor.translateAlternateColorCodes('&', "&3&o/" + label + " help &7&o로 명령어 도움말을 확인하세요.")));
		} else {
			if(split[0].equalsIgnoreCase("help")) {
				if(split.length > 1) {
					if(NumberUtil.isInt(split[1])) {
						sendHelpCommand(sender, label, Integer.valueOf(split[1]));
					} else {
						Messager.sendErrorMessage(sender, "존재하지 않는 페이지입니다.");
					}
				} else {
					sendHelpCommand(sender, label, 1);
				}
			} else if(split[0].equalsIgnoreCase("start")) {
				if(sender.isOp()) {
					if(!AbilityWarThread.isGameTaskRunning()) {
						if(!GameMode.startGame()) {
							Messager.sendErrorMessage(sender, "게임을 시작시키는 도중에 오류가 발생하여 기본 게임을 시작시킵니다.");
						}
						Messager.broadcastMessage(ChatColor.translateAlternateColorCodes('&', "&f관리자 &e" + sender.getName() + "&f님이 게임을 시작시켰습니다."));
					} else {
						Messager.sendErrorMessage(sender, ChatColor.translateAlternateColorCodes('&', "&c능력자 전쟁이 이미 진행되고 있습니다."));
					}
				} else {
					Messager.sendErrorMessage(sender, ChatColor.translateAlternateColorCodes('&', "&c이 명령어를 사용하려면 OP 권한이 있어야 합니다."));
				}
			} else if(split[0].equalsIgnoreCase("stop")) {
				if(sender.isOp()) {
					if(AbilityWarThread.isGameTaskRunning()) {
						Messager.broadcastMessage(ChatColor.translateAlternateColorCodes('&', "&f관리자 &e" + sender.getName() + "&f님이 게임을 중지시켰습니다."));
							
						AbilityWarThread.StopGame();
					} else {
						Messager.sendErrorMessage(sender, ChatColor.translateAlternateColorCodes('&', "&c능력자 전쟁이 진행되고 있지 않습니다."));
					}
				} else {
					Messager.sendErrorMessage(sender, ChatColor.translateAlternateColorCodes('&', "&c이 명령어를 사용하려면 OP 권한이 있어야 합니다."));
				}
			} else if(split[0].equalsIgnoreCase("reload")) {
				if(sender.isOp()) {
					AbilityWarSettings.Refresh();
					AbilitySettings.Refresh();
					
					AbilityWarSettings.Setup();
					AbilitySettings.Setup();
					Script.LoadAll();
					Messager.sendMessage(sender, ChatColor.translateAlternateColorCodes('&', "&f능력자 전쟁 &b콘피그가 리로드되었습니다!"));
				} else {
					Messager.sendErrorMessage(sender, ChatColor.translateAlternateColorCodes('&', "&c이 명령어를 사용하려면 OP 권한이 있어야 합니다."));
				}
			} else if(split[0].equalsIgnoreCase("config")) {
				if(sender instanceof Player) {
					Player p = (Player) sender;
					if(p.isOp()) {
						
						if(split.length > 1) {
							parseConfigCommand(p, label, Messager.removeFirstArg(split));
						} else {
							sendHelpConfigCommand(p, label, 1);
						}
					} else {
						Messager.sendErrorMessage(p, ChatColor.translateAlternateColorCodes('&', "&c이 명령어를 사용하려면 OP 권한이 있어야 합니다."));
					}
				} else {
					Messager.sendErrorMessage(sender, ChatColor.translateAlternateColorCodes('&', "&c콘솔에서 사용할 수 없는 명령어입니다!"));
				}
			} else if(split[0].equalsIgnoreCase("check")) {
				if(sender instanceof Player) {
					Player p = (Player) sender;
					if(AbilityWarThread.isGameTaskRunning()) {
						AbstractGame game = AbilityWarThread.getGame();
						if(game.isParticipating(p)) {
							Participant participant = game.getParticipant(p);
							if(participant.hasAbility()) {
								Messager.sendStringList(p, Messager.formatAbilityInfo(participant.getAbility()));
							} else {
								Messager.sendErrorMessage(sender, ChatColor.translateAlternateColorCodes('&', "&c당신에게 능력이 할당되지 않았습니다."));
							}
						} else {
							Messager.sendErrorMessage(sender, ChatColor.translateAlternateColorCodes('&', "&c게임에 참가하고 있지 않습니다."));
						}
					} else {
						Messager.sendErrorMessage(sender, ChatColor.translateAlternateColorCodes('&', "&c능력자 전쟁이 진행되고 있지 않습니다."));
					}
				} else {
					Messager.sendErrorMessage(sender, ChatColor.translateAlternateColorCodes('&', "&c콘솔에서 사용할 수 없는 명령어입니다!"));
				}
			} else if(split[0].equalsIgnoreCase("yes")) {
				if(sender instanceof Player) {
					Player p = (Player) sender;
					if(AbilityWarThread.isGameTaskRunning()) {
						AbstractGame game = AbilityWarThread.getGame();
						if(game.isParticipating(p)) {
							Participant participant = game.getParticipant(p);
							AbilitySelect select = AbilityWarThread.getGame().getAbilitySelect();
							if(select != null && !select.isEnded()) {
								if(select.isSelector(participant)) {
									if(!select.hasDecided(participant)) {
										select.decideAbility(participant);
									} else {
										Messager.sendMessage(p, ChatColor.translateAlternateColorCodes('&', "&c이미 능력 선택을 마치셨습니다."));
									}
								} else {
									Messager.sendErrorMessage(sender, ChatColor.translateAlternateColorCodes('&', "&c당신은 능력을 선택하고 있지 않습니다."));
								}
							} else {
								Messager.sendErrorMessage(sender, ChatColor.translateAlternateColorCodes('&', "&c능력을 선택하는 중이 아닙니다."));
							}
						} else {
							Messager.sendErrorMessage(sender, ChatColor.translateAlternateColorCodes('&', "&c게임에 참가하고 있지 않습니다."));
						}
					} else {
						Messager.sendErrorMessage(sender, ChatColor.translateAlternateColorCodes('&', "&c능력자 전쟁이 진행되고 있지 않습니다."));
					}
				} else {
					Messager.sendErrorMessage(sender, ChatColor.translateAlternateColorCodes('&', "&c콘솔에서 사용할 수 없는 명령어입니다!"));
				}
			} else if(split[0].equalsIgnoreCase("no")) {
				if(sender instanceof Player) {
					Player p = (Player) sender;
					if(AbilityWarThread.isGameTaskRunning()) {
						AbstractGame game = AbilityWarThread.getGame();
						if(game.isParticipating(p)) {
							Participant participant = game.getParticipant(p);
							AbilitySelect select = AbilityWarThread.getGame().getAbilitySelect();
							if(select != null && !select.isEnded()) {
								if(select.isSelector(participant)) {
									if(!select.hasDecided(participant)) {
										select.alterAbility(participant);
									} else {
										Messager.sendMessage(p, ChatColor.translateAlternateColorCodes('&', "&c이미 능력 선택을 마치셨습니다."));
									}
								} else {
									Messager.sendErrorMessage(sender, ChatColor.translateAlternateColorCodes('&', "&c당신은 능력을 선택하고 있지 않습니다."));
								}
							} else {
								Messager.sendErrorMessage(sender, ChatColor.translateAlternateColorCodes('&', "&c능력을 선택하는 중이 아닙니다."));
							}
						} else {
							Messager.sendErrorMessage(sender, ChatColor.translateAlternateColorCodes('&', "&c게임에 참가하고 있지 않습니다."));
						}
					} else {
						Messager.sendErrorMessage(sender, ChatColor.translateAlternateColorCodes('&', "&c능력자 전쟁이 진행되고 있지 않습니다."));
					}
				} else {
					Messager.sendErrorMessage(sender, ChatColor.translateAlternateColorCodes('&', "&c콘솔에서 사용할 수 없는 명령어입니다!"));
				}
			} else if(split[0].equalsIgnoreCase("skip")) {
				if(sender.isOp()) {
					if(AbilityWarThread.isGameTaskRunning()) {
						AbilitySelect select = AbilityWarThread.getGame().getAbilitySelect();
						if(select != null && !select.isEnded()) {
							select.Skip(sender.getName());
						} else {
							Messager.sendErrorMessage(sender, ChatColor.translateAlternateColorCodes('&', "&c능력을 선택하는 중이 아닙니다."));
						}
					} else {
						Messager.sendErrorMessage(sender, ChatColor.translateAlternateColorCodes('&', "&c능력자 전쟁이 진행되고 있지 않습니다."));
					}
				} else {
					Messager.sendErrorMessage(sender, ChatColor.translateAlternateColorCodes('&', "&c이 명령어를 사용하려면 OP 권한이 있어야 합니다."));
				}
			} else if(split[0].equalsIgnoreCase("util")) {
				if(sender instanceof Player) {
					Player p = (Player) sender;
					if(p.isOp()) {
						
						if(split.length > 1) {
							parseUtilCommand(p, label, Messager.removeFirstArg(split));
						} else {
							sendHelpUtilCommand(p, label, 1);
						}
					} else {
						Messager.sendErrorMessage(p, ChatColor.translateAlternateColorCodes('&', "&c이 명령어를 사용하려면 OP 권한이 있어야 합니다."));
					}
				} else {
					Messager.sendErrorMessage(sender, ChatColor.translateAlternateColorCodes('&', "&c콘솔에서 사용할 수 없는 명령어입니다!"));
				}
			} else if(split[0].equalsIgnoreCase("script")) {
				if(sender instanceof Player) {
					Player p = (Player) sender;
					if(p.isOp()) {
						if(split.length > 2) {
							try {
								Class<? extends AbstractScript> scriptClass = Script.getScriptClass(split[1]);
								if(Pattern.compile("^[가-힣a-zA-Z0-9_]+$").matcher(split[2]).find() && split[2].length() <= 10) {
									File file = new File("plugins/" + AbilityWar.getPlugin().getName() + "/Script/" + split[2] + ".yml");
									if(!file.exists()) {
										ScriptWizard wizard = new ScriptWizard(p, AbilityWar.getPlugin(), scriptClass, split[2]);
										wizard.openScriptWizard(1);
									} else {
										Messager.sendMessage(p, ChatColor.translateAlternateColorCodes('&', "&e" + split[2] + ".yml &f스크립트 파일이 이미 존재합니다."));
									}
								} else {
									Messager.sendMessage(p, ChatColor.translateAlternateColorCodes('&', "&e" + split[2] + "&f은(는) 사용할 수 없는 이름입니다."));
								}
							} catch(ClassNotFoundException | ScriptException ex) {
								Messager.sendErrorMessage(p, ChatColor.translateAlternateColorCodes('&', "&c존재하지 않는 스크립트 유형입니다."));
							} catch(IllegalArgumentException ex) {
								Messager.sendErrorMessage(p, ChatColor.translateAlternateColorCodes('&', "&c사용할 수 없는 스크립트 유형입니다."));
								if(ex.getMessage() != null && !ex.getMessage().isEmpty()) {
									Messager.sendErrorMessage(ex.getMessage());
								}
							}
						} else {
							Messager.sendErrorMessage(p, ChatColor.translateAlternateColorCodes('&', "사용법 &7: &f/" + label + " script <유형> <이름>"));
						}
					} else {
						Messager.sendErrorMessage(p, ChatColor.translateAlternateColorCodes('&', "&c이 명령어를 사용하려면 OP 권한이 있어야 합니다."));
					}
				} else {
					Messager.sendErrorMessage(sender, ChatColor.translateAlternateColorCodes('&', "&c콘솔에서 사용할 수 없는 명령어입니다!"));
				}
			} else if(split[0].equalsIgnoreCase("gamemode")) {
				if(sender instanceof Player) {
					Player p = (Player) sender;
					if(p.isOp()) {
						GameModeGUI gui = new GameModeGUI(p, AbilityWar.getPlugin());
						gui.openGameModeGUI(1);
					} else {
						Messager.sendErrorMessage(p, ChatColor.translateAlternateColorCodes('&', "&c이 명령어를 사용하려면 OP 권한이 있어야 합니다."));
					}
				} else {
					Messager.sendErrorMessage(sender, ChatColor.translateAlternateColorCodes('&', "&c콘솔에서 사용할 수 없는 명령어입니다!"));
				}
			} else if(split[0].equalsIgnoreCase("update")) {
				if(sender.isOp()) {
					if(!AbilityWar.getPlugin().getAutoUpdate().Update(sender)) {
						Messager.sendErrorMessage(sender, ChatColor.translateAlternateColorCodes('&', "&f플러그인이 &3최신 버전 &7(" + AbilityWar.getPlugin().getDescription().getVersion() + ") &f입니다."));
					}
				} else {
					Messager.sendErrorMessage(sender, ChatColor.translateAlternateColorCodes('&', "&c이 명령어를 사용하려면 OP 권한이 있어야 합니다."));
				}
			} else if(split[0].equalsIgnoreCase("specialthanks")) {
				if(sender instanceof Player) {
					Player p = (Player) sender;
					SpecialThanksGUI gui = new SpecialThanksGUI(p, AbilityWar.getPlugin());
					gui.openGUI(1);
				} else {
					Messager.sendErrorMessage(sender, ChatColor.translateAlternateColorCodes('&', "&c콘솔에서 사용할 수 없는 명령어입니다!"));
				}
			} else {
				Messager.sendErrorMessage(sender, ChatColor.translateAlternateColorCodes('&', "존재하지 않는 서브 명령어입니다."));
			}
			
		}
	}

	private void parseConfigCommand(Player p, String label, String[] args) {
		SettingWizard wizard = new SettingWizard(p, AbilityWar.getPlugin());
		if(args[0].equalsIgnoreCase("kit")) {
			wizard.openKitGUI();
		} else if(args[0].equalsIgnoreCase("spawn")) {
			wizard.openSpawnGUI();
		} else if(args[0].equalsIgnoreCase("inv")) {
			wizard.openInvincibilityGUI();
		} else if(args[0].equalsIgnoreCase("game")) {
			wizard.openGameGUI();
		} else if(args[0].equalsIgnoreCase("death")) {
			wizard.openDeathGUI();
		} else {
			if(NumberUtil.isInt(args[0])) {
				sendHelpConfigCommand(p, label, Integer.valueOf(args[0]));
			} else {
				Messager.sendErrorMessage(p, "존재하지 않는 콘피그입니다.");
			}
		}
	}

	private void parseUtilCommand(Player p, String label, String[] args) {
		if(args[0].equalsIgnoreCase("abi")) {
			if(AbilityWarThread.isGameTaskRunning()) {
				if(args.length < 2) {
					Messager.sendErrorMessage(p, ChatColor.translateAlternateColorCodes('&', "사용법 &7: &f/" + label + " util abi <대상>"));
				} else {
					if(args[1].equalsIgnoreCase("@a")) {
						AbilityGUI gui = new AbilityGUI(p, AbilityWar.getPlugin());
						gui.openAbilityGUI(1);
					} else {
						if(Bukkit.getPlayerExact(args[1]) != null) {
							AbstractGame game = AbilityWarThread.getGame();
							Player targetPlayer = Bukkit.getPlayerExact(args[1]);
							if(game.isParticipating(targetPlayer)) {
								Participant target = game.getParticipant(targetPlayer);
								AbilityGUI gui = new AbilityGUI(p, target, AbilityWar.getPlugin());
								gui.openAbilityGUI(1);
							} else {
								Messager.sendErrorMessage(p, targetPlayer.getName() + "님은 탈락했거나 게임에 참여하지 않았습니다.");
							}
						} else {
							Messager.sendErrorMessage(p, args[1] + "은(는) 존재하지 않는 플레이어입니다.");
						}
					}
				}
			} else {
				Messager.sendErrorMessage(p, ChatColor.translateAlternateColorCodes('&', "&c능력자 전쟁이 진행되고 있지 않습니다."));
			}
		} else if(args[0].equalsIgnoreCase("spec")) {
			SpectatorGUI gui = new SpectatorGUI(p, AbilityWar.getPlugin());
			gui.openSpectateGUI(1);
		} else if(args[0].equalsIgnoreCase("ablist")) {
			if(AbilityWarThread.isGameTaskRunning()) {
				ArrayList<String> msg = new ArrayList<String>();
				msg.add(ChatColor.translateAlternateColorCodes('&', "&2===== &a능력자 목록 &2====="));

				Integer Count = 0;
				for(Participant participant : AbilityWarThread.getGame().getParticipants()) {
					if(participant.hasAbility()) {
						Count++;
						AbilityBase Ability = participant.getAbility();
						String name = Ability.getName();
						if(name != null) {
							msg.add(ChatColor.translateAlternateColorCodes('&', "&e" + Count + ". &f" + participant.getPlayer().getName() + " &7: &c" + name));
						}
					}
				}
				
				if(Count.equals(0)) {
					msg.add(ChatColor.translateAlternateColorCodes('&', "&f능력자가 발견되지 않았습니다."));
				}
				
				msg.add(ChatColor.translateAlternateColorCodes('&', "&2========================"));
				
				Messager.broadcastMessage(ChatColor.translateAlternateColorCodes('&', "&f" + p.getName() + "&a님이 플레이어들의 능력을 확인하였습니다."));
				
				for(String m : msg) {
					Messager.sendMessage(p, m);
				}
			} else {
				Messager.sendErrorMessage(p, ChatColor.translateAlternateColorCodes('&', "&c능력자 전쟁이 진행되고 있지 않습니다."));
			}
		} else if(args[0].equalsIgnoreCase("blacklist")) {
			BlackListGUI gui = new BlackListGUI(p, AbilityWar.getPlugin());
			gui.openBlackListGUI(1);
		} else if(args[0].equalsIgnoreCase("resetcool")) {
			if(AbilityWarThread.isGameTaskRunning()) {
				CooldownTimer.ResetCool();
				Messager.broadcastMessage(ChatColor.translateAlternateColorCodes('&', "&f" + p.getName() + "&a님이 플레이어들의 능력 쿨타임을 초기화하였습니다."));
			} else {
				Messager.sendErrorMessage(p, ChatColor.translateAlternateColorCodes('&', "&c능력자 전쟁이 진행되고 있지 않습니다."));
			}
		} else if(args[0].equalsIgnoreCase("resetduration")) {
			if(AbilityWarThread.isGameTaskRunning()) {
				DurationTimer.ResetDuration();
				Messager.broadcastMessage(ChatColor.translateAlternateColorCodes('&', "&f" + p.getName() + "&a님이 플레이어들의 능력 지속시간을 초기화하였습니다."));
			} else {
				Messager.sendErrorMessage(p, ChatColor.translateAlternateColorCodes('&', "&c능력자 전쟁이 진행되고 있지 않습니다."));
			}
		} else if(args[0].equalsIgnoreCase("kit")) {
			if(AbilityWarThread.isGameTaskRunning()) {
				if(args.length < 2) {
					Messager.sendErrorMessage(p, ChatColor.translateAlternateColorCodes('&', "사용법 &7: &f/" + label + " util kit <대상>"));
				} else {
					if(args[1].equalsIgnoreCase("@a")) {
						Messager.broadcastMessage(ChatColor.translateAlternateColorCodes('&', "&f" + p.getName() + "&a님이 &f전체 유저&a에게 기본템을 다시 지급하였습니다."));
						AbilityWarThread.getGame().GiveDefaultKit();
					} else {
						if(Bukkit.getPlayerExact(args[1]) != null) {
							Player target = Bukkit.getPlayerExact(args[1]);
							if(AbilityWarThread.getGame().isParticipating(target)) {
								AbilityWarThread.getGame().GiveDefaultKit(target);
								SoundLib.ENTITY_EXPERIENCE_ORB_PICKUP.playSound(target);
								Messager.broadcastMessage(ChatColor.translateAlternateColorCodes('&', "&f" + p.getName() + "&a님이 &f" + target.getName() + "&a님에게 기본템을 다시 지급하였습니다."));
							} else {
								Messager.sendErrorMessage(p, target.getName() + "님은 탈락했거나 게임에 참여하지 않았습니다.");
							}
						} else {
							Messager.sendErrorMessage(p, args[1] + "은(는) 존재하지 않는 플레이어입니다.");
						}
					}
				}
			} else {
				Messager.sendErrorMessage(p, ChatColor.translateAlternateColorCodes('&', "&c능력자 전쟁이 진행되고 있지 않습니다."));
			}
		} else if(args[0].equalsIgnoreCase("inv")) {
			if(AbilityWarThread.isGameTaskRunning()) {
				if(AbilityWarThread.getGame().isGameStarted()) {
					Invincibility invincibility = AbilityWarThread.getGame().getInvincibility();
					if(invincibility.isInvincible()) {
						invincibility.Stop();
						Messager.broadcastMessage(ChatColor.translateAlternateColorCodes('&', "&f" + p.getName() + "&a님이 무적 상태를 &f비활성화&a하셨습니다."));
					} else {
						invincibility.Start(true);
						Messager.broadcastMessage(ChatColor.translateAlternateColorCodes('&', "&f" + p.getName() + "&a님이 무적 상태를 &f활성화&a하셨습니다."));
					}
				} else {
					Messager.sendErrorMessage(p, ChatColor.translateAlternateColorCodes('&', "&c능력자 전쟁이 시작되지 않았습니다."));
				}
			} else {
				Messager.sendErrorMessage(p, ChatColor.translateAlternateColorCodes('&', "&c능력자 전쟁이 진행되고 있지 않습니다."));
			}
		} else {
			if(NumberUtil.isInt(args[0])) {
				sendHelpUtilCommand(p, label, Integer.valueOf(args[0]));
			} else {
				Messager.sendErrorMessage(p, "존재하지 않는 유틸입니다.");
			}
		}
	}
	
	private void sendHelpCommand(CommandSender sender, String label, Integer Page) {
		int AllPage = 3;
		
		switch(Page) {
			case 1:
				Messager.sendStringList(sender, Messager.getStringList(
						Messager.formatTitle(ChatColor.GOLD, ChatColor.YELLOW, "능력자 전쟁"),
						ChatColor.translateAlternateColorCodes('&', "&b/" + label + " help <페이지> &7로 더 많은 명령어를 확인하세요! ( &b" + Page + " 페이지 &7/ &b" + AllPage + " 페이지 &7)"),
						Messager.formatCommand(label, "start", "능력자 전쟁을 시작시킵니다.", true),
						Messager.formatCommand(label, "stop", "능력자 전쟁을 중지시킵니다.", true),
						Messager.formatCommand(label, "check", "자신의 능력을 확인합니다.", false),
						Messager.formatCommand(label, "yes", "자신의 능력을 확정합니다.", false),
						Messager.formatCommand(label, "no", "자신의 능력을 변경합니다.", false)));
				break;
			case 2:
				Messager.sendStringList(sender, Messager.getStringList(
						Messager.formatTitle(ChatColor.GOLD, ChatColor.YELLOW, "능력자 전쟁"),
						ChatColor.translateAlternateColorCodes('&', "&b/" + label + " help <페이지> &7로 더 많은 명령어를 확인하세요! ( &b" + Page + " 페이지 &7/ &b" + AllPage + " 페이지 &7)"),
						Messager.formatCommand(label, "skip", "모든 유저의 능력을 강제로 확정합니다.", true),
						Messager.formatCommand(label, "reload", "능력자 전쟁 콘피그를 리로드합니다.", true),
						Messager.formatCommand(label, "config", "능력자 전쟁 콘피그 명령어를 확인합니다.", true),
						Messager.formatCommand(label, "util", "능력자 전쟁 유틸 명령어를 확인합니다.", true),
						Messager.formatCommand(label, "script", "능력자 전쟁 스크립트 편집을 시작합니다.", true)));
				break;
			case 3:
				Messager.sendStringList(sender, Messager.getStringList(
						Messager.formatTitle(ChatColor.GOLD, ChatColor.YELLOW, "능력자 전쟁"),
						ChatColor.translateAlternateColorCodes('&', "&b/" + label + " help <페이지> &7로 더 많은 명령어를 확인하세요! ( &b" + Page + " 페이지 &7/ &b" + AllPage + " 페이지 &7)"),
						Messager.formatCommand(label, "gamemode", "능력자 전쟁 게임 모드를 설정합니다.", true),
						Messager.formatCommand(label, "update", "최신버전으로 업데이트를 시도합니다.", true),
						Messager.formatCommand(label, "specialthanks", "능력자 전쟁 플러그인에 기여한 사람들을 확인합니다.", false)));
				break;
			default:
				Messager.sendErrorMessage(sender, "존재하지 않는 페이지입니다.");
				break;
		}
	}

	private void sendHelpConfigCommand(CommandSender sender, String label, Integer Page) {
		int AllPage = 1;
		
		switch(Page) {
			case 1:
				Messager.sendStringList(sender, Messager.getStringList(
						Messager.formatTitle(ChatColor.GOLD, ChatColor.YELLOW, "능력자 전쟁 콘피그"),
						ChatColor.translateAlternateColorCodes('&', "&b/" + label + " config <페이지> &7로 더 많은 명령어를 확인하세요! ( &b" + Page + " 페이지 &7/ &b" + AllPage + " 페이지 &7)"),
						Messager.formatCommand(label + " config", "kit", "능력자 전쟁 기본템을 설정합니다.", true),
						Messager.formatCommand(label + " config", "spawn", "능력자 전쟁 스폰을 설정합니다.", true),
						Messager.formatCommand(label + " config", "inv", "초반 무적을 설정합니다.", true),
						Messager.formatCommand(label + " config", "game", "게임의 전반적인 부분들을 설정합니다.", true),
						Messager.formatCommand(label + " config", "death", "플레이어 사망에 관련된 콘피그를 설정합니다.", true)));
				break;
			default:
				Messager.sendErrorMessage(sender, "존재하지 않는 페이지입니다.");
				break;
		}
	}

	private void sendHelpUtilCommand(CommandSender sender, String label, Integer Page) {
		int AllPage = 2;
		
		switch(Page) {
			case 1:
				Messager.sendStringList(sender, Messager.getStringList(
						Messager.formatTitle(ChatColor.GOLD, ChatColor.YELLOW, "능력자 전쟁 유틸"),
						ChatColor.translateAlternateColorCodes('&', "&b/" + label + " util <페이지> &7로 더 많은 명령어를 확인하세요! ( &b" + Page + " 페이지 &7/ &b" + AllPage + " 페이지 &7)"),
						Messager.formatCommand(label + " util", "abi <대상/@a>", "대상에게 능력을 임의로 부여합니다.", true),
						Messager.formatCommand(label + " util", "inv", "무적 상태를 토글합니다.", true),
						Messager.formatCommand(label + " util", "spec", "관전자 설정 GUI를 띄웁니다.", true),
						Messager.formatCommand(label + " util", "ablist", "능력자 목록을 확인합니다.", true),
						Messager.formatCommand(label + " util", "blacklist", "능력 블랙리스트 설정 GUI를 띄웁니다.", true)));
				break;
			case 2:
				Messager.sendStringList(sender, Messager.getStringList(
						Messager.formatTitle(ChatColor.GOLD, ChatColor.YELLOW, "능력자 전쟁 유틸"),
						ChatColor.translateAlternateColorCodes('&', "&b/" + label + " util <페이지> &7로 더 많은 명령어를 확인하세요! ( &b" + Page + " 페이지 &7/ &b" + AllPage + " 페이지 &7)"),
						Messager.formatCommand(label + " util", "resetcool", "플레이어들의 능력 쿨타임을 초기화시킵니다.", true),
						Messager.formatCommand(label + " util", "resetduration", "플레이어들의 능력 지속시간을 초기화시킵니다.", true),
						Messager.formatCommand(label + " util", "kit <대상/@a>", "대상에게 기본템을 다시 지급합니다.", true)));
				break;
			default:
				Messager.sendErrorMessage(sender, "존재하지 않는 페이지입니다.");
				break;
		}
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
		return parseTabComplete(sender, label, args);
	}
	
	private List<String> parseTabComplete(CommandSender sender, String label, String[] args) {
		if(label.equalsIgnoreCase("abilitywar") || label.equalsIgnoreCase("ability")
		|| label.equalsIgnoreCase("aw") || label.equalsIgnoreCase("va")
		|| label.equalsIgnoreCase("능력자")) {
			switch(args.length) {
				case 1:
					ArrayList<String> Complete = Messager.getStringList(
							"start", "stop", "check", "yes", "no",
							"skip", "reload", "config", "util", "script", "gamemode", "update", "specialthanks");
					
					if(args[0].isEmpty()) {
						return Complete;
					} else {
						return Complete.stream().filter(s -> s.toLowerCase().startsWith(args[0].toLowerCase())).collect(Collectors.toList());
					}
				case 2:
					if(args[0].equalsIgnoreCase("config")) {
						ArrayList<String> Config = Messager.getStringList(
								"kit", "spawn", "inv", "game", "death");
						if(args[1].isEmpty()) {
							return Config;
						} else {
							return Config.stream().filter(s -> s.toLowerCase().startsWith(args[1].toLowerCase())).collect(Collectors.toList());
						}
					} else if(args[0].equalsIgnoreCase("util")) {
						ArrayList<String> Util = Messager.getStringList(
								"abi", "spec", "ablist", "blacklist", "resetcool", "resetduration", "kit", "inv");
						if(args[1].isEmpty()) {
							return Util;
						} else {
							return Util.stream().filter(s -> s.toLowerCase().startsWith(args[1].toLowerCase())).collect(Collectors.toList());
						}
					} else if(args[0].equalsIgnoreCase("script")) {
						List<String> list = Script.getRegisteredScripts();
						
						if(args[1].isEmpty()) {
							return list;
						} else {
							return list.stream().filter(s -> s.toLowerCase().startsWith(args[1].toLowerCase())).collect(Collectors.toList());
						}
					}
				case 3:
					if(args[0].equalsIgnoreCase("util")) {
						if(args[1].equalsIgnoreCase("abi") || args[1].equalsIgnoreCase("kit")) {
							ArrayList<String> Players = new ArrayList<String>();
							for(Player p : Bukkit.getOnlinePlayers()) Players.add(p.getName());
							Players.add("@a");
							Players.sort(new Comparator<String>() {
								
								public int compare(String obj1, String obj2) {
									return obj1.compareToIgnoreCase(obj2);
								}
								
							});
							
	
							if(args[2].isEmpty()) {
								return Players;
							} else {
								return Players.stream().filter(s -> s.toLowerCase().startsWith(args[2].toLowerCase())).collect(Collectors.toList());
							}
						}
					}
					
			}
		}

		return Messager.getStringList();
	}
	
}
