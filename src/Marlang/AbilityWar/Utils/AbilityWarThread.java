package Marlang.AbilityWar.Utils;

import org.bukkit.Bukkit;

import Marlang.AbilityWar.AbilityWar;
import Marlang.AbilityWar.GameManager.AbilitySelect;
import Marlang.AbilityWar.GameManager.Game;

/**
 * Ability War 플러그인 쓰레드
 * @author _Marlang 말랑
 */
public class AbilityWarThread {
	
	private static int GameTask = -1;
	private static int AbilitySelectTask = -1;

	private static Game Game;
	private static AbilitySelect AbilitySelect;

	public static void toggleGameTask(boolean bool) {
		if(bool && !isGameTaskRunning()) {
			setGame(new Game());
			while(!isGameTaskRunning()) {
				GameTask = Bukkit.getScheduler().scheduleSyncRepeatingTask(AbilityWar.getPlugin(), getGame(), 0, 20);
			}
		} else if(!bool && isGameTaskRunning()) {
			Bukkit.getScheduler().cancelTask(GameTask);
			setGame(null);
			GameTask = -1;
		}
	}
	
	public static boolean isGameTaskRunning() {
		return GameTask != -1;
	}

	public static void toggleAbilitySelectTask(boolean bool) {
		if(bool && !isAbilitySelectTaskRunning()) {
			setAbilitySelect(new AbilitySelect(getGame().getPlayers()));
			while(!isAbilitySelectTaskRunning()) {
				AbilitySelectTask = Bukkit.getScheduler().scheduleSyncRepeatingTask(AbilityWar.getPlugin(), getAbilitySelect(), 0, 20);
			}
		} else if(!bool && isAbilitySelectTaskRunning()) {
			Bukkit.getScheduler().cancelTask(AbilitySelectTask);
			setAbilitySelect(null);
			AbilitySelectTask = -1;
		}
	}
	
	public static boolean isAbilitySelectTaskRunning() {
		return AbilitySelectTask != -1;
	}
	
	public static Game getGame() {
		return Game;
	}

	public static void setGame(Game game) {
		Game = game;
	}

	public static AbilitySelect getAbilitySelect() {
		return AbilitySelect;
	}

	public static void setAbilitySelect(AbilitySelect abilitySelect) {
		AbilitySelect = abilitySelect;
	}
	
}
