package DayBreak.AbilityWar.Utils.Data;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Advanced Array
 * @author DayBreak »õº®
 */
public class PushingArray<T> {
	
	private T[] array;
	
	@SuppressWarnings("unchecked")
	public PushingArray(Class<T> clazz, int Size) {
		this.array = (T[]) Array.newInstance(clazz, Size);
	}
	
	public void add(T t) {
		T tempLast = null;
		T Last = null;
		
		for(int i = 0; i < array.length; i++) {
			tempLast = array[i];
			if(i == 0) {
				array[i] = t;
			} else {
				array[i] = Last;
			}
			Last = tempLast;
		}
	}
	
	public List<T> toList() {
		List<T> list = new ArrayList<>(Arrays.asList(array));
		return list;
	}
	
}
