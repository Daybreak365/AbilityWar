package daybreak.abilitywar.game.event;

import daybreak.abilitywar.game.Game;
import org.bukkit.event.Event;

public abstract class GameEvent extends Event {

	private final Game game;

	protected GameEvent(final Game game) {
		this.game = game;
	}

	public Game getGame() {
		return game;
	}

}
