package DayBreak.AbilityWar.Game.Events;

import DayBreak.AbilityWar.Game.Games.Mode.AbstractGame;
import DayBreak.AbilityWar.Game.Manager.Invincibility;

/**
 * {@link Invincibility}가 종료될 때 호출되는 이벤트입니다.
 */
public class InvincibleEndEvent extends GameEvent {

	public InvincibleEndEvent(AbstractGame game) {
		super(game);
	}

}
