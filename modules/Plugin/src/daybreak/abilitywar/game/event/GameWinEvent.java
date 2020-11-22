package daybreak.abilitywar.game.event;

import daybreak.abilitywar.game.AbstractGame;
import daybreak.abilitywar.game.team.interfaces.Members;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

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
		private final @NotNull List<String> winnerNames;

		public Winner(final @NotNull List<String> winnerNames) {
			this.winnerNames = winnerNames;
		}

		public @NotNull List<String> getWinnerNames() {
			return winnerNames;
		}
	}

	public static class PlayerWinner extends Winner {
		private final @NotNull List<Player> winners;

		public PlayerWinner(final @NotNull List<Player> winners) {
			super(winners.stream().map(Player::getName).collect(Collectors.toList()));
			this.winners = winners;
		}

		public @NotNull List<Player> getWinners() {
			return winners;
		}
	}

	public static class TeamWinner extends Winner {
		private final @NotNull Members winner;

		public TeamWinner(final @NotNull Members winner) {
			super(Collections.singletonList(winner.getName()));
			this.winner = winner;
		}

		public @NotNull Members getWinner() {
			return winner;
		}
	}

}
