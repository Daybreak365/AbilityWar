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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

@ModuleBase(EventManager.class)
public final class EventManager implements ListenerModule {

	private final Observers observers = new Observers(EventPriority.HIGH);

	@SuppressWarnings("unchecked")
	private Class<? extends Event> getHandlerListDeclaringClass(Class<? extends Event> eventClass) {
		for (Field field : FieldUtil.getAllFields(eventClass, HandlerList.class)) {
			return (Class<? extends Event>) field.getDeclaringClass();
		}
		return null;
	}

	public void register(final EventObserver observer) {
		observers.register(observer);
	}

	public void unregister(final EventObserver observer) {
		observers.unregister(observer);
	}

	public abstract static class EventObserver {

		protected final Class<? extends Event> eventClass;
		protected final int priority;

		public EventObserver(final Class<? extends Event> eventClass, final int priority) {
			this.eventClass = eventClass;
			this.priority = priority;
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
			final SetMultimap<Integer, EventObserver> priorityMap = getNotNull(observer.eventClass);
			final Class<? extends Event> handlerDeclaringClass = getHandlerListDeclaringClass(observer.eventClass);
			if (handlerDeclaringClass != null && registeredEvents.add(handlerDeclaringClass)) {
				Bukkit.getPluginManager().registerEvent(handlerDeclaringClass, EventManager.this, this.eventPriority, this, AbilityWar.getPlugin());
			}
			priorityMap.put(observer.priority, observer);
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
			if (observers.containsKey(eventClass)) {
				observers.iterationStarted();
				for (EventObserver value : observers.getNotNull(eventClass).values()) {
					value.onEvent(event);
				}
				observers.iterationEnded();
			}
		}

	}

}
