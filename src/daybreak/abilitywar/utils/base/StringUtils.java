package daybreak.abilitywar.utils.base;

public class StringUtils {

	private StringUtils() {
	}

	public static boolean isEmpty(String string) {
		return string == null || string.length() == 0;
	}

	public static String repeat(String string, int count) {
		if (count < 0) {
			throw new IllegalArgumentException("Can't repeat the string negative times: " + count);
		}
		if (count == 1 || string == null) {
			return string;
		}
		final byte[] stringBytes = string.getBytes();
		final int length = stringBytes.length;
		if (count == 0 || length == 0) {
			return "";
		}
		final long arraySize = length * count;
		final int intSize = (int) arraySize;
		if (arraySize != intSize) {
			throw new ArrayIndexOutOfBoundsException("Array size too large: " + arraySize);
		}
		final byte[] bytes = new byte[intSize];
		for (int r = 0; r < count; r++) {
			System.arraycopy(stringBytes, 0, bytes, r * length, length);
		}
		return new String(bytes);
	}

	public static String removeStart(String string, int amount) {
		if (amount < 0) {
			throw new IllegalArgumentException("Can't remove the negative amount of characters from string: " + amount);
		}
		if (string == null) {
			return null;
		}
		if (string.length() - amount <= 0) {
			return "";
		}
		return string.substring(amount);
	}

	public static String removeEnd(String string, int amount) {
		if (amount < 0) {
			throw new IllegalArgumentException("Can't remove the negative amount of characters from string: " + amount);
		}
		if (string == null) {
			return null;
		}
		int newLength = string.length() - amount;
		if (newLength <= 0) {
			return "";
		}
		return string.substring(0, newLength);
	}

}
