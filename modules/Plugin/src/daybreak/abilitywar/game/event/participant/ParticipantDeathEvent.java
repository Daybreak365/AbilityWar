package daybreak.abilitywar.game.event.participant;

import daybreak.abilitywar.game.AbstractGame.Participant;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class ParticipantDeathEvent extends ParticipantEvent implements Cancellable {

	private static final HandlerList handlers = new HandlerList();

	@Override
	public @NotNull HandlerList getHandlers() {
		return handlers;
	}

	public static HandlerList getHandlerList() {
		return handlers;
	}

	public ParticipantDeathEvent(Participant participant) {
		super(participant);
	}

	private boolean isCancelled = false;

	@Override
	public boolean isCancelled() {
		return isCancelled;
	}

	@Override
	public void setCancelled(boolean isCancelled) {
		this.isCancelled = isCancelled;
	}

}
