package daybreak.abilitywar.game.events;

import daybreak.abilitywar.game.games.standard.Game;

/**
 * {@link Game} 타이머가 실행될 때 호출되는 이벤트입니다.
 */
public class GameReadyEvent extends GameEvent {

	public GameReadyEvent(Game game) {
		super(game);
	}

}
