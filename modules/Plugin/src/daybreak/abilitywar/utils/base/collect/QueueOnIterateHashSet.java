package daybreak.abilitywar.utils.base.collect;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Deque;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * Defers changes until the outermost traversal finishes. Deferred changes retain
 * their order.
 *
 * <p>For compatibility, ordinary iterators retain the original contract: they
 * release pending changes when {@code hasNext()} returns {@code false}. They must
 * be exhausted. {@link #forEach(Consumer)} provides an explicit traversal boundary
 * that also releases changes when a callback throws. Plugin-owned sets use
 * {@link ManagedIterationHashSet} for safe snapshot iteration instead.</p>
 *
 * <p>This collection is not thread-safe.</p>
 */
public class QueueOnIterateHashSet<T> extends HashSet<T> {

	private Deque<Runnable> queue = new ArrayDeque<>();
	private int iterations = 0;
	private boolean draining = false;

	@Override
	@Deprecated
	public Spliterator<T> spliterator() throws UnsupportedOperationException {
		throw new UnsupportedOperationException();
	}

	@Override
	public @NotNull Iterator<T> iterator() {
		final Iterator<T> iterator = super.iterator();
		beginIteration();
		return new Iterator<T>() {
			private boolean finished;

			@Override
			public boolean hasNext() {
				if (iterator.hasNext()) return true;
				if (!finished) {
					finished = true;
					endIteration();
				}
				return false;
			}

			@Override
			public T next() {
				return iterator.next();
			}
		};
	}

	protected final @NotNull Iterator<T> snapshotIterator() {
		final List<T> snapshot = new ArrayList<>(size());
		for (Iterator<T> iterator = super.iterator(); iterator.hasNext();) {
			snapshot.add(iterator.next());
		}
		return Collections.unmodifiableList(snapshot).iterator();
	}

	protected final void beginIteration() {
		iterations++;
	}

	protected final void endIteration() {
		if (--iterations == 0) drainQueue();
	}

	@Override
	public void forEach(Consumer<? super T> action) {
		Objects.requireNonNull(action);
		beginIteration();
		Throwable callbackFailure = null;
		try {
			for (Iterator<T> iterator = super.iterator(); iterator.hasNext();) {
				action.accept(iterator.next());
			}
		} catch (RuntimeException | Error failure) {
			callbackFailure = failure;
			throw failure;
		} finally {
			try {
				endIteration();
			} catch (RuntimeException | Error failure) {
				if (callbackFailure == null) throw failure;
				if (callbackFailure != failure) callbackFailure.addSuppressed(failure);
			}
		}
	}

	private void drainQueue() {
		if (draining) return;
		draining = true;
		Throwable firstFailure = null;
		try {
			while (!queue.isEmpty()) {
				try {
					queue.remove().run();
				} catch (RuntimeException | Error failure) {
					if (firstFailure == null) firstFailure = failure;
					else if (firstFailure != failure) firstFailure.addSuppressed(failure);
				}
			}
		} finally {
			draining = false;
		}
		if (firstFailure instanceof RuntimeException) throw (RuntimeException) firstFailure;
		if (firstFailure instanceof Error) throw (Error) firstFailure;
	}

	@Override
	@SuppressWarnings("unchecked")
	public Object clone() {
		final QueueOnIterateHashSet<T> clone = (QueueOnIterateHashSet<T>) super.clone();
		clone.queue = new ArrayDeque<>();
		clone.iterations = 0;
		clone.draining = false;
		return clone;
	}

	@Override
	public boolean add(T element) {
		if (iterations != 0) {
			queue.add(() -> add0(element));
			return false;
		}
		return super.add(element);
	}

	private void add0(T element) {
		super.add(element);
	}

	@Override
	public boolean remove(Object element) {
		if (iterations != 0) {
			queue.add(() -> remove0(element));
			return false;
		}
		return super.remove(element);
	}

	private void remove0(Object element) {
		super.remove(element);
	}

	@Override
	public void clear() {
		if (iterations != 0) {
			queue.add(this::clear0);
			return;
		}
		super.clear();
	}

	private void clear0() {
		super.clear();
	}

	@Override
	public boolean addAll(@NotNull Collection<? extends T> elements) {
		Objects.requireNonNull(elements);
		if (iterations != 0) {
			queue.add(() -> addAll0(elements));
			return false;
		}
		return addAll0(elements);
	}

	private boolean addAll0(Collection<? extends T> elements) {
		boolean changed = false;
		for (T element : elements) {
			changed |= super.add(element);
		}
		return changed;
	}

	@Override
	public boolean removeAll(@NotNull Collection<?> elements) {
		Objects.requireNonNull(elements);
		if (iterations != 0) {
			queue.add(() -> removeAll0(elements));
			return false;
		}
		return removeAll0(elements);
	}

	private boolean removeAll0(Collection<?> elements) {
		return removeIf0(elements::contains);
	}

	@Override
	public boolean retainAll(@NotNull Collection<?> elements) {
		Objects.requireNonNull(elements);
		if (iterations != 0) {
			queue.add(() -> retainAll0(elements));
			return false;
		}
		return retainAll0(elements);
	}

	private boolean retainAll0(Collection<?> elements) {
		return removeIf0(element -> !elements.contains(element));
	}

	@Override
	public boolean removeIf(Predicate<? super T> filter) {
		Objects.requireNonNull(filter);
		if (iterations != 0) {
			queue.add(() -> removeIf0(filter));
			return false;
		}
		return removeIf0(filter);
	}

	private boolean removeIf0(Predicate<? super T> filter) {
		boolean changed = false;
		for (Iterator<T> iterator = super.iterator(); iterator.hasNext();) {
			if (filter.test(iterator.next())) {
				iterator.remove();
				changed = true;
			}
		}
		return changed;
	}

}
