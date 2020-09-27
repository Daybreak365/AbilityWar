package daybreak.abilitywar.game.event;

import daybreak.abilitywar.game.Game;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class InvincibilityStatusChangeEvent extends GameEvent {

	private static final HandlerList handlers = new HandlerList();

	public static HandlerList getHandlerList() {
		return handlers;
	}

	@Override
	public @NotNull HandlerList getHandlers() {
		return handlers;
	}

	private final boolean newStatus;

	public InvincibilityStatusChangeEvent(Game game, final boolean newStatus) {
		super(game);
		this.newStatus = newStatus;
	}

	public boolean getNewStatus() {
		return newStatus;
	}

}
