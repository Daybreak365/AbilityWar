package DayBreak.AbilityWar.Utils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import DayBreak.AbilityWar.Ability.AbilityBase;
import DayBreak.AbilityWar.Ability.AbilityManifest.Rank;
import DayBreak.AbilityWar.Ability.AbilityManifest.Species;
import DayBreak.AbilityWar.Utils.AutoUpdate.AutoUpdate.UpdateObject;

/**
 * 메시지 관리 클래스
 * @author DayBreak 새벽
 */
public class Messager {
	
	private Messager() {}
	
	private static final String Prefix = ChatColor.translateAlternateColorCodes('&', "&2《&aAbilityWar&2》&f");
	
	public static String getPrefix() {
		return Prefix;
	}
	
	/**
	 * 콘솔에 메시지를 전송합니다.
	 */
	public static void sendMessage(String msg) {
		Bukkit.getConsoleSender().sendMessage(Prefix + msg);
	}
	
	/**
	 * 콘솔에 메시지를 전송합니다.
	 */
	public static void sendMessage(List<String> messages) {
		for(String msg : messages) {
			sendMessage(msg);
		}
	}

	/**
	 * 플레이어에게 메시지를 전송합니다.
	 */
	public static void sendMessage(Player p, String msg) {
		p.sendMessage(msg);
	}
	
	/**
	 * 명령어를 실행한 객체에게 메시지를 전송합니다.
	 */
	public static void sendMessage(CommandSender sender, String msg) {
		sender.sendMessage(msg);
	}
	
	/**
	 * 콘솔에 디버그 메시지를 전송합니다.
	 */
	public static void sendDebugMessage(String msg) {
		Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', "&8[&7AbilityWar&8] &f" + msg));
	}
	
	/**
	 * 콘솔에 오류 메시지를 전송합니다.
	 */
	public static void sendErrorMessage(String msg) {
		System.out.println(ChatColor.translateAlternateColorCodes('&', "&f&l[&c&l!&f&l] &r&c" + msg));
	}

	/**
	 * 콘솔에 오류 메시지를 전송합니다.
	 */
	public static void sendErrorMessage() {
		System.out.println(ChatColor.translateAlternateColorCodes('&', "&f&l[&c&l!&f&l] &r&c오류가 발생하였습니다."));
	}
	
	/**
	 * 플레이어에게 오류 메시지를 전송합니다.
	 */
	public static void sendErrorMessage(Player p, String msg) {
		p.sendMessage(ChatColor.translateAlternateColorCodes('&', "&f&l[&c&l!&f&l] &f") + msg);
	}
	
	/**
	 * 명령어를 실행한 객체에게 오류 메시지를 전송합니다.
	 */
	public static void sendErrorMessage(CommandSender sender, String msg) {
		sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&f&l[&c&l!&f&l] &f") + msg);
	}
	
	/**
	 * 오류 메시지를 공지합니다.
	 */
	public static void broadcastErrorMessage(String msg) {
		Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&', "&f&l[&c&l!&f&l] &f") + msg);
	}
	
	/**
	 * 메시지를 공지합니다.
	 */
	public static void broadcastMessage(String msg) {
		Bukkit.broadcastMessage(msg);
	}

	public static void broadcastMessage(List<String> messages) {
		for(String msg : messages) {
			broadcastMessage(msg);
		}
	}

	/**
	 * 채팅창을 청소합니다.
	 */
	public static void clearChat() {
		for(int i = 0; i < 100; i++) for(Player p : Bukkit.getOnlinePlayers()) Messager.sendMessage(p, "");
	}

	/**
	 * 플레이어의 채팅창을 청소합니다.
	 */
	public static void clearChat(Player target) {
		for(int i = 0; i < 100; i++) Messager.sendMessage(target, "");
	}
	
	/**
	 * 제목을 구성합니다.
	 */
	public static String formatTitle(String title) {
		String Base = "_________________________________________________________";
		int Pivot = Base.length() / 2;
		String Center = ChatColor.translateAlternateColorCodes('&', "[ " + "&e" + title + "&6" + " ]&m&l");
		String Return = ChatColor.translateAlternateColorCodes('&', "&6&m&l" + Base.substring(0, Math.max(0, (Pivot - Center.length() / 2))) + "&r&6");
		Return += Center + Base.substring(Pivot + Center.length() / 2);
		return Return;
	}
	
	/**
	 * 짧은 제목을 구성합니다.
	 */
	public static String formatShortTitle(String title) {
		String Base = "________________________________";
		int Pivot = Base.length() / 2;
		String Center = ChatColor.translateAlternateColorCodes('&', "[ " + "&e" + title + "&6" + " ]&m&l");
		String Return = ChatColor.translateAlternateColorCodes('&', "&6&m&l" + Base.substring(0, Math.max(0, (Pivot - Center.length() / 2))) + "&r&6");
		Return += Center + Base.substring(Pivot + Center.length() / 2);
		return Return;
	}

	/**
	 * 제목을 구성합니다.
	 */
	public static String formatTitle(ChatColor First, ChatColor Second, String title) {
		String Base = "_________________________________________________________";
		int Pivot = Base.length() / 2;
		String Center = ChatColor.translateAlternateColorCodes('&', "[ " + Second + title + First + " ]&m&l");
		String Return = ChatColor.translateAlternateColorCodes('&', First + "&m&l" + Base.substring(0, Math.max(0, (Pivot - Center.length() / 2))) + "&r" + First);
		Return += Center + Base.substring(Pivot + Center.length() / 2);
		return Return;
	}

	/**
	 * 짧은 제목을 구성합니다.
	 */
	public static String formatShortTitle(ChatColor First, ChatColor Second, String title) {
		String Base = "________________________________";
		int Pivot = Base.length() / 2;
		String Center = ChatColor.translateAlternateColorCodes('&', "[ " + Second + title + First + " ]&m&l");
		String Return = ChatColor.translateAlternateColorCodes('&', First + "&m&l" + Base.substring(0, Math.max(0, (Pivot - Center.length() / 2))) + "&r" + First);
		Return += Center + Base.substring(Pivot + Center.length() / 2);
		return Return;
	}

	/**
	 * 업데이트 설명을 구성합니다.
	 * @throws IOException 
	 */
	public static ArrayList<String> formatUpdate(UpdateObject update) throws IOException {
		ArrayList<String> UpdateInfo = new ArrayList<String>();
		UpdateInfo.add(Messager.formatTitle(ChatColor.DARK_GREEN, ChatColor.GREEN, "업데이트"));
		UpdateInfo.add(ChatColor.translateAlternateColorCodes('&', "&b" + update.getTag() + " &f업데이트 &f(&7v" + update.getVersion() + "&f) "
				+ "(&7" + (update.getFileSize() / 1024) + "KB&f)"));
		for(String s : update.getPatchNote()) {
			UpdateInfo.add(s);
		}
		UpdateInfo.add(ChatColor.translateAlternateColorCodes('&', "&2-----------------------------------------------------"));
		
		return UpdateInfo;
	}

	/**
	 * 업데이트 공지를 구성합니다.
	 * @throws IOException 
	 */
	public static ArrayList<String> formatUpdateNotice(UpdateObject update) throws IOException {
		ArrayList<String> UpdateInfo = new ArrayList<String>();
		UpdateInfo.add(Messager.formatTitle(ChatColor.DARK_AQUA, ChatColor.AQUA, "업데이트"));
		UpdateInfo.add(ChatColor.translateAlternateColorCodes('&', "&f적용 가능한 &3업데이트&f가 있습니다: &b" + update.getTag() + " &f업데이트 &f(&7v" + update.getVersion() + "&f) "
				+ "(&7" + (update.getFileSize() / 1024) + "KB&f)"));
		UpdateInfo.add(ChatColor.translateAlternateColorCodes('&', "&3업데이트&f를 진행하려면 &e/aw update &f명령어를 사용하세요."));
		UpdateInfo.add(ChatColor.translateAlternateColorCodes('&', "&3---------------------------------------------------------"));
		
		return UpdateInfo;
	}
	
	/**
	 * 능력 설명을 구성합니다.
	 */
	public static ArrayList<String> formatAbilityInfo(AbilityBase Ability) {
		ArrayList<String> AbilityInfo = new ArrayList<String>();
		AbilityInfo.add(formatShortTitle(ChatColor.GREEN, ChatColor.YELLOW, "능력 정보"));
		
		String name = Ability.getName();
		Rank rank = Ability.getRank();
		Species species = Ability.getSpecies();
		
		if(name != null && rank != null) {
			String Restricted = Ability.isRestricted() ? "&f[&7능력 비활성화됨&f]" : "&f[&a능력 활성화됨&f]";
			AbilityInfo.add(ChatColor.translateAlternateColorCodes('&', "&b" + name + " " + Restricted + " " + rank.getRankName() + " " + species.getName()));
			
			for(String s : Ability.getExplain()) {
				AbilityInfo.add(ChatColor.translateAlternateColorCodes('&', "&f" + s));
			}
		} else {
			AbilityInfo.add(ChatColor.translateAlternateColorCodes('&', "&c능력 설명을 불러오는 도중 오류가 발생하였습니다."));
			AbilityInfo.add(ChatColor.translateAlternateColorCodes('&', "&cAbility Class : " + Ability.getClass().getName()));
		}
		
		AbilityInfo.add(ChatColor.translateAlternateColorCodes('&', "&a------------------------------"));
		
		return AbilityInfo;
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
	public static String formatCommand(String Label, String Command, String Help, boolean AdminCommand) {
		if(!AdminCommand) {
			return ChatColor.translateAlternateColorCodes('&', "&a유  저: &6/" + Label + " &e" + Command + " &7: &f" + Help);
		} else {
			return ChatColor.translateAlternateColorCodes('&', "&c관리자: &6/" + Label + " &e" + Command + " &7: &f" + Help);
		}
	}

	/**
	 * 명령어 도움말을 구성합니다.
	 */
	public static String formatCommand(String Label, String Command, String Help) {
		return ChatColor.translateAlternateColorCodes('&', "&6/" + Label + " &e" + Command + " &7: &f" + Help);
	}

	/**
	 * String ArrayList를 만듭니다.
	 */
	public static ArrayList<String> getStringList(String[] arr, String... str) {
		ArrayList<String> Return = new ArrayList<String>();
		for(String s : arr) {
			Return.add(s);
		}
		
		for(String s : str) {
			Return.add(s);
		}
		
		return Return;
	}
	
	/**
	 * String ArrayList를 만듭니다.
	 */
	public static ArrayList<String> getStringList(String... str) {
		ArrayList<String> Return = new ArrayList<String>();
		for(String s : str) {
			Return.add(s);
		}
		
		return Return;
	}
	
	/**
	 * 메시지 목록을 공지합니다.
	 */
	public static void broadcastStringList(List<String> msg) {
		for(String s : msg) {
			broadcastMessage(s);
		}
	}
	
	/**
	 * 명령어를 실행한 객체에게 메시지 목록을 전송합니다.
	 */
	public static void sendStringList(CommandSender sender, List<String> msg) {
		for(String s : msg) {
			sender.sendMessage(s);
		}
	}

	/**
	 * 플레이어에게 메시지 목록을 전송합니다.
	 */
	public static void sendStringList(Player p, ArrayList<String> msg) {
		for(String s : msg) {
			p.sendMessage(s);
		}
	}
	
	/**
	 * String 배열에서 첫번째 인수를 삭제합니다.
	 */
	public static String[] removeFirstArg(String[] args) {
		return removeArgs(args, 1);
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

}
