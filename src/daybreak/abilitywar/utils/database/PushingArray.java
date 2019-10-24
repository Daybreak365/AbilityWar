package daybreak.abilitywar.utils.database;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/**
 * Advanced Array
 * @author DayBreak 새벽
 */
public class PushingArray<T> {
	// TODO: 최적화
	private final T[] array;
	
	@SuppressWarnings("unchecked")
	public PushingArray(Class<T> clazz, int Size) {
		this.array = (T[]) Array.newInstance(clazz, Size);
	}
	
	public void add(T t) {
		T tempLast;
		T last = null;
		
		for(int i = 0; i < array.length; i++) {
			tempLast = array[i];
			if(i == 0) {
				array[i] = t;
			} else {
				array[i] = last;
			}
			last = tempLast;
		}
	}
	
	public List<T> toList() {
		return new ArrayList<>(Arrays.asList(array));
	}
	
}
