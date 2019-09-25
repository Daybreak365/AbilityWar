package daybreak.abilitywar.game.events;

import daybreak.abilitywar.game.games.mode.AbstractGame;
import daybreak.abilitywar.game.manager.Invincibility;

/**
 * {@link Invincibility}가 종료될 때 호출되는 이벤트입니다.
 */
public class InvincibleEndEvent extends GameEvent {

	public InvincibleEndEvent(AbstractGame game) {
		super(game);
	}

}
