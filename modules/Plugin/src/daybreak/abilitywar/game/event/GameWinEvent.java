package daybreak.abilitywar.game.event;

import daybreak.abilitywar.game.AbstractGame;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class GameWinEvent extends GameEvent {

	private static final HandlerList handlers = new HandlerList();

	public static HandlerList getHandlerList() {
		return handlers;
	}

	@Override
	public @NotNull HandlerList getHandlers() {
		return handlers;
	}

	@NotNull
	private final Winner winner;

	public GameWinEvent(AbstractGame game, @NotNull Winner winner) {
		super(game);
		this.winner = winner;
	}

	@NotNull
	public Winner getWinner() {
		return winner;
	}

	public static class Winner {
		private final @Nullable List<Player> winners;
		private final @NotNull List<String> winnerNames;

		public Winner(final @Nullable List<Player> winners, final @NotNull List<String> winnerNames) {
			this.winners = winners;
			this.winnerNames = winnerNames;
		}

		public @Nullable List<Player> getWinners() {
			return winners;
		}

		public @NotNull List<String> getWinnerNames() {
			return winnerNames;
		}
	}

}
