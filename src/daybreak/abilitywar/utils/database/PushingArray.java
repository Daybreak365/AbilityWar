package daybreak.abilitywar.utils.database;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * Advanced Array
 *
 * @author Daybreak 새벽
 */
public class PushingArray<T> {
	// TODO: 최적화
	private final T[] array;

	@SuppressWarnings("unchecked")
	public PushingArray(Class<T> clazz, int size) {
		this.array = (T[]) Array.newInstance(clazz, size);
	}

	public void add(T t) {
		T tempLast;
		T last = null;

		for (int i = 0; i < array.length; i++) {
			tempLast = array[i];
			if (i == 0) {
				array[i] = t;
			} else {
				array[i] = last;
			}
			last = tempLast;
		}
	}

	public ArrayList<T> toList() {
		return new ArrayList<>(Arrays.asList(array));
	}

}
