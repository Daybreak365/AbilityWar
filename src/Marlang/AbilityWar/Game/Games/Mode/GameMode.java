package Marlang.AbilityWar.Game.Games.Mode;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.ChatColor;

import Marlang.AbilityWar.Config.AbilityWarSettings;
import Marlang.AbilityWar.Config.Nodes.ConfigNodes;
import Marlang.AbilityWar.Game.Games.AbstractGame;
import Marlang.AbilityWar.Game.Games.GameManifest;
import Marlang.AbilityWar.Utils.Messager;
import Marlang.AbilityWar.Utils.Thread.AbilityWarThread;

/**
 * 게임 모드
 * @author _Marlang 말랑
 */
public class GameMode {
	
	private static ArrayList<Class<? extends AbstractGame>> modeList = new ArrayList<>();

	static {
		registerGameMode(DefaultGame.class);
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
						Messager.sendMessage(ChatColor.translateAlternateColorCodes('&', "&e" + gameClass.getName() + " &f게임모드는 생성자가 올바르지 않아 등록되지 않았습니다."));
					}
				} else {
					Messager.sendMessage(ChatColor.translateAlternateColorCodes('&', "&e" + gameClass.getName() + " &f게임모드는 겹치는 이름이 있어 등록되지 않았습니다."));
				}
			} else {
				Messager.sendMessage(ChatColor.translateAlternateColorCodes('&', "&e" + gameClass.getName() + " &f게임모드는 GameManifest 어노테이션이 존재하지 않아 등록되지 않았습니다."));
			}
		} else {
			Messager.sendMessage(gameClass.getName() + " 게임모드는 겹치는 이름이 있어 등록되지 않았습니다.");
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
			AbilityWarThread.startGame(AbilityWarSettings.getGameMode().newInstance());
			return true;
		} catch (InstantiationException | IllegalAccessException e) {
			AbilityWarSettings.setNewProperty(ConfigNodes.Game_Mode, DefaultGame.class.getName());
			AbilityWarThread.startGame(new DefaultGame());
			return false;
		}
	}
	
}
