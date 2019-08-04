package DayBreak.AbilityWar.Game.Manager.PassiveManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.event.Event;
import org.bukkit.event.EventException;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.plugin.EventExecutor;

import DayBreak.AbilityWar.AbilityWar;
import DayBreak.AbilityWar.Game.Games.Mode.AbstractGame;

public class PassiveManager implements EventExecutor {

	private final Map<Class<? extends Event>, List<PassiveExecutor>> passiveExecutors = new HashMap<>();
	
	private final AbstractGame game;
	
	public PassiveManager(AbstractGame game) {
		this.game = game;
	}
	
	private final List<Class<? extends Event>> registeredEvents = new ArrayList<>();
	
	public void register(Class<? extends Event> eventClass, PassiveExecutor executor) {
		if(!passiveExecutors.containsKey(eventClass)) {
			passiveExecutors.put(eventClass, new ArrayList<PassiveExecutor>());
			if(eventClass != null && !registeredEvents.contains(eventClass)) {
				Bukkit.getPluginManager().registerEvent(eventClass, game, EventPriority.HIGHEST, this, AbilityWar.getPlugin());
				registeredEvents.add(eventClass);
			}
		}
		List<PassiveExecutor> list = passiveExecutors.get(eventClass);
		if(!list.contains(executor)) list.add(executor);
	}

	public void unregisterAll(PassiveExecutor executor) {
		for(Class<? extends Event> eventClass : passiveExecutors.keySet()) {
			List<PassiveExecutor> list = passiveExecutors.get(eventClass);
			while(list.contains(executor)) list.remove(executor);
		}
	}

	@Override
	public void execute(Listener listener, Event event) throws EventException {
		Class<? extends Event> eventClass = event.getClass();
		if(passiveExecutors.containsKey(eventClass)) {
			for(PassiveExecutor pe : new ArrayList<>(passiveExecutors.get(eventClass))) {
				pe.execute(event);
			}
		}
	}
	
}
