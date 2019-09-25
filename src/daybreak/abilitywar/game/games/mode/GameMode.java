package daybreak.abilitywar.game.games.mode;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.ChatColor;

import daybreak.abilitywar.config.AbilityWarSettings;
import daybreak.abilitywar.config.enums.ConfigNodes;
import daybreak.abilitywar.game.games.changeability.ChangeAbilityWar;
import daybreak.abilitywar.game.games.defaultgame.DefaultGame;
import daybreak.abilitywar.game.games.squirtgunfight.SummerVacation;
import daybreak.abilitywar.game.games.teamgame.TeamFight;
import daybreak.abilitywar.utils.Messager;
import daybreak.abilitywar.utils.thread.AbilityWarThread;

/**
 * 게임 모드
 * @author DayBreak 새벽
 */
public class GameMode {
	
	private static ArrayList<Class<? extends AbstractGame>> modeList = new ArrayList<>();

	static {
		registerGameMode(DefaultGame.class);
		registerGameMode(ChangeAbilityWar.class);
		registerGameMode(SummerVacation.class);
		registerGameMode(TeamFight.class);
	}

	public static void registerGameMode(Class<? extends AbstractGame> gameClass) {
		if(!modeList.contains(gameClass)) {
			GameManifest manifest = gameClass.getAnnotation(GameManifest.class);
			if(manifest != null) {
				if(!containsName(manifest.Name())) {
					try {
						gameClass.getConstructor();
						modeList.add(gameClass);
					} catch (NoSuchMethodException | SecurityException e) {
						Messager.sendConsoleErrorMessage(ChatColor.translateAlternateColorCodes('&', "&e" + gameClass.getName() + " &f게임모드는 생성자가 올바르지 않아 등록되지 않았습니다."));
					}
				} else {
					Messager.sendConsoleErrorMessage(ChatColor.translateAlternateColorCodes('&', "&e" + gameClass.getName() + " &f게임모드는 겹치는 이름이 있어 등록되지 않았습니다."));
				}
			} else {
				Messager.sendConsoleErrorMessage(ChatColor.translateAlternateColorCodes('&', "&e" + gameClass.getName() + " &f게임모드는 GameManifest 어노테이션이 존재하지 않아 등록되지 않았습니다."));
			}
		} else {
			Messager.sendConsoleErrorMessage(gameClass.getName() + " 게임모드는 겹치는 이름이 있어 등록되지 않았습니다.");
		}
	}

	private static boolean containsName(String name) {
		for(Class<? extends AbstractGame> gameClass : modeList) {
			GameManifest manifest = gameClass.getAnnotation(GameManifest.class);
			if(manifest != null) {
				if(manifest.Name().equalsIgnoreCase(name)) {
					return true;
				}
			}
		}
		
		return false;
	}

	public static List<String> nameValues() {
		ArrayList<String> Values = new ArrayList<String>();
		
		for(Class<? extends AbstractGame> gameClass : modeList) {
			GameManifest manifest = gameClass.getAnnotation(GameManifest.class);
			if(manifest != null) {
				Values.add(manifest.Name());
			}
		}
		
		return Values;
	}
	
	public static Class<? extends AbstractGame> getByString(String name) {
		for(Class<? extends AbstractGame> gameClass : modeList) {
			GameManifest manifest = gameClass.getAnnotation(GameManifest.class);
			if(manifest != null) {
				if(manifest.Name().equalsIgnoreCase(name)) {
					return gameClass;
				}
			}
		}
		
		return null;
	}
	
	public static boolean startGame() {
		try {
			AbilityWarThread.StartGame(AbilityWarSettings.getGameMode().newInstance());
			return true;
		} catch (InstantiationException | IllegalAccessException e) {
			AbilityWarSettings.setNewProperty(ConfigNodes.GameMode, DefaultGame.class.getName());
			AbilityWarThread.StartGame(new DefaultGame());
			return false;
		}
	}
	
}
