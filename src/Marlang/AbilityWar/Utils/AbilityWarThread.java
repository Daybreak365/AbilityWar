package Marlang.AbilityWar.Utils;

import org.bukkit.Bukkit;

import Marlang.AbilityWar.AbilityWar;
import Marlang.AbilityWar.API.Events.AbilityWarProgressEvent;
import Marlang.AbilityWar.API.Events.AbilityWarProgressEvent.Progress;
import Marlang.AbilityWar.GameManager.Game;

/**
 * Ability War 플러그인 쓰레드
 * @author _Marlang 말랑
 */
public class AbilityWarThread {
	
	private static int GameTask = -1;

	private static Game Game;

	public static void toggleGameTask(boolean bool) {
		if(bool && !isGameTaskRunning()) {
			setGame(new Game());
			while(!isGameTaskRunning()) {
				GameTask = Bukkit.getScheduler().scheduleSyncRepeatingTask(AbilityWar.getPlugin(), getGame(), 0, 20);
			}
		} else if(!bool && isGameTaskRunning()) {
			AbilityWarProgressEvent event = new AbilityWarProgressEvent(Progress.Game_ENDED, getGame().getGameAPI());
			Bukkit.getPluginManager().callEvent(event);
			
			Bukkit.getScheduler().cancelTask(GameTask);
			setGame(null);
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
