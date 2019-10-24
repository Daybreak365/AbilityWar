package daybreak.abilitywar.utils.math;

/**
 * Number Util
 * @author DayBreak 새벽
 */
public class NumberUtil {

	private NumberUtil() {}
	
	public static boolean isInt(String s) {
		boolean isInt = true;
		try {
			Integer.parseInt(s);
		} catch (Exception e) {
			isInt = false;
		}
		return isInt;
	}

	public static double Subtract(Double one, Double two) {
		if (one > two) {
			return one - two;
		} else {
			return two - one;
		}
	}

	public static String parseTimeString(Integer Second) {
		int Hour = Second / 3600;
		Second -= Hour * 3600;
		int Minute = Second / 60;
		Second -= Minute * 60;

		return (Hour != 0 ? Hour + "시간 " : "") + (Minute != 0 ? Minute + "분 " : "") + (Second >= 0 ? Second + "초" : "");
	}

	public static NumberStatus getNumberStatus(Number Number) {
		if (Number.intValue() > 0) {
			return NumberStatus.Plus;
		} else if (Number.intValue() < 0) {
			return NumberStatus.Minus;
		} else {
			return NumberStatus.Zero;
		}
	}

	public enum NumberStatus {

		Minus, Zero, Plus;

		public boolean isMinus() {
			return this.equals(NumberStatus.Minus);
		}

		public boolean isZero() {
			return this.equals(NumberStatus.Zero);
		}

		public boolean isPlus() {
			return this.equals(NumberStatus.Plus);
		}

	}

}
