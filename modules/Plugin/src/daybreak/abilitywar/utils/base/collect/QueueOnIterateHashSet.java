package daybreak.abilitywar.utils.base.collect;

import com.google.common.collect.UnmodifiableIterator;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Deque;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Spliterator;
import java.util.function.Predicate;

public class QueueOnIterateHashSet<T> extends HashSet<T> {

	private Deque<Runnable> queue = new LinkedList<>();
	private int iterations = 0;

	@Override
	@Deprecated
	public Spliterator<T> spliterator() throws UnsupportedOperationException {
		throw new UnsupportedOperationException();
	}

	@Override
	public @NotNull Iterator<T> iterator() {
		return new LockedIterator(super.iterator());
	}

	private class LockedIterator extends UnmodifiableIterator<T> {
		private boolean checked = false;

		private final Iterator<T> iterator;

		private LockedIterator(final Iterator<T> iterator) {
			this.iterator = iterator;
			iterations++;
		}

		@Override
		public boolean hasNext() {
			if (iterator.hasNext()) {
				return true;
			} else {
				if (checked) return false;
				checked = true;
				iterations = Math.max(0, iterations - 1);
				if (iterations == 0) {
					while (!queue.isEmpty()) {
						queue.remove().run();
					}
				}
				return false;
			}
		}

		@Override
		public T next() {
			return iterator.next();
		}
	}

	@Override
	public boolean add(T element) {
		if (iterations != 0) {
			queue.add(new Runnable() {
				@Override
				public void run() {
					add0(element);
				}
			});
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
			queue.add(new Runnable() {
				@Override
				public void run() {
					remove0(element);
				}
			});
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
			queue.add(new Runnable() {
				@Override
				public void run() {
					clear0();
				}
			});
		}
		super.clear();
	}

	private void clear0() {
		super.clear();
	}

	@Override
	public boolean addAll(@NotNull Collection<? extends T> elements) {
		if (iterations != 0) {
			queue.add(new Runnable() {
				@Override
				public void run() {
					addAll0(elements);
				}
			});
			return false;
		}
		return super.addAll(elements);
	}

	private void addAll0(Collection<? extends T> elements) {
		super.addAll(elements);
	}

	@Override
	public boolean retainAll(@NotNull Collection<?> elements) {
		if (iterations != 0) {
			queue.add(new Runnable() {
				@Override
				public void run() {
					retainAll0(elements);
				}
			});
			return false;
		}
		return super.retainAll(elements);
	}

	private void retainAll0(Collection<?> elements) {
		super.retainAll(elements);
	}

	@Override
	public boolean removeIf(Predicate<? super T> filter) {
		if (iterations != 0) {
			queue.add(new Runnable() {
				@Override
				public void run() {
					removeIf0(filter);
				}
			});
			return false;
		}
		return super.removeIf(filter);
	}

	private void removeIf0(Predicate<? super T> filter) {
		super.removeIf(filter);
	}

}
