package daybreak.abilitywar.game.event;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class GameCreditEvent extends Event {

	private static final HandlerList handlers = new HandlerList();

	@Override
	public HandlerList getHandlers() {
		return handlers;
	}

	public static HandlerList getHandlerList() {
		return handlers;
	}

	private final List<String> creditList = new ArrayList<String>();

	public List<String> getCreditList() {
		return new ArrayList<String>(creditList);
	}

	public void addCredit(String... strings) {
		creditList.addAll(Arrays.asList(strings));
	}

}
