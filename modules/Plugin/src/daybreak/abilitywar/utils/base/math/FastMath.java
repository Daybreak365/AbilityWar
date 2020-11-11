package daybreak.abilitywar.utils.base.math;

import daybreak.abilitywar.utils.base.io.FileUtil;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class FastMath {

	private FastMath() {}

	private static final String CACHE_PATH = "cache/math.cache";
	private static final int accuracy = (int) Math.pow(10, 5);
	private static final int length = (int) (6.283185307179586476925286766559 * accuracy);
	private static final double[] sin;

	static {
		final double[] cache = loadCache();
		if (cache != null) {
			sin = cache;
		} else {
			sin = new double[length];
			for (int i = 0; i < length; i++) {
				sin[i] = Math.sin(((double) i + 1) / accuracy);
			}
			final File file = FileUtil.newFile(CACHE_PATH);
			try (final ObjectOutputStream stream = new ObjectOutputStream(new FileOutputStream(file))) {
				stream.writeObject(sin);
				stream.flush();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	private static double[] loadCache() {
		final File file = FileUtil.getFile(CACHE_PATH);
		if (file.exists()) {
			try (final ObjectInputStream stream = new ObjectInputStream(new FileInputStream(file))) {
				final double[] cache = (double[]) stream.readObject();
				return cache.length == length ? cache : null;
			} catch (Exception ignored) {}
		}
		return null;
	}

	public static double sin(final double a) {
		final double radians = Math.abs(a) % 6.283185307179586476925286766559;
		if (a > 0) return sin[Math.max((int) (radians * accuracy) - 1, 0)];
		else if (a < 0) return -sin[Math.max((int) (radians * accuracy) - 1, 0)];
		else return 0;
	}

	public static double cos(double radians) {
		return sin(radians + 1.57079632679489661923);
	}

	public static double tan(double radians) {
		return sin(radians) / cos(radians);
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

	public static double square(double a) {
		return a * a;
	}

}
