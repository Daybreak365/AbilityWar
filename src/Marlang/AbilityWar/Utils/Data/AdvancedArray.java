package Marlang.AbilityWar.Utils.Data;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class AdvancedArray<T> {
	
	T[] array;
	
	@SuppressWarnings("unchecked")
	public AdvancedArray(Class<T> clazz, Integer Size) {
		this.array = (T[]) Array.newInstance(clazz, Size);
	}
	
	public void add(T t) {
		T tempLast = null;
		T Last = null;
		
		for(Integer i = 0; i < array.length; i++) {
			tempLast = array[i];
			if(i.equals(0)) {
				array[i] = t;
			} else {
				array[i] = Last;
			}
			Last = tempLast;
		}
	}
	
	public List<T> getList() {
		List<T> list = new ArrayList<>(Arrays.asList(array));
		return list;
	}
	
}
