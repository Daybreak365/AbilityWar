package Marlang.AbilityWar.GameManager;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import Marlang.AbilityWar.Utils.AbilityWarThread;
import Marlang.AbilityWar.Utils.Messager;

public class MainCommand implements CommandExecutor {
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		return parseCommand(sender, label, args);
	}
	
	public boolean parseCommand(CommandSender sender, String label, String[] split) {
		if(split.length == 0) {
			sendHelpCommand(sender, label);
		} else {
			if(split[0].equalsIgnoreCase("start")) {
				if(sender.isOp()) {
					if(!AbilityWarThread.isGameTaskRunning()) {
						AbilityWarThread.toggleGameTask(true);
						Messager.broadcastMessage(ChatColor.translateAlternateColorCodes('&', "&f관리자 &e" + sender.getName() + "&f님이 게임을 시작시켰습니다."));
					} else {
						Messager.sendMessage(sender, ChatColor.translateAlternateColorCodes('&', "&c능력자 전쟁이 이미 진행되고 있습니다."));
					}
				} else {
					Messager.sendMessage(sender, ChatColor.translateAlternateColorCodes('&', "&c이 명령어를 사용하려면 OP 권한이 있어야 합니다."));
				}
			} else if(split[0].equalsIgnoreCase("stop")) {
				if(sender.isOp()) {
					if(AbilityWarThread.isGameTaskRunning()) {
						AbilityWarThread.toggleGameTask(false);
						Messager.broadcastMessage(ChatColor.translateAlternateColorCodes('&', "&f관리자 &e" + sender.getName() + "&f님이 게임을 중지시켰습니다."));
						AbilityWarThread.setGame(null);
						Messager.broadcastMessage(ChatColor.translateAlternateColorCodes('&', "&7게임이 초기화되었습니다."));
					} else {
						Messager.sendMessage(sender, ChatColor.translateAlternateColorCodes('&', "&c능력자 전쟁이 진행되고 있지 않습니다."));
					}
				} else {
					Messager.sendMessage(sender, ChatColor.translateAlternateColorCodes('&', "&c이 명령어를 사용하려면 OP 권한이 있어야 합니다."));
				}
			}
		}
		
		return true;
	}

	public void sendHelpCommand(CommandSender sender, String label) {
		Messager.sendSyncMessage(sender, Messager.getArrayList(
				Messager.formatTitle("능력자 전쟁"),
				Messager.formatCommand(label, "start", "능력자 전쟁을 시작시킵니다.", true),
				Messager.formatCommand(label, "stop", "능력자 전쟁을 중지시킵니다.", true)));
	}
	
}
