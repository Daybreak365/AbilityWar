package daybreak.abilitywar.utils.base;

public class Hashes {

	private static final int PRIME = 31;

	private Hashes() {
	}

	public static int hashCode(int a, int b) {
		int hashCode = 1;
		hashCode = PRIME * hashCode + a;
		hashCode = PRIME * hashCode + b;
		return hashCode;
	}

	public static int hashCode(int... ints) {
		int hashCode = 1;
		for (int i : ints) {
			hashCode = PRIME * hashCode + i;
		}
		return hashCode;
	}

}
