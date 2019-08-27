package DayBreak.AbilityWar.Game.Events;

import DayBreak.AbilityWar.Game.Games.Mode.AbstractGame;
import DayBreak.AbilityWar.Utils.Thread.TimerBase;

/**
 * {@link AbstractGame} {@link TimerBase}가 실행될 때 호출되는 이벤트입니다.
 */
public class GameReadyEvent extends GameEvent {

	public GameReadyEvent(AbstractGame game) {
		super(game);
	}

}
