package daybreak.abilitywar.game.module;

import org.bukkit.event.Event;
import org.bukkit.event.EventException;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.EventExecutor;
import org.junit.Test;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.*;

public class EventManagerTest {

	// No static HandlerList: these synthetic events exercise internal dispatch
	// without registering anything with a running Bukkit server.
	public static class ParentEvent extends Event {
		@Override
		public HandlerList getHandlers() {
			return null;
		}
	}

	public static class ChildEvent extends ParentEvent {}

	private static EventExecutor executor(EventManager manager) throws Exception {
		Field field = EventManager.class.getDeclaredField("observers");
		field.setAccessible(true);
		return (EventExecutor) ((Map<?, ?>) field.get(manager)).get(EventPriority.NORMAL);
	}

	@Test
	public void unregisterRemovesParentAndAllChildSubscriptions() throws Exception {
		EventManager manager = new EventManager();
		AtomicInteger calls = new AtomicInteger();
		EventManager.EventObserver observer = new EventManager.EventObserver(ParentEvent.class, EventPriority.NORMAL, 0,
				Arrays.asList(ChildEvent.class, ChildEvent.class)) {
			@Override
			protected void onEvent(Event event) {
				calls.incrementAndGet();
			}
		};
		manager.register(observer);
		EventExecutor executor = executor(manager);
		executor.execute(manager, new ParentEvent());
		executor.execute(manager, new ChildEvent());
		assertEquals(2, calls.get());
		manager.unregister(observer);
		executor.execute(manager, new ParentEvent());
		executor.execute(manager, new ChildEvent());
		assertEquals(2, calls.get());
	}

	@Test
	public void exceptionStillAppliesPendingUnregistrationAndRegistration() throws Exception {
		EventManager manager = new EventManager();
		AtomicInteger calls = new AtomicInteger();
		EventManager.EventObserver replacement = new EventManager.EventObserver(ParentEvent.class, EventPriority.NORMAL, 1) {
			@Override
			protected void onEvent(Event event) {
				calls.incrementAndGet();
			}
		};
		RuntimeException failure = new RuntimeException("callback failed");
		EventManager.EventObserver original = new EventManager.EventObserver(ParentEvent.class, EventPriority.NORMAL, 0,
				Collections.singletonList(ChildEvent.class)) {
			@Override
			protected void onEvent(Event event) {
				manager.unregister(this);
				manager.register(replacement);
				throw failure;
			}
		};
		manager.register(original);
		EventExecutor executor = executor(manager);
		try {
			executor.execute(manager, new ParentEvent());
			fail("Expected callback failure");
		} catch (RuntimeException actual) {
			assertSame(failure, actual);
		}
		executor.execute(manager, new ParentEvent());
		executor.execute(manager, new ChildEvent());
		assertEquals(1, calls.get());
		manager.unregister(replacement);
		executor.execute(manager, new ParentEvent());
		assertEquals(1, calls.get());
	}

	@Test
	public void nestedDispatchPreservesPriorityAndDefersRemovalUntilOuterDispatchEnds() throws Exception {
		EventManager manager = new EventManager();
		EventExecutor executor = executor(manager);
		List<String> calls = new ArrayList<>();
		EventManager.EventObserver child = new EventManager.EventObserver(ChildEvent.class, EventPriority.NORMAL, 2) {
			@Override
			protected void onEvent(Event event) {
				calls.add("child");
			}
		};
		EventManager.EventObserver later = new EventManager.EventObserver(ParentEvent.class, EventPriority.NORMAL, 3) {
			@Override
			protected void onEvent(Event event) {
				calls.add("later");
			}
		};
		EventManager.EventObserver first = new EventManager.EventObserver(ParentEvent.class, EventPriority.NORMAL, 1) {
			@Override
			protected void onEvent(Event event) {
				calls.add("first");
				manager.unregister(child);
				manager.unregister(later);
				try {
					executor.execute(manager, new ChildEvent());
				} catch (EventException exception) {
					throw new AssertionError(exception);
				}
			}
		};
		manager.register(later);
		manager.register(first);
		manager.register(child);
		executor.execute(manager, new ParentEvent());
		assertEquals(Arrays.asList("first", "child", "later"), calls);
		calls.clear();
		executor.execute(manager, new ParentEvent());
		executor.execute(manager, new ChildEvent());
		assertEquals(Collections.singletonList("first"), calls);
	}
}
