package daybreak.abilitywar.game.events.participant;

import daybreak.abilitywar.game.games.mode.AbstractGame.Participant;
import org.bukkit.event.HandlerList;

public class ParticipantDeathEvent extends ParticipantEvent {

	public ParticipantDeathEvent(Participant participant) {
		super(participant);
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
