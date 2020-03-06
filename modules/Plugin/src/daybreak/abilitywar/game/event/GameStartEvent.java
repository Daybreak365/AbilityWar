package daybreak.abilitywar.game.event;

import daybreak.abilitywar.game.Game;

/**
 * {@link Game}이 시작될 때 호출되는 이벤트입니다.
 */
public class GameStartEvent extends GameEvent {

	public GameStartEvent(Game game) {
		super(game);
	}

}
