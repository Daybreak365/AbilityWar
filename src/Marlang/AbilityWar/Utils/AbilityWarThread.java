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
	
	/**
	 * 게임을 시작시킵니다.
	 * @param Game 시작시킬 게임
	 */
	public static void startGame(AbstractGame Game) {
		if(!isGameTaskRunning()) {
			setGame(Game);
			while(!isGameTaskRunning()) {
				GameTask = Bukkit.getScheduler().scheduleSyncRepeatingTask(AbilityWar.getPlugin(), getGame(), 0, 20);
			}
		}
	}
	
	/**
	 * 진행중인 게임을 종료합니다.
	 */
	public static void stopGame() {
		if(isGameTaskRunning()) {
			//Notify
			getGame().onGameEnd();
			//Notify
			
			Bukkit.getScheduler().cancelTask(GameTask);
			setGame(null);
			GameTask = -1;
		}
	}
	
	/**
	 * 게임이 진행중일 경우 true, 아닐 경우 false를 반환합니다.
	 */
	public static boolean isGameTaskRunning() {
		return GameTask != -1;
	}
	
	/**
	 * AbstractGame을 반환합니다.
	 * 진행중인 게임이 없을 경우 null을 반환합니다.
	 */
	public static AbstractGame getGame() {
		return Game;
	}
	
	private static void setGame(AbstractGame game) {
		Game = game;
	}
	
}
