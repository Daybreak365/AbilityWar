package daybreak.abilitywar.utils.base.collect;

public class Pair<Left, Right> implements Cloneable {

	public static <Left, Right> Pair<Left, Right> of(Left left, Right right) {
		return new Pair<>(left, right);
	}

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

	@Override
	public Pair<Left, Right> clone() {
		return of(left, right);
	}

}
