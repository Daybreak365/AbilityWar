package Marlang.AbilityWar.GameManager;

import java.util.ArrayList;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import Marlang.AbilityWar.AbilityWar;
import Marlang.AbilityWar.Utils.Messager;

/**
 * 게임 관리 클래스
 * @author _Marlang 말랑
 */
public class Game extends Thread {
	
	private static AbilityWar Plugin;
	
	public static void Initialize(AbilityWar Plugin) {
		Game.Plugin = Plugin;
	}
	
	int Seconds = 0;
	
	ArrayList<Player> Spectators = new ArrayList<Player>();
	ArrayList<Player> Players = new ArrayList<Player>();
	
	@Override
	public void run() {
		Seconds++;
		GameProgress(Seconds);
	}
	
	public void GameProgress(Integer Seconds) {
		switch(Seconds) {
			case 1:
				SetupPlayers();
				broadcastPlayerList();
				break;
			case 5:
				broadcastPluginDescription();
				break;
		}
	}
	
	public void broadcastPlayerList() {
		int Count = 0;
		
        ArrayList<String> msg = new ArrayList<String>();
		
		msg.add(ChatColor.translateAlternateColorCodes('&', "&6==== &e게임 참여자 목록 &6===="));
		for(Player p : Players) {
			Count++;
			msg.add(ChatColor.translateAlternateColorCodes('&', "&a" + Count + ". &f" + p.getName()));
		}
		msg.add(ChatColor.translateAlternateColorCodes('&', "&e총 인원수 : " + Count + "명"));
		msg.add(ChatColor.translateAlternateColorCodes('&', "&6==========================="));
		
		Messager.broadcastSyncMessage(msg);
	}
	
	public void broadcastPluginDescription() {
		ArrayList<String> msg = Messager.getArrayList(
				ChatColor.translateAlternateColorCodes('&', "&cAbilityWar &f- &6능력자 전쟁"),
				ChatColor.translateAlternateColorCodes('&', "&e버전 &7: &f" + Plugin.getDescription().getVersion()),
				ChatColor.translateAlternateColorCodes('&', "&b개발자 &7: &f_Marlang"),
				ChatColor.translateAlternateColorCodes('&', "&9디스코드 &7: &f말랑&7#5908"));
		
		Messager.broadcastSyncMessage(msg);
	}
	
	public ArrayList<Player> getSpectators() {
		return Spectators;
	}
	
	public void SetupPlayers() {
		ArrayList<Player> Players = new ArrayList<Player>();
		
		for(Player p : Bukkit.getOnlinePlayers()) {
			if(!getSpectators().contains(p)) {
				Players.add(p);
			}
		}

		this.Players = Players;
	}
	
}
