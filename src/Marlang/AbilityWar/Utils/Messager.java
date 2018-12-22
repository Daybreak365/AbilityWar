package Marlang.AbilityWar.Utils;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import Marlang.AbilityWar.Ability.AbilityBase;

/**
 * 메시지 관리 클래스
 */
public class Messager {
	
	private static String Prefix = ChatColor.translateAlternateColorCodes('&', "&2《&aAbilityWar&2》&f");

	/**
	 * 콘솔에 메시지를 전송합니다.
	 */
	public static void sendMessage(String msg) {
		System.out.println(Prefix + msg);
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
	 * 콘솔에 오류 메시지를 전송합니다.
	 */
	public static void sendErrorMessage(String msg) {
		System.out.println(Prefix + ChatColor.RED + msg);
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
	 * 메시지를 공지합니다.
	 */
	public static void broadcastMessage(String msg) {
		Bukkit.broadcastMessage(msg);
	}
	
	/**
	 * 제목을 구성합니다.
	 */
	public static String formatTitle(String title) {
		String Base = "______________________________________________________________";
		int Pivot = Base.length() / 2;
		String Center = ChatColor.translateAlternateColorCodes('&', "[ " + "&e" + title + "&6" + " ]");
		String Return = ChatColor.translateAlternateColorCodes('&', "&6" + Base.substring(0, Math.max(0, (Pivot - Center.length() / 2))));
		Return += Center + Base.substring(Pivot + Center.length() / 2);
		return Return;
	}
	
	/**
	 * 짧은 제목을 구성합니다.
	 */
	public static String formatShortTitle(String title) {
		String Base = "_____________________________________";
		int Pivot = Base.length() / 2;
		String Center = ChatColor.translateAlternateColorCodes('&', "[ " + "&e" + title + "&6" + " ]");
		String Return = ChatColor.translateAlternateColorCodes('&', "&6" + Base.substring(0, Math.max(0, (Pivot - Center.length() / 2))));
		Return += Center + Base.substring(Pivot + Center.length() / 2);
		return Return;
	}

	/**
	 * 제목을 구성합니다.
	 */
	public static String formatTitle(ChatColor First, ChatColor Second, String title) {
		String Base = "______________________________________________________________";
		int Pivot = Base.length() / 2;
		String Center = "[ " + Second + title + First + " ]";
		String Return = ChatColor.translateAlternateColorCodes('&', First + Base.substring(0, Math.max(0, (Pivot - Center.length() / 2))));
		Return += Center + Base.substring(Pivot + Center.length() / 2);
		return Return;
	}
	
	/**
	 * 짧은 제목을 구성합니다.
	 */
	public static String formatShortTitle(ChatColor First, ChatColor Second, String title) {
		String Base = "_____________________________________";
		int Pivot = Base.length() / 2;
		String Center = "[ " + Second + title + First + " ]";
		String Return = ChatColor.translateAlternateColorCodes('&', First + Base.substring(0, Math.max(0, (Pivot - Center.length() / 2))));
		Return += Center + Base.substring(Pivot + Center.length() / 2);
		return Return;
	}
	
	/**
	 * 능력 설명을 구성합니다.
	 */
	public static ArrayList<String> formatAbility(AbilityBase Ability) {
		ArrayList<String> AbilityInfo = new ArrayList<String>();
		AbilityInfo.add(formatShortTitle(ChatColor.GREEN, ChatColor.YELLOW, "능력 정보"));
		if(AbilityWarThread.getGame().isAbilityRestricted()) {
			AbilityInfo.add(ChatColor.translateAlternateColorCodes('&', "&b" + Ability.getAbilityName() + " &f[&7능력 비활성화됨&f] " + Ability.getRank().getRankName()));
		} else if(!AbilityWarThread.getGame().isAbilityRestricted()) {
			AbilityInfo.add(ChatColor.translateAlternateColorCodes('&', "&b" + Ability.getAbilityName() + " &f[&a능력 활성화됨&f] " + Ability.getRank().getRankName()));
		}
		
		for(String s : Ability.getExplain()) {
			AbilityInfo.add(ChatColor.translateAlternateColorCodes('&', "&f" + s));
		}
		
		AbilityInfo.add(ChatColor.translateAlternateColorCodes('&', "&a-----------------------------------------"));
		
		return AbilityInfo;
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
	 * ArrayList를 만듭니다.
	 */
	public static ArrayList<String> getArrayList(String... str) {
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
	public static void sendStringList(CommandSender sender, ArrayList<String> msg) {
		for(String s : msg) {
			sender.sendMessage(s);
		}
	}

	/**
	 * 플레이어에게 Sync 메시지를 전송합니다.
	 */
	public static void sendStringList(Player p, ArrayList<String> msg) {
		for(String s : msg) {
			p.sendMessage(s);
		}
	}
	
}
