package daybreak.abilitywar.utils.base.collect;

import java.util.Collection;
import java.util.LinkedList;

public class LimitedPushingList<T> extends LinkedList<T> {
	private final int maxSize;

	public LimitedPushingList(final int maxSize) {
		this.maxSize = maxSize;
	}

	@Override
	@Deprecated
	public void addFirst(T t) {
		throw new UnsupportedOperationException();
	}

	@Override
	@Deprecated
	public void addLast(T t) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean add(T t) {
		if (size() >= maxSize) poll();
		return super.add(t);
	}

	@Override
	@Deprecated
	public boolean addAll(Collection<? extends T> c) {
		throw new UnsupportedOperationException();
	}

	@Override
	@Deprecated
	public boolean addAll(int index, Collection<? extends T> c) {
		throw new UnsupportedOperationException();
	}

	@Override
	@Deprecated
	public void add(int index, T element) {
		throw new UnsupportedOperationException();
	}

}
