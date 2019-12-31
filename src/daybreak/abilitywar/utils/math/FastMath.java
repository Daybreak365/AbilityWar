package daybreak.abilitywar.utils.math;

import daybreak.abilitywar.config.Configuration;
import daybreak.abilitywar.config.enums.ConfigNodes;

public class FastMath {

	private static final int accuracy = (int) Math.pow(10, Configuration.Settings.getInt(ConfigNodes.TRIGONOMETRIC_FUNCTION_ACCURACY));
	private static final double[] sin = new double[accuracy * 360];
	private static final double[] cos = new double[accuracy * 360];
	private static final double[] tan = new double[accuracy * 360];

	static {
		for (int i = 0; i < accuracy * 360; i++) {
			sin[i] = Math.sin(((double) i + 1) / accuracy);
			cos[i] = Math.cos(((double) i + 1) / accuracy);
			tan[i] = Math.tan(((double) i + 1) / accuracy);
		}
	}

	public static double sin(double radians) {
		if (radians > 0) return sin[Math.max((int) ((radians % 360.0) * accuracy) - 1, 0)];
		else if (radians < 0) return -sin[Math.max((int) ((radians % 360.0) * accuracy) - 1, 0)];
		else return 0;

	}

	public static double cos(double radians) {
		if (radians > 0) return cos[(int) ((radians % 360.0) * accuracy) - 1];
		else if (radians < 0) return -cos[Math.max((int) ((radians % 360.0) * accuracy) - 1, 0)];
		else return 0;

	}

	public static double tan(double radians) {
		if (radians > 0) return tan[(int) ((radians % 360.0) * accuracy) - 1];
		else if (radians < 0) return -tan[Math.max((int) ((radians % 360.0) * accuracy) - 1, 0)];
		else return 0;

	}

}
