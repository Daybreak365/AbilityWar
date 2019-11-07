package daybreak.abilitywar.game.events;

import daybreak.abilitywar.game.games.standard.Game;

/**
 * {@link Game}이 시작될 때 호출되는 이벤트입니다.
 */
public class GameStartEvent extends GameEvent {

	public GameStartEvent(Game game) {
		super(game);
	}

}
