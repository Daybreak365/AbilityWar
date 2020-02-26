package daybreak.abilitywar.utils.math;

public class FastMath {

	private static final int accuracy = (int) Math.pow(10, 3);
	private static final double[] sin = new double[accuracy * 360];
	private static final double[] cos = new double[accuracy * 360];
	private static final double[] tan = new double[accuracy * 180];

	static {
		for (int i = 0; i < accuracy * 360; i++) {
			sin[i] = Math.sin(((double) i + 1) / accuracy);
			cos[i] = Math.cos(((double) i + 1) / accuracy);
		}
		for (int i = 0; i < accuracy * 180; i++) {
			tan[i] = Math.tan(((double) i + 1) / accuracy);
		}
	}

	public static double sin(double radians) {
		if (radians > 0) return sin[Math.max((int) ((radians % 360.0) * accuracy) - 1, 0)];
		else if (radians < 0) return -sin[Math.max((int) ((radians % 360.0) * accuracy) - 1, 0)];
		else return 0;

	}

	public static double cos(double radians) {
		if (radians > 0) return cos[Math.max((int) ((radians % 360.0) * accuracy) - 1, 0)];
		else if (radians < 0) return -cos[Math.max((int) ((radians % 360.0) * accuracy) - 1, 0)];
		else return 0;

	}

	public static double tan(double radians) {
		if (radians > 0) return tan[Math.max((int) ((radians % 180.0) * accuracy) - 1, 0)];
		else if (radians < 0) return -tan[Math.max((int) ((radians % 180.0) * accuracy) - 1, 0)];
		else return 0;

	}

	public static int gcd(int a, int b) {
		if (a == 0) return b;
		if (b == 0) return a;
		int k;
		for (k = 0; ((a | b) & 1) == 0; ++k) {
			a >>= 1;
			b >>= 1;
		}
		while ((a & 1) == 0) a >>= 1;
		do {
			while ((b & 1) == 0) b >>= 1;
			if (a > b) {
				int temp = a;
				a = b;
				b = temp;
			}
			b = (b - a);
		} while (b != 0);
		return a << k;
	}

}
