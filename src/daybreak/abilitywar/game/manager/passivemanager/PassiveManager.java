package daybreak.abilitywar.game.manager.passivemanager;

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

import daybreak.abilitywar.AbilityWar;
import daybreak.abilitywar.game.games.mode.AbstractGame;
import daybreak.abilitywar.utils.ReflectionUtil;

public class PassiveManager implements Listener, EventExecutor {

	public PassiveManager(AbstractGame game) {
		game.registerListener(this);
	}

	private final HashMap<Class<? extends Event>, ArrayList<PassiveExecutor>> passiveExecutors = new HashMap<>();
	private final EventPriority priority = EventPriority.HIGHEST;
	private final ArrayList<Class<? extends Event>> registeredEvents = new ArrayList<>();

	@SuppressWarnings("unchecked")
	private Class<? extends Event> getHandlerListDeclaringClass(Class<? extends Event> eventClass) {
		Class<? extends Event> handlerClass = null;
		try {
			for (Field f : ReflectionUtil.FieldUtil.getDeclaredInheritedFields(eventClass)) {
				if (f.getType().equals(HandlerList.class)) {
					handlerClass = (Class<? extends Event>) f.getDeclaringClass();
				}
			}
		} catch (Exception ignored) {}
		return handlerClass;
	}

	public void register(Class<? extends Event> eventClass, PassiveExecutor executor) {
		if (!passiveExecutors.containsKey(eventClass)) {
			passiveExecutors.put(eventClass, new ArrayList<>());
		}

		Class<? extends Event> handlerDeclaringClass = getHandlerListDeclaringClass(eventClass);
		if (handlerDeclaringClass != null && !registeredEvents.contains(handlerDeclaringClass)) {
			Bukkit.getPluginManager().registerEvent(handlerDeclaringClass, this, priority, this, AbilityWar.getPlugin());
			registeredEvents.add(handlerDeclaringClass);
		}

		List<PassiveExecutor> list = passiveExecutors.get(eventClass);
		if (!list.contains(executor)) {
			list.add(executor);
		}
	}

	public void unregisterAll(PassiveExecutor executor) {
		for (Class<? extends Event> eventClass : passiveExecutors.keySet()) {
			List<PassiveExecutor> list = passiveExecutors.get(eventClass);
			while (list.contains(executor))
				list.remove(executor);
		}
	}

	@Override
	public void execute(Listener listener, Event event) {
		Class<?> eventClass = event.getClass();
		if (passiveExecutors.containsKey(eventClass)) {
			for (PassiveExecutor executor : passiveExecutors.get(eventClass)) {
				executor.execute(event);
			}
		}
	}

}
