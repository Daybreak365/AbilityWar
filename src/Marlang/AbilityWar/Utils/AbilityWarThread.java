package Marlang.AbilityWar.Utils;

import org.bukkit.Bukkit;

import Marlang.AbilityWar.AbilityWar;
import Marlang.AbilityWar.GameManager.Game;

public class AbilityWarThread {
	
	private static AbilityWar Plugin;
	
	public static void Initialize(AbilityWar Plugin) {
		AbilityWarThread.Plugin = Plugin;
	}
	
	private static int GameTask = -1;
	
	private static Game Game;
	
	public static void toggleGameTask(boolean bool) {
		if(bool && !isGameTaskRunning()) {
			setGame(new Game());
			while(!isGameTaskRunning()) {
				GameTask = Bukkit.getScheduler().scheduleSyncRepeatingTask(Plugin, getGame(), 0, 20);
			}
		} else if(!bool && isGameTaskRunning()) {
			Bukkit.getScheduler().cancelTask(GameTask);
			GameTask = -1;
		}
	}
	
	public static boolean isGameTaskRunning() {
		return GameTask != -1;
	}

	public static Game getGame() {
		return Game;
	}

	public static void setGame(Game game) {
		Game = game;
	}
	
}
