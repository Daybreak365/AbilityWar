package daybreak.abilitywar.utils.base;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

public class Messager {

	private Messager() {
	}

	public static final String defaultPrefix = "§2《§aAbilityWar§2》§f";
	private static final ConsoleCommandSender console = Bukkit.getConsoleSender();

	public static List<String> asList(String... strings) {
		List<String> list = new ArrayList<>(strings.length);
		Collections.addAll(list, strings);
		return list;
	}

	public static Set<String> asSet(String... strings) {
		Set<String> list = new HashSet<>(strings.length);
		Collections.addAll(list, strings);
		return list;
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

	public static void sendErrorMessage(CommandSender cs, String str) {
		cs.sendMessage(ChatColor.WHITE + "[" + ChatColor.RED + "!" + ChatColor.WHITE + "] " + ChatColor.RED + str);
	}

	public static void broadcastErrorMessage(String str) {
		Bukkit.broadcastMessage(ChatColor.WHITE + "[" + ChatColor.RED + "!" + ChatColor.WHITE + "] " + ChatColor.RED + str);

	}

	public static void sendConsoleMessage(String string) {
		console.sendMessage(defaultPrefix + string);
	}

	public static void sendConsoleMessage(Iterable<String> strings) {
		for (String string : strings) {
			console.sendMessage(defaultPrefix + string);
		}
	}

}
