package daybreak.abilitywar.game.events.participant;

import daybreak.abilitywar.game.games.mode.AbstractGame.Participant;
import daybreak.abilitywar.utils.base.Precondition;
import org.bukkit.event.player.PlayerEvent;

public abstract class ParticipantEvent extends PlayerEvent {

	public ParticipantEvent(Participant participant) {
		super(Precondition.checkNotNull(participant).getPlayer());
		this.participant = participant;
	}

	private final Participant participant;

	public Participant getParticipant() {
		return participant;
	}

}
