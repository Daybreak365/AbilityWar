package DayBreak.AbilityWar.Game.Manager.EventExecutor;

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

	private final Map<Class<? extends Event>, List<Executor>> executorMap = new HashMap<>();
	
	private final AbstractGame game;
	
	public PassiveManager(AbstractGame game) {
		this.game = game;
	}
	
	public void registerExecutor(Class<? extends Event> eventClass, Executor executor) {
		if(!executorMap.containsKey(eventClass)) {
			executorMap.put(eventClass, new ArrayList<>());
			Bukkit.getPluginManager().registerEvent(eventClass, game, EventPriority.HIGHEST, this, AbilityWar.getPlugin());
		}
		
		if(!executorMap.get(eventClass).contains(executor)) executorMap.get(eventClass).add(executor);
	}

	public void unregisterExecutor(Executor executor) {
		for(Class<? extends Event> eventClass : executorMap.keySet()) {
			List<Executor> list = executorMap.get(eventClass);
			if(list.contains(executor)) list.remove(executor);
		}
	}
	
	@Override
	public void execute(Listener listener, Event event) throws EventException {
		Class<? extends Event> eventClass = event.getClass();
		if(executorMap.containsKey(eventClass)) {
			for(Executor exe : executorMap.get(eventClass)) {
				exe.execute(event);
			}
		}
	}
	
}
