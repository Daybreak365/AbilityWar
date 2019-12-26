package daybreak.abilitywar.utils.database.collections;

/**
 * 두 개의 자료를 동시에 가지고 있는 구조
 * @param <L>	첫 번째 자료
 * @param <R>	두 번째 자료
 */
public class Pair<L, R> {

	private final L left;
	private final R right;

	public Pair(L left, R right) {
		this.left = left;
		this.right = right;
	}

	public L getLeft() {
		return left;
	}

	public R getRight() {
		return right;
	}

	public static <L, R> Pair<L, R> of(L left, R right) {
		return new Pair<>(left, right);
	}

}
