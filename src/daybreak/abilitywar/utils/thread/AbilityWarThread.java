package daybreak.abilitywar.utils.thread;

import daybreak.abilitywar.game.games.mode.AbstractGame;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;

/**
 * Ability War 플러그인 쓰레드
 *
 * @author Daybreak 새벽
 */
public class AbilityWarThread {

	private AbilityWarThread() {
	}

	private static AbstractGame currentGame = null;

	/**
	 * 게임을 시작시킵니다. 진행중인 게임이 있을 경우 아무 작업도 하지 않습니다.
	 *
	 * @param game 시작시킬 게임
	 */
	public static void StartGame(AbstractGame game) {
		if (!isGameTaskRunning()) {
			setGame(game);
			game.start();
		}
	}

	/**
	 * 진행중인 게임을 종료합니다. 진행중인 게임이 없을 경우 아무 작업도 하지 않습니다.
	 */
	public static void StopGame() {
		if (isGameTaskRunning()) {
			currentGame.stop(false);
			setGame(null);

			Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&', "&7게임이 중지되었습니다."));
		}
	}

	private static void setGame(final AbstractGame game) {
		currentGame = game;
	}

	/**
	 * 게임이 진행중일 경우 true, 아닐 경우 false를 반환합니다.
	 */
	public static boolean isGameTaskRunning() {
		return currentGame != null && currentGame.isRunning();
	}

	public static boolean isGameOf(Class<?> clazz) {
		return currentGame != null && clazz.isAssignableFrom(currentGame.getClass());
	}

	/**
	 * AbstractGame을 반환합니다. 진행중인 게임이 없을 경우 null을 반환합니다.
	 */
	public static AbstractGame getGame() {
		return currentGame;
	}

}
