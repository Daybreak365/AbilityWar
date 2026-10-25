package daybreak.abilitywar.utils.base.collect;

import org.jetbrains.annotations.NotNull;

import java.util.Iterator;
import java.util.function.Consumer;

/**
 * Internal timer/observer set with unmodifiable snapshot iterators. Abandoning an
 * iterator cannot retain later changes. {@link #forEach(Consumer)} still defers
 * changes until the outermost callback traversal finishes, including on failure.
 */
public final class ManagedIterationHashSet<T> extends QueueOnIterateHashSet<T> {

	@Override
	public @NotNull Iterator<T> iterator() {
		return snapshotIterator();
	}
}
