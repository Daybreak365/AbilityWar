package DayBreak.AbilityWar.Utils.Thread;

import org.bukkit.ChatColor;

import DayBreak.AbilityWar.Game.Games.Mode.AbstractGame;
import DayBreak.AbilityWar.Utils.Messager;

/**
 * Ability War 플러그인 쓰레드
 * @author DayBreak 새벽
 */
public class AbilityWarThread {
	
	private AbilityWarThread() {}
	
	private static AbstractGame Game = null;
	
	/**
	 * 게임을 시작시킵니다.
	 * 진행중인 게임이 있을 경우 아무 작업도 하지 않습니다.
	 * @param Game 시작시킬 게임
	 */
	public static void StartGame(final AbstractGame Game) {
		if(!isGameTaskRunning()) {
			setGame(Game);
			Game.StartTimer();
		}
	}
	
	/**
	 * 진행중인 게임을 종료합니다.
	 * 진행중인 게임이 없을 경우 아무 작업도 하지 않습니다.
	 */
	public static void StopGame() {
		if(isGameTaskRunning()) {
			Game.StopTimer();
			setGame(null);
			
			Messager.broadcastMessage(ChatColor.translateAlternateColorCodes('&', "&7게임이 중지되었습니다."));
		}
	}

	private static void setGame(final AbstractGame game) {
		Game = game;
	}
	
	/**
	 * 게임이 진행중일 경우 true, 아닐 경우 false를 반환합니다.
	 */
	public static boolean isGameTaskRunning() {
		return Game != null && Game.isTimerRunning();
	}
	
	/**
	 * AbstractGame을 반환합니다.
	 * 진행중인 게임이 없을 경우 null을 반환합니다.
	 */
	public static AbstractGame getGame() {
		return Game;
	}
	
}
