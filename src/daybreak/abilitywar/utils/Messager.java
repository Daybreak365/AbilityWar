package daybreak.abilitywar.utils;

import java.io.IOException;
import java.util.ArrayList;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

import daybreak.abilitywar.ability.AbilityBase;
import daybreak.abilitywar.ability.AbilityManifest.Rank;
import daybreak.abilitywar.ability.AbilityManifest.Species;
import daybreak.abilitywar.utils.installer.Installer.UpdateObject;

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
		String Base = "_________________________________________________________";
		int Pivot = Base.length() / 2;
		String center = ChatColor.translateAlternateColorCodes('&', "[ " + titleColor + title + bracketColor + " ]&m&l");
		String result = ChatColor.translateAlternateColorCodes('&', bracketColor + "&m&l" + Base.substring(0, Math.max(0, (Pivot - center.length() / 2))) + "&r" + bracketColor);
		result += center + Base.substring(Pivot + center.length() / 2);
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
		String Base = "________________________________";
		int Pivot = Base.length() / 2;
		String center = ChatColor.translateAlternateColorCodes('&', "[ " + titleColor + title + bracketColor + " ]&m&l");
		String result = ChatColor.translateAlternateColorCodes('&', bracketColor + "&m&l" + Base.substring(0, Math.max(0, (Pivot - center.length() / 2))) + "&r" + bracketColor);
		result += center + Base.substring(Pivot + center.length() / 2);
		return result;
	}

	/**
	 * 설치 설명을 구성합니다.
	 * @throws IOException 
	 */
	public static String[] formatInstall(UpdateObject update) throws IOException {
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
	 * 업데이트 공지를 구성합니다.
	 * @throws IOException 
	 */
	public static String[] formatUpdateNotice(UpdateObject update) throws IOException {
		return new String[] {
				Messager.formatTitle(ChatColor.DARK_AQUA, ChatColor.AQUA, "업데이트"),
				ChatColor.translateAlternateColorCodes('&', "&f적용 가능한 &3업데이트&f가 있습니다: &b" + update.getTag() + " &f업데이트 &f(&7v" + update.getVersion() + "&f) " + "(&7" + (update.getFileSize() / 1024) + "KB&f)"),
				ChatColor.translateAlternateColorCodes('&', "&3업데이트&f를 진행하려면 &e/aw update &f명령어를 사용하세요."),
				ChatColor.translateAlternateColorCodes('&', "&3---------------------------------------------------------")
		};
	}

	/**
	 * 능력 설명을 구성합니다.
	 */
	public static String[] formatAbilityInfo(AbilityBase Ability) {
		ArrayList<String> info = new ArrayList<String>();
		info.add(formatShortTitle(ChatColor.GREEN, ChatColor.YELLOW, "능력 정보"));
		
		String name = Ability.getName();
		Rank rank = Ability.getRank();
		Species species = Ability.getSpecies();
		
		if(name != null && rank != null) {
			String restricted = Ability.isRestricted() ? "&f[&7능력 비활성화됨&f]" : "&f[&a능력 활성화됨&f]";
			info.add(ChatColor.translateAlternateColorCodes('&', "&b" + name + " " + restricted + " " + rank.getRankName() + " " + species.getName()));
			for(String s : Ability.getExplain()) {
				info.add(ChatColor.translateAlternateColorCodes('&', "&f" + s));
			}
		} else {
			info.add(ChatColor.translateAlternateColorCodes('&', "&c능력 설명을 불러오는 도중 오류가 발생하였습니다."));
			info.add(ChatColor.translateAlternateColorCodes('&', "&cAbility Class : " + Ability.getClass().getName()));
		}
		
		info.add(ChatColor.translateAlternateColorCodes('&', "&a------------------------------"));
		
		return info.toArray(new String[info.size()]);
	}

	/**
	 * 쿨타임 설명을 구성합니다.
	 */
	public static String formatCooldown(Integer Cool) {
		return ChatColor.translateAlternateColorCodes('&', "&c쿨타임 &7: &f" + Cool + "초");
	}

	/**
	 * 명령어 도움말을 구성합니다.
	 */
	public static String formatCommand(String label, String command, String help, boolean admin) {
		if(admin) {
			return ChatColor.translateAlternateColorCodes('&', "&c관리자: &6/" + label + " &e" + command + " &7: &f" + help);
		} else {
			return ChatColor.translateAlternateColorCodes('&', "&a유  저: &6/" + label + " &e" + command + " &7: &f" + help);
		}
	}

	public static ArrayList<String> asList(String... strs) {
		return new ArrayList<String>() {
			
			private static final long serialVersionUID = 1L;

			{
				for (String s : strs) {
					add(s);
				}
			}
			
		};
	}

	/**
	 * 채팅창을 청소합니다.
	 */
	public static void clearChat() {
		for(int c = 0; c < 100; c++) {
			for(Player p : Bukkit.getOnlinePlayers()) {
				p.sendMessage("");
			}
		}
	}

	/**
	 * 플레이어의 채팅창을 청소합니다.
	 */
	public static void clearChat(CommandSender cs) {
		for(int i = 0; i < 100; i++) {
			cs.sendMessage("");
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

	/**
	 * String 배열에서 인수를 삭제합니다.
	 */
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

	public void sendConsoleMessage(String str) {
		console.sendMessage(prefix + str);
	}

	public void sendConsoleMessage(String[] strs) {
		for (int c = 0; c < strs.length; c++) {
			strs[c] = prefix + strs[c];
		}
		console.sendMessage(strs);
	}

	public void broadcastMessage(String str) {
		Bukkit.broadcastMessage(prefix + str);
	}

	public void sendMessage(CommandSender cs, String str) {
		cs.sendMessage(prefix + str);
	}

	public void sendMessages(CommandSender cs, String... strs) {
		for (int c = 0; c < strs.length; c++) {
			strs[c] = prefix + strs[c];
		}
		cs.sendMessage(strs);
	}

}
