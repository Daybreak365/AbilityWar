package DayBreak.AbilityWar.Game.Manager.PassiveManager;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.event.Event;
import org.bukkit.event.EventException;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.plugin.EventExecutor;

import DayBreak.AbilityWar.AbilityWar;
import DayBreak.AbilityWar.Game.Games.Mode.AbstractGame;
import DayBreak.AbilityWar.Utils.ReflectionUtil;

public class PassiveManager implements Listener, EventExecutor {

	private final Map<Class<? extends Event>, List<PassiveExecutor>> passiveExecutors = new HashMap<>();
	
	public PassiveManager(AbstractGame game) {
		game.registerListener(this);
	}
	
	private final EventPriority priority = EventPriority.HIGHEST;
	
	@SuppressWarnings("unchecked")
	private Class<? extends Event> getHandlerListDeclaringClass(Class<? extends Event> eventClass) {
		Class<? extends Event> handlerClass = null;
		try {
			for(Field f : ReflectionUtil.FieldUtil.getDeclaredInheritedFields(eventClass)) {
				if(f.getType().equals(HandlerList.class)) {
					handlerClass = (Class<? extends Event>) f.getDeclaringClass();
				}
			}
		} catch(Exception ex) {}
		return handlerClass;
	}
	
	private final List<Class<? extends Event>> registeredEvents = new ArrayList<>();
	
	public void register(Class<? extends Event> eventClass, PassiveExecutor executor) {
		if(!passiveExecutors.containsKey(eventClass)) {
			passiveExecutors.put(eventClass, new ArrayList<PassiveExecutor>());
		}
		
		Class<? extends Event> handlerDeclaringClass = getHandlerListDeclaringClass(eventClass);
		if(handlerDeclaringClass != null && !registeredEvents.contains(handlerDeclaringClass)) {
			Bukkit.getPluginManager().registerEvent(handlerDeclaringClass, this, priority, this, AbilityWar.getPlugin());
			registeredEvents.add(handlerDeclaringClass);
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
