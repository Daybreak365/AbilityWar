package daybreak.abilitywar.game.team.event;

import daybreak.abilitywar.game.Game;
import daybreak.abilitywar.game.event.GameEvent;
import daybreak.abilitywar.game.team.interfaces.Members;
import daybreak.abilitywar.game.team.interfaces.Teamable;
import org.jetbrains.annotations.NotNull;

public abstract class TeamEvent extends GameEvent {

	private final Teamable teamable;
	private final Members team;

	public TeamEvent(@NotNull final Game game, @NotNull final Teamable teamable, @NotNull final Members team) {
		super(game);
		this.teamable = teamable;
		this.team = team;
	}

	@NotNull
	public Teamable getTeamable() {
		return teamable;
	}

	@NotNull
	public Members getTeam() {
		return team;
	}
}
