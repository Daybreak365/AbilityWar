package daybreak.abilitywar.game.manager.gui;

import org.bukkit.Bukkit;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;

import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Resolves a page after its placeholders are displayed. All methods, including
 * the supplied renderer, run on the server thread: explanations may access
 * settings or addon fields as well as Bukkit inventory metadata.
 */
public final class InventoryRenderTask {

	private final Function<Runnable, BukkitTask> scheduler;
	private BukkitTask task;
	private Render current;

	public InventoryRenderTask(Plugin plugin) {
		this(runnable -> Bukkit.getScheduler().runTask(plugin, runnable));
	}

	InventoryRenderTask(Function<Runnable, BukkitTask> scheduler) {
		this.scheduler = scheduler;
	}

	public void render(Inventory inventory, Consumer<Render> renderer) {
		cancel();
		final Render render = new Render(inventory);
		this.current = render;
		this.task = scheduler.apply(() -> {
			if (!render.isCurrent()) return;
			this.task = null;
			try {
				renderer.accept(render);
			} finally {
				if (render.isCurrent()) this.current = null;
			}
		});
	}

	public void cancel() {
		this.current = null;
		if (task != null) {
			task.cancel();
			this.task = null;
		}
	}

	public final class Render {
		private final Inventory inventory;

		private Render(Inventory inventory) {
			this.inventory = inventory;
		}

		public boolean isCurrent() {
			return current == this;
		}

		public void setItem(int slot, ItemStack item) {
			// An addon explanation can open another page while an item is built.
			if (isCurrent()) inventory.setItem(slot, item);
		}
	}
}
