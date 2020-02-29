package daybreak.abilitywar.game.events.participant;

import com.google.common.base.Preconditions;
import daybreak.abilitywar.game.games.mode.AbstractGame.Participant;
import org.bukkit.event.player.PlayerEvent;

public abstract class ParticipantEvent extends PlayerEvent {

	public ParticipantEvent(Participant participant) {
		super(Preconditions.checkNotNull(participant).getPlayer());
		this.participant = participant;
	}

	private final Participant participant;

	public Participant getParticipant() {
		return participant;
	}

}
