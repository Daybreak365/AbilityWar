package daybreak.abilitywar.game.events;

import daybreak.abilitywar.game.games.mode.AbstractGame;
import daybreak.abilitywar.utils.thread.TimerBase;

/**
 * {@link AbstractGame} {@link TimerBase}가 실행될 때 호출되는 이벤트입니다.
 */
public class GameReadyEvent extends GameEvent {

	public GameReadyEvent(AbstractGame game) {
		super(game);
	}

}
