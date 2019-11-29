package daybreak.abilitywar.utils;

import daybreak.abilitywar.ability.AbilityBase;
import daybreak.abilitywar.game.games.mode.AbstractGame;
import daybreak.abilitywar.game.games.mode.decorator.TeamGame;
import daybreak.abilitywar.utils.installer.Installer.UpdateObject;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.StringJoiner;

public class Messager {

	public static final String defaultPrefix = ChatColor.translateAlternateColorCodes('&', "&2《&aAbilityWar&2》&f");
	private static final ConsoleCommandSender console = Bukkit.getConsoleSender();

	/**
	 * 제목 포맷을 만듭니다.
	 *
	 * @param bracketColor 괄호 색
	 * @param titleColor   제목 색
	 * @param title        제목
	 * @return 제목 포맷
	 */
	public static String formatTitle(ChatColor bracketColor, ChatColor titleColor, String title) {
		String base = "_________________________________________________________";
		int pivot = base.length() / 2;
		String center = ChatColor.translateAlternateColorCodes('&', "[ " + titleColor + title + bracketColor + " ]&m&l");
		String result = ChatColor.translateAlternateColorCodes('&', bracketColor + "&m&l" + base.substring(0, Math.max(0, (pivot - center.length() / 2))) + "&r" + bracketColor);
		result += center + base.substring(pivot + center.length() / 2);
		return result;
	}

	/**
	 * 제목 포맷을 만듭니다.
	 *
	 * @param bracketColor 괄호 색
	 * @param titleColor   제목 색
	 * @param title        제목
	 * @return 제목 포맷
	 */
	public static String formatShortTitle(ChatColor bracketColor, ChatColor titleColor, String title) {
		String base = "________________________________";
		int pivot = base.length() / 2;
		String center = ChatColor.translateAlternateColorCodes('&', "[ " + titleColor + title + bracketColor + " ]&m&l");
		String result = ChatColor.translateAlternateColorCodes('&', bracketColor + "&m&l" + base.substring(0, Math.max(0, (pivot - center.length() / 2))) + "&r" + bracketColor);
		result += center + base.substring(pivot + center.length() / 2);
		return result;
	}

	/**
	 * 설치 설명을 구성합니다.
	 */
	public static String[] formatInstall(UpdateObject update) {
		ArrayList<String> info = new ArrayList<String>();
		info.add(Messager.formatTitle(ChatColor.DARK_GREEN, ChatColor.GREEN, "업데이트"));
		info.add(ChatColor.translateAlternateColorCodes('&', "&b" + update.getTag() + " &f업데이트 &f(&7v" + update.getVersion() + "&f) " + "(&7" + (update.getFileSize() / 1024) + "KB&f)"));
		for (String msg : update.getUpdates()) {
			info.add(msg);
		}
		info.add(ChatColor.translateAlternateColorCodes('&', "&2-----------------------------------------------------"));

		return info.toArray(new String[info.size()]);
	}

	/**
	 * 능력 설명을 구성합니다.
	 */
	public static String[] formatAbilityInfo(AbilityBase ability) {
		ArrayList<String> list = Messager.asList(
				Messager.formatShortTitle(ChatColor.GREEN, ChatColor.YELLOW, "능력 정보"),
				ChatColor.translateAlternateColorCodes('&', "&b" + ability.getName() + " " + (ability.isRestricted() ? "&f[&7능력 비활성화됨&f]" : "&f[&a능력 활성화됨&f]") + " " + ability.getRank().getRankName() + " " + ability.getSpecies().getSpeciesName()));
		list.addAll(ability.getExplain());
		list.add(ChatColor.translateAlternateColorCodes('&', "&a--------------------------------"));
		return list.toArray(new String[0]);
	}

	/**
	 * 팀 설명을 구성합니다.
	 */
	public static String[] formatTeamInfo(TeamGame teamGame, TeamGame.Team team) {
		ArrayList<String> info = new ArrayList<>();
		info.add(formatShortTitle(ChatColor.DARK_PURPLE, ChatColor.WHITE, "팀 정보"));
		info.add(ChatColor.translateAlternateColorCodes('&', "&5팀 이름&f: &r" + team.getDisplayName() + " &r(" + team.getName() + ")"));
		StringJoiner joiner = new StringJoiner(ChatColor.WHITE + ", ", ChatColor.DARK_PURPLE + "팀원" + ChatColor.WHITE + ": " + ChatColor.RESET, ChatColor.WHITE + ".");
		for (AbstractGame.Participant participant : teamGame.getParticipants(team)) {
			joiner.add(ChatColor.YELLOW + participant.getPlayer().getName());
		}
		info.add(joiner.toString());
		info.add(ChatColor.translateAlternateColorCodes('&', "&5-------------------------------"));
		return info.toArray(new String[0]);
	}

	/**
	 * 쿨타임 설명을 구성합니다.
	 */
	public static String formatCooldown(int cool) {
		return ChatColor.translateAlternateColorCodes('&', "&c쿨타임 &7: &f" + cool + "초");
	}

	/**
	 * 명령어 도움말을 구성합니다.
	 */
	public static String formatCommand(String label, String command, String help, boolean admin) {
		if (admin) {
			return ChatColor.translateAlternateColorCodes('&', "&c관리자: &6/" + label + " &e" + command + " &7: &f" + help);
		} else {
			return ChatColor.translateAlternateColorCodes('&', "&a유  저: &6/" + label + " &e" + command + " &7: &f" + help);
		}
	}

	public static ArrayList<String> asList(String... strings) {
		return new ArrayList<>(Arrays.asList(strings));
	}

	/**
	 * 채팅창을 청소합니다.
	 */
	public static void clearChat() {
		for (int c = 0; c < 100; c++) {
			for (Player p : Bukkit.getOnlinePlayers()) {
				p.sendMessage("");
			}
		}
	}

	public static String[] removeArgs(String[] args, int startIndex) {
		if (args.length == 0)
			return args;
		else if (args.length < startIndex)
			return new String[0];
		else {
			String[] newArgs = new String[args.length - startIndex];
			System.arraycopy(args, startIndex, newArgs, 0, args.length - startIndex);
			return newArgs;
		}
	}

	public static void sendConsoleErrorMessage(String str) {
		console.sendMessage(ChatColor.WHITE + "[" + ChatColor.RED + "!" + ChatColor.WHITE + "] " + ChatColor.RED + str);
	}

	public static void sendConsoleErrorMessage(String... strs) {
		for (int c = 0; c < strs.length; c++) {
			strs[c] = ChatColor.WHITE + "[" + ChatColor.RED + "!" + ChatColor.WHITE + "] " + ChatColor.RED + strs[c];
		}
		console.sendMessage(strs);
	}

	public static void sendErrorMessage(CommandSender cs, String str) {
		cs.sendMessage(ChatColor.WHITE + "[" + ChatColor.RED + "!" + ChatColor.WHITE + "] " + ChatColor.RED + str);
	}

	public static void broadcastErrorMessage(String str) {
		Bukkit.broadcastMessage(ChatColor.WHITE + "[" + ChatColor.RED + "!" + ChatColor.WHITE + "] " + ChatColor.RED + str);

	}

	private final String prefix;

	public Messager(String prefix) {
		if (prefix != null) {
			this.prefix = prefix;
		} else {
			this.prefix = "";
		}
	}

	public Messager() {
		this.prefix = defaultPrefix;
	}

	public void sendConsoleMessage(String string) {
		console.sendMessage(prefix + string);
	}

	public void sendConsoleMessage(String[] strings) {
		for (String string : strings) {
			console.sendMessage(prefix + string);
		}
	}

	public void broadcastMessage(String string) {
		Bukkit.broadcastMessage(prefix + string);
	}

	public void sendMessage(CommandSender cs, String string) {
		cs.sendMessage(prefix + string);
	}

	public void sendMessages(CommandSender cs, String... strings) {
		for (String string : strings) {
			cs.sendMessage(prefix + string);
		}
	}

}
