package daybreak.abilitywar.utils.base.math;

/**
 * Number Util
 *
 * @author Daybreak 새벽
 */
public class NumberUtil {

	private NumberUtil() {
	}

	public static boolean isInt(String s) {
		boolean isInt = true;
		try {
			Integer.parseInt(s);
		} catch (NumberFormatException e) {
			isInt = false;
		}
		return isInt;
	}

	public static double subtract(double d1, double d2) {
		return d1 > d2 ? d1 - d2 : d2 - d1;
	}

}
