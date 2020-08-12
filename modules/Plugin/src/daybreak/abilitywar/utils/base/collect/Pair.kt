package daybreak.abilitywar.utils.base.collect

class Pair<L, R>(val left: L, val right: R) : Cloneable {

	companion object {
		@JvmStatic
		fun <L, R> of(left: L, right: R): Pair<L, R> {
			return Pair(left, right)
		}
	}

	public override fun clone(): Pair<L, R> {
		return of(left, right)
	}

	override fun equals(other: Any?): Boolean {
		if (this === other) return true
		if (javaClass != other?.javaClass) return false
		other as Pair<*, *>
		if (left != other.left) return false
		if (right != other.right) return false
		return true
	}

	override fun hashCode(): Int {
		return 31 * (left?.hashCode() ?: 0) + (right?.hashCode() ?: 0)
	}

}