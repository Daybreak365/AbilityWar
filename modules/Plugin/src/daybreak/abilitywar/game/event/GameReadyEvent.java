package daybreak.abilitywar.game.event;

import daybreak.abilitywar.game.Game;

/**
 * {@link Game} 타이머가 실행될 때 호출되는 이벤트입니다.
 */
public class GameReadyEvent extends GameEvent {

	public GameReadyEvent(Game game) {
		super(game);
	}

}
