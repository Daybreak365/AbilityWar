package Marlang.AbilityWar.API;

import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;

import Marlang.AbilityWar.API.Exception.GameException;
import Marlang.AbilityWar.API.Exception.PlayerException;
import Marlang.AbilityWar.Ability.AbilityBase;
import Marlang.AbilityWar.GameManager.AbilitySelect;
import Marlang.AbilityWar.GameManager.Game;
import Marlang.AbilityWar.Utils.AbilityWarThread;
import Marlang.AbilityWar.Utils.TimerBase;

/**
 * 게임 API
 * @author _Marlang 말랑
 */
public class GameAPI {
	
	Game game;
	
	public GameAPI(Game game) {
		this.game = game;
	}
	
	/**
	 *                       게임 API를 받아옵니다.
	 * @return               GameAPI
	 * @throws GameException 게임이 시작되지 않은 경우
	 */
	public static GameAPI getAPI() throws GameException {
		if(AbilityWarThread.isGameTaskRunning()) {
			return AbilityWarThread.getGame().getGameAPI();
		} else {
			throw new GameException();
		}
	}
	
	/**
	 * 게임 진행 여부를 받아옵니다.
	 * 게임 진행중이면 True,
	 * 게임 진행중이 아니면 False를 반환합니다.
	 */
	public static boolean isGameRunning() {
		return AbilityWarThread.isGameTaskRunning();
	}
	
	/**
	 *                       게임을 시작시킵니다.
	 * @throws GameException 게임이 이미 시작된 경우
	 */
	public static void StartGame() throws GameException {
		if(!AbilityWarThread.isGameTaskRunning()) {
			AbilityWarThread.toggleGameTask(true);
		} else {
			throw new GameException();
		}
	}
	
	/**
	 *                       게임을 종료시킵니다.
	 * @throws GameException 게임이 시작되지 않은 경우
	 */
	public static void StopGame() throws GameException {
		if(AbilityWarThread.isGameTaskRunning()) {
			TimerBase.ResetTasks();
			HandlerList.unregisterAll(AbilityWarThread.getGame().getDeathManager());	
			AbilityWarThread.toggleGameTask(false);
			AbilityWarThread.setGame(null);
		} else {
			throw new GameException();
		}
	}
	
	/**
	 *                       능력 선택을 스킵합니다.
	 * @param AdminName      능력 선택을 스킵한 관리자의 이름
	 * @throws GameException 능력 선택이 진행중이지 않은 경우
	 */
	public void SkipAbilitySelect(String AdminName) throws GameException {
		AbilitySelect select = game.getAbilitySelect();
		if(select != null && !select.isAbilitySelectFinished()) {
			select.Skip(AdminName);
		} else {
			throw new GameException();
		}
	}
	
	/**
	 *                       능력 선택을 스킵합니다.
	 * @throws GameException 능력 선택이 진행중이지 않은 경우
	 */
	public void SkipAbilitySelect() throws GameException {
		AbilitySelect select = game.getAbilitySelect();
		if(select != null && !select.isAbilitySelectFinished()) {
			select.Skip("CONSOLE");
		} else {
			throw new GameException();
		}
	}
	
	public boolean HasAbility(Player p) {
		return game.getAbilities().containsKey(p);
	}
	
	/**
	 *                         플레이어의 능력을 받아옵니다.
	 * @return                 플레이어의 능력
	 * @throws PlayerException 플레이어에게 능력이 없을 경우
	 */
	public AbilityBase GetAbility(Player p) throws PlayerException {
		if(game.getAbilities().containsKey(p)) {
			return game.getAbilities().get(p);
		} else {
			throw new PlayerException();
		}
	}
	
	/**
	 *                         플레이어에게 능력을 부여합니다.
	 * @param p                능력을 부여할 플레이어
	 * @param Ability          플레이어에게 부여할 능력 클래스
	 * @throws PlayerException 플레이어가 게임 참여자가 아닐 경우
	 */
	public void SetAbility(Player p, Class<? extends AbilityBase> AbilityClass) throws PlayerException {
		if(game.getPlayers().contains(p)) {
			try {
				AbilityBase Ability = AbilityClass.newInstance();
				Ability.setPlayer(p);
				game.getAbilities().put(p, Ability);
			} catch (InstantiationException | IllegalAccessException e) {
				e.printStackTrace();
			}
		} else {
			throw new PlayerException();
		}
	}
	
}
