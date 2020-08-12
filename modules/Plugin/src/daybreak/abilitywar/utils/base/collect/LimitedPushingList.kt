package daybreak.abilitywar.utils.base.collect

import java.util.LinkedList

class LimitedPushingList<T>(private val maxSize: Int) : LinkedList<T>() {
	override fun add(element: T): Boolean {
		if (size >= maxSize) {
			poll()
		}
		return super.add(element)
	}

}