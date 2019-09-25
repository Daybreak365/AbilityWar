package daybreak.abilitywar.game.events;

import static daybreak.abilitywar.utils.Validate.notNull;

import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;

import daybreak.abilitywar.game.games.mode.AbstractGame.Participant;

public class ParticipantDeathEvent extends PlayerEvent {

	public ParticipantDeathEvent(Participant p) {
		super(notNull(p).getPlayer());
		this.participant = p;
	}

	private final Participant participant;

	public Participant getParticipant() {
		return participant;
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
