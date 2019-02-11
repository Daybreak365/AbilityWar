package Marlang.AbilityWar.Utils;

/**
 * Validate
 * @author _Marlang ¸»¶û
 */
public class Validate {

	public static void NotNull(Object... objects) throws IllegalArgumentException {
		if(objects != null) {
			for(Object o : objects) {
				if(o == null) {
					throw new IllegalArgumentException("Null");
				}
			}
		} else {
			throw new IllegalArgumentException();
		}
	}
	
}
