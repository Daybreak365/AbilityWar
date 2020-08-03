package daybreak.abilitywar.utils.base.collect

import java.util.LinkedList

class PushingList<T>(private val maxSize: Int) : LinkedList<T>() {
	override fun add(e: T): Boolean {
		if (size >= maxSize) {
			poll()
		}
		return super.add(e)
	}

}