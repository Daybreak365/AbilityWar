package Marlang.AbilityWar.GameManager;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import Marlang.AbilityWar.AbilityWar;
import Marlang.AbilityWar.Ability.AbilityBase;
import Marlang.AbilityWar.Config.SettingWizard;
import Marlang.AbilityWar.Utils.AbilityWarThread;
import Marlang.AbilityWar.Utils.NumberUtil;
import Marlang.AbilityWar.Utils.Messager;

public class MainCommand implements CommandExecutor {
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		return parseCommand(sender, label, args);
	}
	
	public boolean parseCommand(CommandSender sender, String label, String[] split) {
		if(split.length == 0) {
			sendHelpCommand(sender, label, 1);
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
						AbilityWarThread.toggleGameTask(true);
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
						AbilityWarThread.toggleGameTask(false);
						Messager.broadcastMessage(ChatColor.translateAlternateColorCodes('&', "&f관리자 &e" + sender.getName() + "&f님이 게임을 중지시켰습니다."));
						AbilityWarThread.setGame(null);
						Messager.broadcastMessage(ChatColor.translateAlternateColorCodes('&', "&7게임이 초기화되었습니다."));
					} else {
						Messager.sendErrorMessage(sender, ChatColor.translateAlternateColorCodes('&', "&c능력자 전쟁이 진행되고 있지 않습니다."));
					}
				} else {
					Messager.sendErrorMessage(sender, ChatColor.translateAlternateColorCodes('&', "&c이 명령어를 사용하려면 OP 권한이 있어야 합니다."));
				}
			} else if(split[0].equalsIgnoreCase("reload")) {
				if(sender.isOp()) {
					AbilityWar.getSetting().Reload();
					Messager.sendMessage(sender, ChatColor.translateAlternateColorCodes('&', "&2능력자 전쟁 콘피그가 리로드되었습니다."));
				} else {
					Messager.sendErrorMessage(sender, ChatColor.translateAlternateColorCodes('&', "&c이 명령어를 사용하려면 OP 권한이 있어야 합니다."));
				}
			} else if(split[0].equalsIgnoreCase("config")) {
				if(sender instanceof Player) {
					Player p = (Player) sender;
					if(p.isOp()) {
						
						if(split.length > 1) {
							if(split[1].equalsIgnoreCase("kit")) {
								SettingWizard.openKitGUI(p);
							} else {
								if(NumberUtil.isInt(split[1])) {
									sendHelpConfigCommand(p, label, Integer.valueOf(split[1]));
								} else {
									Messager.sendErrorMessage(p, "존재하지 않는 콘피그입니다.");
								}
							}
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
						if(AbilityWarThread.getGame().getAbilities().containsKey(p)) {
							AbilityBase Ability = AbilityWarThread.getGame().getAbilities().get(p);
							Messager.sendStringList(p, Messager.formatAbility(Ability));
						} else {
							Messager.sendErrorMessage(sender, ChatColor.translateAlternateColorCodes('&', "&c당신에게 능력이 할당되지 않았습니다."));
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
						if(AbilityWarThread.getGame().getAbilities().containsKey(p)) {
							if(AbilityWarThread.getAbilitySelect() != null) {
								if(!AbilityWarThread.getAbilitySelect().getAbilitySelect(p)) {
									AbilityWarThread.getAbilitySelect().decideAbility(p, true);
								} else {
									p.sendMessage(ChatColor.translateAlternateColorCodes('&', "&c이미 능력 선택을 마치셨습니다."));
								}
							} else {
								Messager.sendErrorMessage(sender, ChatColor.translateAlternateColorCodes('&', "&c능력을 선택하는 중이 아닙니다."));
							}
						} else {
							Messager.sendErrorMessage(sender, ChatColor.translateAlternateColorCodes('&', "&c당신에게 능력이 할당되지 않았습니다."));
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
						if(AbilityWarThread.getGame().getAbilities().containsKey(p)) {
							if(AbilityWarThread.getAbilitySelect() != null) {
								if(!AbilityWarThread.getAbilitySelect().getAbilitySelect(p)) {
									AbilityWarThread.getAbilitySelect().changeAbility(p);
								} else {
									p.sendMessage(ChatColor.translateAlternateColorCodes('&', "&c이미 능력 선택을 마치셨습니다."));
								}
							} else {
								Messager.sendErrorMessage(sender, ChatColor.translateAlternateColorCodes('&', "&c능력을 선택하는 중이 아닙니다."));
							}
						} else {
							Messager.sendErrorMessage(sender, ChatColor.translateAlternateColorCodes('&', "&c당신에게 능력이 할당되지 않았습니다."));
						}
					} else {
						Messager.sendErrorMessage(sender, ChatColor.translateAlternateColorCodes('&', "&c능력자 전쟁이 진행되고 있지 않습니다."));
					}
				} else {
					Messager.sendErrorMessage(sender, ChatColor.translateAlternateColorCodes('&', "&c콘솔에서 사용할 수 없는 명령어입니다!"));
				}
			}
		}
		
		return true;
	}

	public void sendHelpCommand(CommandSender sender, String label, Integer Page) {
		int AllPage = 2;
		
		switch(Page) {
			case 1:
				Messager.sendStringList(sender, Messager.getArrayList(
						Messager.formatTitle(ChatColor.GOLD, ChatColor.YELLOW, "능력자 전쟁"),
						ChatColor.translateAlternateColorCodes('&', "&b/" + label + " help <페이지> &7로 더 많은 명령어를 확인하세요! ( &b" + Page + " 페이지 &7/ &b" + AllPage + " 페이지 &7)"),
						Messager.formatCommand(label, "start", "능력자 전쟁을 시작시킵니다.", true),
						Messager.formatCommand(label, "stop", "능력자 전쟁을 중지시킵니다.", true),
						Messager.formatCommand(label, "check", "자신의 능력을 확인합니다.", false),
						Messager.formatCommand(label, "yes", "자신의 능력을 확정합니다.", false),
						Messager.formatCommand(label, "no", "자신의 능력을 변경합니다.", false)));
				break;
			case 2:
				Messager.sendStringList(sender, Messager.getArrayList(
						Messager.formatTitle(ChatColor.GOLD, ChatColor.YELLOW, "능력자 전쟁"),
						ChatColor.translateAlternateColorCodes('&', "&b/" + label + " help <페이지> &7로 더 많은 명령어를 확인하세요! ( &b" + Page + " 페이지 &7/ &b" + AllPage + " 페이지 &7)"),
						Messager.formatCommand(label, "reload", "능력자 전쟁을 리로드합니다.", true),
						Messager.formatCommand(label, "config", "능력자 전쟁 콘피그 명령어를 확인합니다.", true)));
				break;
			default:
				Messager.sendErrorMessage(sender, "존재하지 않는 페이지입니다.");
				break;
		}
	}

	public void sendHelpConfigCommand(CommandSender sender, String label, Integer Page) {
		int AllPage = 1;
		
		switch(Page) {
			case 1:
				Messager.sendStringList(sender, Messager.getArrayList(
						Messager.formatTitle(ChatColor.GOLD, ChatColor.YELLOW, "능력자 전쟁 콘피그"),
						ChatColor.translateAlternateColorCodes('&', "&b/" + label + " config <페이지> &7로 더 많은 명령어를 확인하세요! ( &b" + Page + " 페이지 &7/ &b" + AllPage + " 페이지 &7)"),
						Messager.formatCommand(label + " config", "kit", "능력자 전쟁 기본템을 설정합니다.", true)));
				break;
			default:
				Messager.sendErrorMessage(sender, "존재하지 않는 페이지입니다.");
				break;
		}
	}
	
}
