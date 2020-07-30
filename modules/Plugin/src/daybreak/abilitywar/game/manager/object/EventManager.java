package daybreak.abilitywar.game.manager.object;

import daybreak.abilitywar.AbilityWar;
import daybreak.abilitywar.ability.SubscribeEvent;
import daybreak.abilitywar.game.AbstractGame;
import daybreak.abilitywar.game.AbstractGame.GameUpdate;
import daybreak.abilitywar.utils.base.reflect.ReflectionUtil.FieldUtil;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.TreeMap;
import org.bukkit.Bukkit;
import org.bukkit.event.Event;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.plugin.EventExecutor;

public class EventManager implements Listener, EventExecutor, AbstractGame.Observer {

	public EventManager(AbstractGame game) {
		game.attachObserver(this);
		Bukkit.getPluginManager().registerEvents(this, AbilityWar.getPlugin());
	}

	private final HashMap<Class<? extends Event>, TreeMap<Integer, HashSet<EventObserver>>> observers = new HashMap<>();
	private final Set<Class<? extends Event>> registeredEvents = new HashSet<>();

	@SuppressWarnings("unchecked")
	private Class<? extends Event> getHandlerListDeclaringClass(Class<? extends Event> eventClass) {
		for (Field field : FieldUtil.getAllFields(eventClass, HandlerList.class)) {
			return (Class<? extends Event>) field.getDeclaringClass();
		}
		return null;
	}

	public void register(EventObserver observer) {
		if (iterationDepth != 0) {
			toRegister.add(observer);
			return;
		}
		final TreeMap<Integer, HashSet<EventObserver>> observerMap;
		if (!observers.containsKey(observer.eventClass)) {
			observerMap = new TreeMap<>();
			observers.put(observer.eventClass, observerMap);
		} else observerMap = observers.get(observer.eventClass);

		final Class<? extends Event> handlerDeclaringClass = getHandlerListDeclaringClass(observer.eventClass);
		if (handlerDeclaringClass != null && registeredEvents.add(handlerDeclaringClass)) {
			Bukkit.getPluginManager().registerEvent(handlerDeclaringClass, this, EventPriority.HIGH, this, AbilityWar.getPlugin());
		}

		final HashSet<EventObserver> observers;
		if (observerMap.get(observer.subscriber.priority()) == null) {
			observers = new HashSet<>();
			observerMap.put(observer.subscriber.priority(), observers);
		} else observers = observerMap.get(observer.subscriber.priority());
		observers.add(observer);
	}

	public void unregister(EventObserver observer) {
		if (iterationDepth != 0) {
			toUnregister.add(observer);
			return;
		}
		if (observers.containsKey(observer.eventClass)) {
			final TreeMap<Integer, HashSet<EventObserver>> treeMap = observers.get(observer.eventClass);
			treeMap.entrySet().removeIf(entry -> entry.getValue().remove(observer) && entry.getValue().size() == 0);
		}
	}

	private int iterationDepth = 0;
	private final List<EventObserver> toRegister = new LinkedList<>(), toUnregister = new LinkedList<>();

	@Override
	public void execute(Listener listener, Event event) {
		final Class<? extends Event> eventClass = event.getClass();
		if (observers.containsKey(eventClass)) {
			iterationDepth++;
			for (HashSet<EventObserver> value : observers.get(eventClass).values()) {
				for (EventObserver observer : value) {
					observer.onEvent(event);
				}
			}
			iterationDepth = Math.max(0, iterationDepth - 1);
			if (iterationDepth == 0) {
				final TreeMap<Integer, HashSet<EventObserver>> treeMap = observers.get(eventClass);
				treeMap.entrySet().removeIf(entry -> entry.getValue().removeAll(toUnregister) && entry.getValue().size() == 0);
				toUnregister.clear();
				for (final Iterator<EventObserver> iterator = toRegister.iterator(); iterator.hasNext(); ) {
					register(iterator.next());
					iterator.remove();
				}
			}
		}
	}

	@Override
	public void update(GameUpdate update) {
		if (update.equals(GameUpdate.END)) {
			HandlerList.unregisterAll(this);
		}
	}

	public abstract static class EventObserver {

		protected final Class<? extends Event> eventClass;
		protected final SubscribeEvent subscriber;
		protected final Method method;

		public EventObserver(Class<? extends Event> eventClass, SubscribeEvent subscriber, Method method) {
			this.eventClass = eventClass;
			this.subscriber = subscriber;
			this.method = method;
		}

		protected abstract void onEvent(Event event);

	}

}
