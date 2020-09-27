package daybreak.abilitywar.game.team.event;

import daybreak.abilitywar.game.Game;
import daybreak.abilitywar.game.team.interfaces.Members;
import daybreak.abilitywar.game.team.interfaces.Teamable;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class TeamCreatedEvent extends TeamEvent {

	private static final HandlerList handlers = new HandlerList();

	public static HandlerList getHandlerList() {
		return handlers;
	}

	@Override
	public @NotNull HandlerList getHandlers() {
		return handlers;
	}

	public TeamCreatedEvent(@NotNull Game game, @NotNull Teamable teamable, @NotNull Members team) {
		super(game, teamable, team);
	}

}
