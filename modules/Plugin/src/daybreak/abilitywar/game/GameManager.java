package daybreak.abilitywar.game;

import daybreak.abilitywar.config.Configuration;
import daybreak.abilitywar.config.Configuration.Settings;
import daybreak.abilitywar.config.Configuration.Settings.DeveloperSettings;
import daybreak.abilitywar.config.enums.ConfigNodes;
import daybreak.abilitywar.game.list.standard.StandardGame;
import daybreak.abilitywar.game.manager.GameFactory;
import daybreak.abilitywar.game.manager.GameFactory.GameRegistration;
import daybreak.abilitywar.game.manager.GameFactory.GameRegistration.Flag;
import daybreak.abilitywar.game.manager.GameFactory.TeamGameRegistration;
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

	public static boolean startGame(String[] args) throws IllegalArgumentException {
		try {
			final GameRegistration registration = GameFactory.getRegistration(Settings.getGameMode());
			if (registration != null) {
				if (registration.hasFlag(Flag.TEAM_GAME_SUPPORTED) && Settings.isTeamGameEnabled()) {
					final TeamGameRegistration teamGame = registration.getTeamGame();
					if (teamGame.hasFlag(Flag.CONSTRUCTOR_ARGS)) {
						return teamGame.getConstructor().newInstance((Object) args).start();
					} else {
						return teamGame.getConstructor().newInstance().start();
					}
				} else {
					if (registration.hasFlag(Flag.CONSTRUCTOR_ARGS)) {
						return registration.getConstructor().newInstance((Object) args).start();
					} else {
						return registration.getConstructor().newInstance().start();
					}
				}
			}
		} catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
			if (e.getCause() != null && e.getCause() instanceof IllegalArgumentException) {
				throw (IllegalArgumentException) e.getCause();
			}
			if (DeveloperSettings.isEnabled()) {
				e.printStackTrace();
			}
		}
		Configuration.modifyProperty(ConfigNodes.GAME_MODE, StandardGame.class.getName());
		return new StandardGame().start();
	}

	public static boolean stopGame() {
		if (currentGame != null) {
			return currentGame.stop();
		}
		return false;
	}

}
