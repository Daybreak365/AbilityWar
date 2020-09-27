package daybreak.abilitywar.utils.base.collect;

import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public class Pair<L, R> implements Cloneable {

	public static <L, R> Pair<L, R> of(final L left, final R right) {
		return new Pair<>(left, right);
	}

	private final L left;
	private final R right;

	public Pair(@Nullable final L left, @Nullable final R right) {
		this.left = left;
		this.right = right;
	}

	public L getLeft() {
		return left;
	}

	public R getRight() {
		return right;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		final Pair<?, ?> pair = (Pair<?, ?>) o;
		if (!Objects.equals(left, pair.left)) return false;
		return Objects.equals(right, pair.right);
	}

	@Override
	public int hashCode() {
		int result = left != null ? left.hashCode() : 0;
		result = 31 * result + (right != null ? right.hashCode() : 0);
		return result;
	}
}
