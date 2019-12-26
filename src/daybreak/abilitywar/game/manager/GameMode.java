package daybreak.abilitywar.game.manager;

import daybreak.abilitywar.config.AbilityWarSettings;
import daybreak.abilitywar.config.AbilityWarSettings.Settings;
import daybreak.abilitywar.config.enums.ConfigNodes;
import daybreak.abilitywar.game.games.changeability.ChangeAbilityWar;
import daybreak.abilitywar.game.games.mixability.MixAbility;
import daybreak.abilitywar.game.games.mode.AbstractGame;
import daybreak.abilitywar.game.games.mode.GameManifest;
import daybreak.abilitywar.game.games.squirtgunfight.SummerVacation;
import daybreak.abilitywar.game.games.standard.DefaultGame;
import daybreak.abilitywar.game.games.teamgame.TeamFight;
import daybreak.abilitywar.utils.Messager;
import daybreak.abilitywar.utils.thread.AbilityWarThread;
import org.bukkit.ChatColor;

import java.util.ArrayList;
import java.util.List;

/**
 * 게임 모드
 *
 * @author Daybreak 새벽
 */
public class GameMode {

	private GameMode() {
	}

	private static ArrayList<Class<? extends AbstractGame>> modeList = new ArrayList<>();

	static {
		registerGameMode(DefaultGame.class);
		registerGameMode(ChangeAbilityWar.class);
		registerGameMode(SummerVacation.class);
		registerGameMode(TeamFight.class);
		registerGameMode(MixAbility.class);
	}

	public static void registerGameMode(Class<? extends AbstractGame> gameClass) {
		if (!modeList.contains(gameClass)) {
			GameManifest manifest = gameClass.getAnnotation(GameManifest.class);
			if (manifest != null) {
				if (!containsName(manifest.Name())) {
					try {
						gameClass.getConstructor();
						modeList.add(gameClass);
					} catch (NoSuchMethodException | SecurityException e) {
						Messager.sendConsoleErrorMessage(ChatColor.translateAlternateColorCodes('&',
								"&e" + gameClass.getName() + " &f게임모드는 생성자가 올바르지 않아 등록되지 않았습니다."));
					}
				} else {
					Messager.sendConsoleErrorMessage(ChatColor.translateAlternateColorCodes('&',
							"&e" + gameClass.getName() + " &f게임모드는 겹치는 이름이 있어 등록되지 않았습니다."));
				}
			} else {
				Messager.sendConsoleErrorMessage(ChatColor.translateAlternateColorCodes('&',
						"&e" + gameClass.getName() + " &f게임모드는 GameManifest 어노테이션이 존재하지 않아 등록되지 않았습니다."));
			}
		} else {
			Messager.sendConsoleErrorMessage(gameClass.getName() + " 게임모드는 겹치는 이름이 있어 등록되지 않았습니다.");
		}
	}

	private static boolean containsName(String name) {
		for (Class<? extends AbstractGame> gameClass : modeList) {
			GameManifest manifest = gameClass.getAnnotation(GameManifest.class);
			if (manifest != null) {
				if (manifest.Name().equalsIgnoreCase(name)) {
					return true;
				}
			}
		}

		return false;
	}

	public static List<String> nameValues() {
		ArrayList<String> Values = new ArrayList<String>();

		for (Class<? extends AbstractGame> gameClass : modeList) {
			GameManifest manifest = gameClass.getAnnotation(GameManifest.class);
			if (manifest != null) {
				Values.add(manifest.Name());
			}
		}

		return Values;
	}

	public static Class<? extends AbstractGame> getByString(String name) {
		for (Class<? extends AbstractGame> gameClass : modeList) {
			GameManifest manifest = gameClass.getAnnotation(GameManifest.class);
			if (manifest != null) {
				if (manifest.Name().equalsIgnoreCase(name)) {
					return gameClass;
				}
			}
		}

		return null;
	}

	public static boolean startGame() {
		try {
			AbilityWarThread.StartGame(Settings.getGameMode().newInstance());
			return true;
		} catch (InstantiationException | IllegalAccessException e) {
			e.printStackTrace();
			AbilityWarSettings.modifyProperty(ConfigNodes.GAME_MODE, DefaultGame.class.getName());
			AbilityWarThread.StartGame(new DefaultGame());
			return false;
		}
	}

}
