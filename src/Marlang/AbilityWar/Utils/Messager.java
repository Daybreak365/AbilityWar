package Marlang.AbilityWar.Utils;

import org.bukkit.ChatColor;

/**
 * 메시지 관리 클래스
 */
public class Messager {
	
	private static String Prefix = ChatColor.translateAlternateColorCodes('&', "&2《&aAbilityWar&2》&f");
	
	/**
	 * 콘솔에 메시지를 전송합니다.
	 * @param msg
	 */
	public static void sendMessage(String msg) {
		System.out.println(Prefix + msg);
	}
	
}
