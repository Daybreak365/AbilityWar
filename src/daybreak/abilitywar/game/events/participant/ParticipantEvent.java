package daybreak.abilitywar.game.events.participant;

import daybreak.abilitywar.game.games.mode.AbstractGame;
import org.bukkit.event.player.PlayerEvent;

import static daybreak.abilitywar.utils.base.Precondition.checkNotNull;

public abstract class ParticipantEvent extends PlayerEvent {

	public ParticipantEvent(AbstractGame.Participant participant) {
		super(checkNotNull(participant).getPlayer());
		this.participant = participant;
	}

	private final AbstractGame.Participant participant;

	public AbstractGame.Participant getParticipant() {
		return participant;
	}

}
