package daybreak.abilitywar.game.events;

import java.util.ArrayList;
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

	private final List<String> creditList = new ArrayList<String>();
	
	public List<String> getCreditList() {
		return new ArrayList<String>(creditList);
	}
	
	public void addCredit(String... strings) {
		for(String str : strings) {
			creditList.add(str);
		}
	}
	
}
