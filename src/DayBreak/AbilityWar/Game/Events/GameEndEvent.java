package DayBreak.AbilityWar.Game.Events;

import DayBreak.AbilityWar.Game.Games.Mode.AbstractGame;

/**
 * {@link AbstractGame}이 종료될 때 호출되는 이벤트입니다.
 */
public class GameEndEvent extends GameEvent {

	public GameEndEvent(AbstractGame game) {
		super(game);
	}

}
