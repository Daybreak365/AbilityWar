package daybreak.abilitywar.game.event;

import daybreak.abilitywar.game.AbstractGame;
import org.bukkit.event.Event;

public abstract class GameEvent extends Event {

	private final AbstractGame game;

	protected GameEvent(final AbstractGame game) {
		this.game = game;
	}

	public AbstractGame getGame() {
		return game;
	}

}
