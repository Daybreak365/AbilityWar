package daybreak.abilitywar.game.team.event;

import daybreak.abilitywar.game.AbstractGame.Participant;
import daybreak.abilitywar.game.Game;
import daybreak.abilitywar.game.team.interfaces.Members;
import daybreak.abilitywar.game.team.interfaces.Teamable;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class ParticipantTeamChangedEvent extends TeamEvent {

	private static final HandlerList handlers = new HandlerList();

	public static HandlerList getHandlerList() {
		return handlers;
	}

	@Override
	public @NotNull HandlerList getHandlers() {
		return handlers;
	}

	private final Participant participant;
	private final Members oldTeam;

	public ParticipantTeamChangedEvent(@NotNull Game game, @NotNull Teamable teamable, final @NotNull Participant participant, final @NotNull Members oldTeam, @NotNull Members team) {
		super(game, teamable, team);
		this.participant = participant;
		this.oldTeam = oldTeam;
	}

	public Participant getParticipant() {
		return participant;
	}

	public Members getOldTeam() {
		return oldTeam;
	}
}
