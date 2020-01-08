package daybreak.abilitywar.utils.database;

import java.util.LinkedList;

/**
 * Advanced Array
 *
 * @author Daybreak 새벽
 */
public class PushingList<T> extends LinkedList<T> {

	private final int size;

	public PushingList(int size) {
		this.size = size;
	}

	public boolean add(T e) {
		if (size() >= size) {
			poll();
		}
		return super.add(e);
	}

}
