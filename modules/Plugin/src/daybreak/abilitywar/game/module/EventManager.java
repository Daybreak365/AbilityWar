package daybreak.abilitywar.game.module;

import com.google.common.collect.Multimap;
import com.google.common.collect.MultimapBuilder;
import com.google.common.collect.SetMultimap;
import daybreak.abilitywar.AbilityWar;
import daybreak.abilitywar.utils.base.reflect.ReflectionUtil.FieldUtil;
import org.bukkit.Bukkit;
import org.bukkit.event.Event;
import org.bukkit.event.EventException;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.plugin.EventExecutor;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@ModuleBase(EventManager.class)
public final class EventManager implements ListenerModule {

	private final Map<EventPriority, Observers> observers = new EnumMap<>(EventPriority.class);

	public EventManager() {
		for (EventPriority priority : EventPriority.values()) {
			observers.put(priority, new Observers(priority));
		}
	}

	@SuppressWarnings("unchecked")
	private static Class<? extends Event> getHandlerListDeclaringClass(Class<? extends Event> eventClass) {
		for (Field field : FieldUtil.getAllFields(eventClass, HandlerList.class)) {
			return (Class<? extends Event>) field.getDeclaringClass();
		}
		return null;
	}

	public void register(final EventObserver observer) {
		observers.get(observer.eventPriority).register(observer);
	}

	public void unregister(final EventObserver observer) {
		observers.get(observer.eventPriority).unregister(observer);
	}

	public abstract static class EventObserver {

		protected final Class<? extends Event> eventClass;
		protected final EventPriority eventPriority;
		protected final int priority;
		protected final List<Class<? extends Event>> childs;

		public EventObserver(final Class<? extends Event> eventClass, final EventPriority eventPriority, final int priority, final List<Class<? extends Event>> childs) {
			this.eventClass = eventClass;
			this.eventPriority = eventPriority;
			this.priority = priority;
			this.childs = childs.isEmpty() ? childs : childs.stream().filter(eventClass::isAssignableFrom).distinct().collect(Collectors.toList());
		}

		public EventObserver(final Class<? extends Event> eventClass, final EventPriority eventPriority, final int priority) {
			this(eventClass, eventPriority, priority, Collections.emptyList());
		}

		protected abstract void onEvent(Event event);

	}

	private class Observers extends HashMap<Class<? extends Event>, SetMultimap<Integer, EventObserver>> implements EventExecutor {

		private final EventPriority eventPriority;

		private Observers(final EventPriority eventPriority) {
			this.eventPriority = eventPriority;
		}

		private int iterations = 0;
		private final List<EventObserver> toRegister = new LinkedList<>(), toUnregister = new LinkedList<>();
		private final Set<Class<? extends Event>> registeredEvents = new HashSet<>();

		@NotNull
		public SetMultimap<Integer, EventObserver> getNotNull(final Class<? extends Event> key) {
			final SetMultimap<Integer, EventObserver> got = super.get(key);
			if (got == null) {
				final SetMultimap<Integer, EventObserver> multimap = MultimapBuilder.treeKeys().hashSetValues().build();
				super.put(key, multimap);
				return multimap;
			} else {
				return got;
			}
		}

		private void iterationStarted() {
			iterations++;
		}

		private void iterationEnded() {
			if ((iterations = Math.max(0, iterations - 1)) == 0) {
				for (final Iterator<EventObserver> iterator = toUnregister.iterator(); iterator.hasNext();) {
					unregister0(iterator.next());
					iterator.remove();
				}
				for (final Iterator<EventObserver> iterator = toRegister.iterator(); iterator.hasNext();) {
					register0(iterator.next());
					iterator.remove();
				}
			}
		}

		private void register(final EventObserver observer) {
			if (iterations != 0) {
				toRegister.add(observer);
				return;
			}
			register0(observer);
		}

		private void register0(final EventObserver observer) {
			register1(observer.eventClass, observer);
			for (Class<? extends Event> child : observer.childs) {
				register1(child, observer);
			}
		}

		private void register1(final Class<? extends Event> eventClass, final EventObserver observer) {
			final Class<? extends Event> handlerDeclaringClass = getHandlerListDeclaringClass(eventClass);
			if (handlerDeclaringClass != null && registeredEvents.add(handlerDeclaringClass)) {
				Bukkit.getPluginManager().registerEvent(handlerDeclaringClass, EventManager.this, this.eventPriority, this, AbilityWar.getPlugin());
			}
			getNotNull(eventClass).put(observer.priority, observer);
		}

		private void unregister(final EventObserver observer) {
			if (iterations != 0) {
				toUnregister.add(observer);
				return;
			}
			unregister0(observer);
		}

		private void unregister0(final EventObserver observer) {
			final Multimap<Integer, EventObserver> priorityMap = super.get(observer.eventClass);
			if (priorityMap != null) {
				priorityMap.remove(observer.priority, observer);
			}
		}

		@Override
		public void execute(@NotNull Listener listener, @NotNull Event event) throws EventException {
			final Class<? extends Event> eventClass = event.getClass();
			if (containsKey(eventClass)) {
				iterationStarted();
				for (EventObserver value : getNotNull(eventClass).values()) {
					value.onEvent(event);
				}
				iterationEnded();
			}
		}

	}

}
