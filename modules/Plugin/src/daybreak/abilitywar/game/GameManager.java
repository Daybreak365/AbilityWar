package daybreak.abilitywar.game;

import daybreak.abilitywar.config.Configuration;
import daybreak.abilitywar.config.Configuration.Settings;
import daybreak.abilitywar.config.enums.ConfigNodes;
import daybreak.abilitywar.game.list.standard.DefaultGame;
import daybreak.abilitywar.game.manager.GameFactory;
import daybreak.abilitywar.game.manager.GameFactory.GameRegistration;

import java.lang.reflect.InvocationTargetException;

public class GameManager {

	static AbstractGame currentGame = null;

	public static boolean isGameRunning() {
		return currentGame != null;
	}

	public static boolean isGameOf(Class<?> clazz) {
		return currentGame != null && clazz.isAssignableFrom(currentGame.getClass());
	}

	public static AbstractGame getGame() {
		return currentGame;
	}

	public static boolean startGame() {
		try {
			GameRegistration registration = GameFactory.getRegistration(Settings.getGameMode());
			if (registration != null) {
				return registration.getConstructor().newInstance().start();
			}
		} catch (InstantiationException | IllegalAccessException | InvocationTargetException ignored) {
		}
		Configuration.modifyProperty(ConfigNodes.GAME_MODE, DefaultGame.class.getName());
		return new DefaultGame().start();
	}

	public static boolean stopGame() {
		if (currentGame != null) {
			return currentGame.stop();
		}
		return false;
	}

}
