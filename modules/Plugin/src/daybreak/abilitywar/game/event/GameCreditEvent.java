package daybreak.abilitywar.game.event;

import daybreak.abilitywar.game.Game;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class GameCreditEvent extends GameEvent {

	private static final HandlerList handlers = new HandlerList();

	public static HandlerList getHandlerList() {
		return handlers;
	}

	@Override
	public @NotNull HandlerList getHandlers() {
		return handlers;
	}

	private final List<String> credits = new LinkedList<>();

	public GameCreditEvent(Game game) {
		super(game);
	}

	public List<String> getCredits() {
		return Collections.unmodifiableList(credits);
	}

	public void addCredit(final String... strings) {
		credits.addAll(Arrays.asList(strings));
	}

}
