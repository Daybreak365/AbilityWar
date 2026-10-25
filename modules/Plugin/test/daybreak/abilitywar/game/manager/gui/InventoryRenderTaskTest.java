package daybreak.abilitywar.game.manager.gui;

import org.bukkit.inventory.Inventory;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;
import org.junit.Test;

import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;

public class InventoryRenderTaskTest {

	@Test
	public void rendersOnlyWhenTheServerTaskRunsAndUsesItsOwnInventory() {
		final Scheduler scheduler = new Scheduler();
		final InventoryRenderTask renderer = new InventoryRenderTask(scheduler::schedule);
		final List<Integer> slots = new ArrayList<>();
		renderer.render(inventory(slots), render -> render.setItem(3, null));
		assertTrue(slots.isEmpty());
		scheduler.tasks.get(0).run();
		assertEquals(Arrays.asList(3), slots);
	}

	@Test
	public void replacesPendingWorkEvenWhenReopeningTheSameInventory() {
		final Scheduler scheduler = new Scheduler();
		final InventoryRenderTask renderer = new InventoryRenderTask(scheduler::schedule);
		final List<Integer> slots = new ArrayList<>();
		final Inventory inventory = inventory(slots);
		renderer.render(inventory, render -> render.setItem(1, null));
		renderer.render(inventory, render -> render.setItem(2, null));
		assertTrue(scheduler.tasks.get(0).cancelled);
		// A stale callback must be harmless independently of scheduler cancellation.
		scheduler.tasks.get(0).run();
		scheduler.tasks.get(1).run();
		assertEquals(Arrays.asList(2), slots);
	}

	@Test
	public void closingThePageCancelsPendingWork() {
		final Scheduler scheduler = new Scheduler();
		final InventoryRenderTask renderer = new InventoryRenderTask(scheduler::schedule);
		final List<Integer> slots = new ArrayList<>();
		renderer.render(inventory(slots), render -> render.setItem(0, null));
		renderer.cancel();
		renderer.cancel();
		assertTrue(scheduler.tasks.get(0).cancelled);
		scheduler.tasks.get(0).run();
		assertTrue(slots.isEmpty());
	}

	@Test
	public void pageChangesDuringAnExplanationCannotWriteToEitherPage() {
		final Scheduler scheduler = new Scheduler();
		final InventoryRenderTask renderer = new InventoryRenderTask(scheduler::schedule);
		final List<Integer> oldSlots = new ArrayList<>(), newSlots = new ArrayList<>();
		final Inventory nextInventory = inventory(newSlots);
		renderer.render(inventory(oldSlots), render -> {
			renderer.render(nextInventory, next -> next.setItem(2, null));
			assertFalse(render.isCurrent());
			render.setItem(1, null);
		});
		scheduler.tasks.get(0).run();
		assertTrue(oldSlots.isEmpty());
		assertTrue(newSlots.isEmpty());
		scheduler.tasks.get(1).run();
		assertEquals(Arrays.asList(2), newSlots);
	}

	@Test
	public void anExplanationFailureInvalidatesItsRenderAndAllowsTheNextPage() {
		final Scheduler scheduler = new Scheduler();
		final InventoryRenderTask renderer = new InventoryRenderTask(scheduler::schedule);
		final List<Integer> slots = new ArrayList<>();
		final Inventory inventory = inventory(slots);
		final InventoryRenderTask.Render[] failedRender = new InventoryRenderTask.Render[1];
		renderer.render(inventory, render -> {
			failedRender[0] = render;
			throw new IllegalStateException("addon explanation failure");
		});
		try {
			scheduler.tasks.get(0).run();
			fail("Expected explanation failure");
		} catch (IllegalStateException expected) {
			assertEquals("addon explanation failure", expected.getMessage());
		}
		assertFalse(failedRender[0].isCurrent());
		failedRender[0].setItem(1, null);
		renderer.render(inventory, render -> render.setItem(2, null));
		scheduler.tasks.get(1).run();
		assertEquals(Arrays.asList(2), slots);
	}

	private static Inventory inventory(List<Integer> writtenSlots) {
		return (Inventory) Proxy.newProxyInstance(Inventory.class.getClassLoader(), new Class<?>[]{Inventory.class}, (proxy, method, args) -> {
			if (method.getName().equals("setItem")) {
				writtenSlots.add((Integer) args[0]);
				return null;
			}
			throw new AssertionError("Unexpected inventory access: " + method.getName());
		});
	}

	private static final class Scheduler {
		private final List<Task> tasks = new ArrayList<>();

		private BukkitTask schedule(Runnable runnable) {
			final Task task = new Task(runnable);
			tasks.add(task);
			return task;
		}
	}

	private static final class Task implements BukkitTask {
		private final Runnable runnable;
		private boolean cancelled;

		private Task(Runnable runnable) { this.runnable = runnable; }
		private void run() { runnable.run(); }
		@Override public int getTaskId() { return 0; }
		@Override public Plugin getOwner() { return null; }
		@Override public boolean isSync() { return true; }
		@Override public boolean isCancelled() { return cancelled; }
		@Override public void cancel() { cancelled = true; }
	}
}
