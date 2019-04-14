package DayBreak.AbilityWar.Utils.Thread;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;

import DayBreak.AbilityWar.AbilityWar;
import DayBreak.AbilityWar.Game.Games.AbstractGame;
import DayBreak.AbilityWar.Utils.Messager;

/**
 * Ability War 플러그인 쓰레드
 * @author DayBreak 새벽
 */
public class AbilityWarThread {
	
	private AbilityWarThread() {}
	
	private static int GameTask = -1;

	private static AbstractGame Game = null;
	
	/**
	 * 게임을 시작시킵니다.
	 * 진행중인 게임이 있을 경우 아무 작업도 하지 않습니다.
	 * @param Game 시작시킬 게임
	 */
	public static void StartGame(final AbstractGame Game) {
		if(!isGameTaskRunning()) {
			setGame(Game);
			while(!isGameTaskRunning()) {
				GameTask = Bukkit.getScheduler().scheduleSyncRepeatingTask(AbilityWar.getPlugin(), Game, 0, 20);
			}
		}
	}
	
	/**
	 * 진행중인 게임을 종료합니다.
	 * 진행중인 게임이 없을 경우 아무 작업도 하지 않습니다.
	 */
	public static void StopGame() {
		if(isGameTaskRunning()) {
			//Notify
			try {
				Method onEnd = AbstractGame.class.getDeclaredMethod("onEnd");
				onEnd.setAccessible(true);
				onEnd.invoke(getGame());
				onEnd.setAccessible(false);
				
				TimerBase.ResetTasks();
				
				Field gameListener = AbstractGame.class.getDeclaredField("gameListener");
				gameListener.setAccessible(true);
				HandlerList.unregisterAll((Listener) gameListener.get(getGame()));
				gameListener.setAccessible(false);
				
				HandlerList.unregisterAll(getGame());
			} catch(Exception ex) {
				//Should Not Happen
			}
			
			Bukkit.getScheduler().cancelTask(GameTask);
			setGame(null);
			GameTask = -1;
			
			Messager.broadcastMessage(ChatColor.translateAlternateColorCodes('&', "&7게임이 초기화되었습니다."));
		}
	}

	private static void setGame(AbstractGame game) {
		Game = game;
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
	
}
