package daybreak.abilitywar.utils.base.collect

import com.google.common.collect.UnmodifiableIterator
import java.util.LinkedList
import java.util.Spliterator
import java.util.function.Predicate
import kotlin.math.max

class QueueOnIterateHashSet<T> : HashSet<T>() {

	private val queue = LinkedList<Runnable>()
	private var iterations = 0

	@Deprecated("Not supported")
	@Throws(UnsupportedOperationException::class)
	override fun spliterator(): Spliterator<T> {
		throw UnsupportedOperationException()
	}

	override fun iterator(): LockedIterator {
		return LockedIterator(super.iterator())
	}

	inner class LockedIterator(private val iterator: MutableIterator<T>): UnmodifiableIterator<T>() {
		private var checked = false
		init {
			iterations++
		}
		override fun hasNext(): Boolean {
			return if (iterator.hasNext()) true else {
				if (checked) return false
				checked = true
				iterations = max(0, iterations - 1)
				if (iterations == 0) {
					while (!queue.isEmpty()) {
						queue.remove().run()
					}
				}
				false
			}
		}
		override fun next(): T {
			return iterator.next()
		}
	}

	override fun add(element: T): Boolean {
		if (iterations != 0) {
			queue.add(Runnable { super.add(element) })
			return false
		}
		return super.add(element)
	}

	override fun remove(element: T): Boolean {
		if (iterations != 0) {
			queue.add(Runnable { super.remove(element) })
			return false
		}
		return super.remove(element)
	}

	override fun clear() {
		if (iterations != 0) {
			queue.add(Runnable { super.clear() })
			return
		}
		super.clear()
	}

	override fun addAll(elements: Collection<T>): Boolean {
		if (iterations != 0) {
			queue.add(Runnable { super.addAll(elements) })
			return false
		}
		return super.addAll(elements)
	}

	override fun retainAll(elements: Collection<T>): Boolean {
		if (iterations != 0) {
			queue.add(Runnable { super.retainAll(elements) })
			return false
		}
		return super.retainAll(elements)
	}

	override fun removeIf(filter: Predicate<in T>): Boolean {
		if (iterations != 0) {
			queue.add(Runnable { super.removeIf(filter) })
			return false
		}
		return super.removeIf(filter)
	}

}