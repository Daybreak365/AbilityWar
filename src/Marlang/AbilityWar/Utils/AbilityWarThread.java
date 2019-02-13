package Marlang.AbilityWar.Utils;

import org.bukkit.Bukkit;

import Marlang.AbilityWar.AbilityWar;
import Marlang.AbilityWar.GameManager.Game.AbstractGame;

/**
 * Ability War 플러그인 쓰레드
 * @author _Marlang 말랑
 */
public class AbilityWarThread {
	
	private static int GameTask = -1;

	private static AbstractGame Game;
	
	public static void startGame(AbstractGame Game) {
		if(!isGameTaskRunning()) {
			setGame(Game);
			while(!isGameTaskRunning()) {
				GameTask = Bukkit.getScheduler().scheduleSyncRepeatingTask(AbilityWar.getPlugin(), getGame(), 0, 20);
			}
		}
	}
	
	public static void stopGame() {
		if(isGameTaskRunning()) {
			//Notify
			getGame().GameEnd();
			//Notify
			
			Bukkit.getScheduler().cancelTask(GameTask);
			setGame(null);
			GameTask = -1;
		}
	}
	
	public static boolean isGameTaskRunning() {
		return GameTask != -1;
	}
	
	public static AbstractGame getGame() {
		return Game;
	}
	
	public static void setGame(AbstractGame game) {
		Game = game;
	}
	
}
