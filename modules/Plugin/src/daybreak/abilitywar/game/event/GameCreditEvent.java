package daybreak.abilitywar.game.event;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class GameCreditEvent extends Event {

	private static final HandlerList handlers = new HandlerList();

	@Override
	public HandlerList getHandlers() {
		return handlers;
	}

	public static HandlerList getHandlerList() {
		return handlers;
	}

	private final List<String> creditList = new ArrayList<>();

	public List<String> getCreditList() {
		return new ArrayList<>(creditList);
	}

	public void addCredit(String... strings) {
		creditList.addAll(Arrays.asList(strings));
	}

}
