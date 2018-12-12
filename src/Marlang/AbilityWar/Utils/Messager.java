package Marlang.AbilityWar.Utils;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import Marlang.AbilityWar.AbilityWar;

/**
 * 메시지 관리 클래스
 */
public class Messager {
	
	private static String Prefix = ChatColor.translateAlternateColorCodes('&', "&2《&aAbilityWar&2》&f");
	
	private static AbilityWar Plugin;
	
	public static void Initialize(AbilityWar Plugin) {
		Messager.Plugin = Plugin;
	}
	
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
	/*
	public static ArrayList<String> formatAbility(AbilityBase Ability) {
		ArrayList<String> AbilityInfo = new ArrayList<String>();
		AbilityInfo.add("");
		AbilityInfo.add(ChatColor.translateAlternateColorCodes('&', "&b&l" + Ability.getAbilityName() + " &f&l[&7&l능력 비활성화됨&f&l] " + Ability.getRank().getRankName()));
		for(String s : Ability.getInfo()) {
			AbilityInfo.add(ChatColor.translateAlternateColorCodes('&', "&f&l" + s));
		}

		AbilityInfo.add("");
		return AbilityInfo;
	}
	*/
	

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
	 * Sync 메시지를 공지합니다.
	 */
	public static void broadcastSyncMessage(List<String> msg) {
		Bukkit.getScheduler().runTaskAsynchronously(Plugin, new Runnable() {
			@Override
			public void run() {
				for(String s : msg) {
					broadcastMessage(s);
				}
			}
		});
	}
	
	/**
	 * 명령어를 실행한 객체에게 Sync 메시지를 전송합니다.
	 */
	public static void sendSyncMessage(CommandSender sender, ArrayList<String> msg) {
		Bukkit.getScheduler().runTaskAsynchronously(Plugin, new Runnable() {
			@Override
			public void run() {
				for(String s : msg) {
					sender.sendMessage(s);
				}
			}
		});
	}

	/**
	 * 플레이어에게 Sync 메시지를 전송합니다.
	 */
	public static void sendSyncMessage(Player p, ArrayList<String> msg) {
		Bukkit.getScheduler().runTaskAsynchronously(Plugin, new Runnable() {
			@Override
			public void run() {
				for(String s : msg) {
					p.sendMessage(s);
				}
			}
		});
	}
	
}
