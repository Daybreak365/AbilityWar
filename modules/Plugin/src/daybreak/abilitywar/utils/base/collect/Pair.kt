package daybreak.abilitywar.utils.base.collect

class Pair<L, R>(val left: L, val right: R) : Cloneable {

	public override fun clone(): Pair<L, R> {
		return of(left, right)
	}

	companion object {
		@JvmStatic
		fun <L, R> of(left: L, right: R): Pair<L, R> {
			return Pair(left, right)
		}
	}

}