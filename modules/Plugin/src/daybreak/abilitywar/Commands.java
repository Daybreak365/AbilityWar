package daybreak.abilitywar;

import daybreak.abilitywar.Command.Condition;
import daybreak.abilitywar.game.GameManager;
import daybreak.abilitywar.utils.base.Formatter;
import daybreak.abilitywar.utils.base.Messager;
import daybreak.abilitywar.utils.base.logging.Logger;
import daybreak.abilitywar.utils.base.math.NumberUtil;
import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

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
						sendHelpCommand(sender, command, Integer.parseInt(args[0]));
					} else {
						Messager.sendErrorMessage(sender, "존재하지 않는 페이지입니다.");
					}
				} else {
					sendHelpCommand(sender, command, 1);
				}
				return true;
			}

			private void sendHelpCommand(CommandSender sender, String command, int page) {
				int allPage = 2;
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
	}

	@Override
	public boolean onCommand(CommandSender sender, org.bukkit.command.Command cmd, String label, String[] args) {
		mainCommand.handle(sender, label, args);
		return true;
	}

	@Override
	public List<String> onTabComplete(CommandSender commandSender, org.bukkit.command.Command command, String s, String[] strings) {
		return null;
	}

}
