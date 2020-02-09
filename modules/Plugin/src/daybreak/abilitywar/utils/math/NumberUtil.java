package daybreak.abilitywar.utils.math;

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
		} catch (Exception e) {
			isInt = false;
		}
		return isInt;
	}

	public static double subtract(double d1, double d2) {
		return d1 > d2 ? d1 - d2 : d2 - d1;
	}

	public static String parseTimeString(int seconds) {
		int hour = seconds / 3600;
		seconds -= hour * 3600;
		int minute = seconds / 60;
		seconds -= minute * 60;

		return (hour != 0 ? hour + "시간 " : "") + (minute != 0 ? minute + "분 " : "") + (seconds >= 0 ? seconds + "초" : "");
	}

	public static int[] parseTime(int seconds) {
		int[] time = new int[2];
		time[0] = seconds / 60;
		seconds -= time[0] * 60;
		time[1] = seconds;

		return time;
	}

	public static NumberStatus getNumberStatus(Number Number) {
		if (Number.doubleValue() > 0.0) {
			return NumberStatus.Plus;
		} else if (Number.doubleValue() < 0.0) {
			return NumberStatus.Minus;
		} else {
			return NumberStatus.Zero;
		}
	}

	public enum NumberStatus {

		Minus, Zero, Plus;

		public boolean isMinus() {
			return equals(NumberStatus.Minus);
		}

		public boolean isZero() {
			return equals(NumberStatus.Zero);
		}

		public boolean isPlus() {
			return equals(NumberStatus.Plus);
		}

	}

}
