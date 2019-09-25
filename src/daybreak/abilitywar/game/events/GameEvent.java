package daybreak.abilitywar.game.events;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import daybreak.abilitywar.game.games.mode.AbstractGame;

/**
 * {@link AbstractGame} 관련 이벤트
 */
public abstract class GameEvent extends Event {

	private final AbstractGame game;
	
	protected GameEvent(AbstractGame game) {
		this.game = game;
	}

	public AbstractGame getGame() {
		return game;
	}

	private static final HandlerList handlers = new HandlerList();

	@Override
	public HandlerList getHandlers() {
		return handlers;
	}

	public static HandlerList getHandlerList() {
		return handlers;
	}

}
