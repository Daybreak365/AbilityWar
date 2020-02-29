package daybreak.abilitywar.utils.base.collect;

/**
 * 두 개의 자료를 동시에 가지고 있는 구조
 *
 * @param <Left>  첫 번째 객체
 * @param <Right> 두 번째 객체
 */
public class Pair<Left, Right> {

	private final Left left;
	private final Right right;

	public Pair(Left left, Right right) {
		this.left = left;
		this.right = right;
	}

	public Left getLeft() {
		return left;
	}

	public Right getRight() {
		return right;
	}

	public static <Left, Right> Pair<Left, Right> of(Left left, Right right) {
		return new Pair<>(left, right);
	}

}
