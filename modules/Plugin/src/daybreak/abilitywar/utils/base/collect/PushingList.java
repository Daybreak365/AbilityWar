package daybreak.abilitywar.utils.base.collect;

import java.util.LinkedList;

public class PushingList<T> extends LinkedList<T> {

	private final int size;

	public PushingList(int size) {
		this.size = size;
	}

	@Override
	public boolean add(T e) {
		if (size() >= size) {
			poll();
		}
		return super.add(e);
	}

}
