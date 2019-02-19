package Marlang.AbilityWar.Utils.Math;

import java.util.Random;

/**
 * Math Util
 * 
 * @author _Marlang 말랑
 */
public class NumberUtil {

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
		Integer Hour = Second / 3600;
		Second -= Hour * 3600;
		Integer Minute = Second / 60;
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

	public static enum NumberStatus {

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

	public static <T extends Enum<?>> T randomEnum(Class<T> clazz) {
		Random r = new Random();
		Integer X = r.nextInt(clazz.getEnumConstants().length);
		return clazz.getEnumConstants()[X];
	}
}
