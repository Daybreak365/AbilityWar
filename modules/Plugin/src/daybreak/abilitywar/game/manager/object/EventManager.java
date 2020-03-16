package daybreak.abilitywar.game.manager.object;

import daybreak.abilitywar.AbilityWar;
import daybreak.abilitywar.game.AbstractGame;
import daybreak.abilitywar.game.AbstractGame.GameUpdate;
import daybreak.abilitywar.utils.base.reflect.ReflectionUtil;
import org.bukkit.Bukkit;
import org.bukkit.event.Event;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.plugin.EventExecutor;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class EventManager implements Listener, EventExecutor, AbstractGame.Observer {

	public EventManager(AbstractGame game) {
		game.attachObserver(this);
		Bukkit.getPluginManager().registerEvents(this, AbilityWar.getPlugin());
	}

	private final HashMap<Class<? extends Event>, Set<Observer>> observers = new HashMap<>();
	private final Set<Class<? extends Event>> registeredEvents = new HashSet<>();

	@SuppressWarnings("unchecked")
	private Class<? extends Event> getHandlerListDeclaringClass(Class<? extends Event> eventClass) {
		try {
			for (Field field : ReflectionUtil.FieldUtil.getExistingFields(eventClass, HandlerList.class)) {
				return (Class<? extends Event>) field.getDeclaringClass();
			}
		} catch (Exception ignored) {
		}
		return null;
	}

	public void register(Class<? extends Event> eventClass, Observer executor) {
		if (!observers.containsKey(eventClass))
			observers.put(eventClass, Collections.synchronizedSet(new HashSet<>()));

		Class<? extends Event> handlerDeclaringClass = getHandlerListDeclaringClass(eventClass);
		if (handlerDeclaringClass != null && registeredEvents.add(handlerDeclaringClass)) {
			Bukkit.getPluginManager().registerEvent(handlerDeclaringClass, this, EventPriority.HIGHEST, this, AbilityWar.getPlugin());
		}

		observers.get(eventClass).add(executor);
	}

	public void unregisterAll(Observer executor) {
		for (Class<? extends Event> eventClass : observers.keySet()) {
			observers.get(eventClass).remove(executor);
		}
	}

	@Override
	public void execute(Listener listener, Event event) {
		Class<? extends Event> eventClass = event.getClass();
		if (observers.containsKey(eventClass)) {
			for (Observer executor : observers.get(eventClass)) {
				executor.onEvent(event);
			}
		}
	}

	@Override
	public void update(GameUpdate update) {
		if (update.equals(GameUpdate.END)) {
			HandlerList.unregisterAll(this);
		}
	}

	public interface Observer {

		void onEvent(Event event);

	}

}
