package daybreak.abilitywar.game.events;

import daybreak.abilitywar.game.games.mode.AbstractGame;

/**
 * {@link AbstractGame}이 시작될 때 호출되는 이벤트입니다.
 */
public class GameStartEvent extends GameEvent {

	public GameStartEvent(AbstractGame game) {
		super(game);
	}

}
