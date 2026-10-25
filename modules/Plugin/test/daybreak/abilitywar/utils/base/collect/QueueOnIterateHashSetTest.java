package daybreak.abilitywar.utils.base.collect;

import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;

import static org.junit.Assert.*;

public class QueueOnIterateHashSetTest {

	@Test
	public void legacyIteratorDefersChangesUntilTerminalHasNext() {
		QueueOnIterateHashSet<Integer> set = new QueueOnIterateHashSet<>();
		set.add(1);
		Iterator<Integer> iterator = set.iterator();
		assertEquals(Integer.valueOf(1), iterator.next());
		assertFalse(set.add(2));
		assertFalse(set.remove(1));
		assertEquals(1, set.size());
		assertTrue(set.contains(1));
		assertFalse(set.contains(2));
		assertFalse(iterator.hasNext());
		assertEquals(Collections.singleton(2), set);
		assertFalse(iterator.hasNext());
		assertTrue(set.add(3));
	}

	@Test
	public void nestedLegacyIteratorsReleaseOnlyAfterBothAreExhausted() {
		QueueOnIterateHashSet<Integer> set = new QueueOnIterateHashSet<>();
		set.add(1);
		Iterator<Integer> first = set.iterator();
		Iterator<Integer> second = set.iterator();
		set.add(2);
		first.next();
		assertFalse(first.hasNext());
		assertFalse(set.contains(2));
		second.next();
		assertFalse(second.hasNext());
		assertEquals(new HashSet<>(Arrays.asList(1, 2)), set);
	}

	@Test
	public void managedTraversalOfLegacySetReleasesChangesAfterCallbackFailure() {
		QueueOnIterateHashSet<Integer> set = new QueueOnIterateHashSet<>();
		set.add(1);
		RuntimeException failure = new RuntimeException("callback failed");
		try {
			set.forEach(value -> {
				set.clear();
				set.add(2);
				throw failure;
			});
			fail("Expected callback failure");
		} catch (RuntimeException actual) {
			assertSame(failure, actual);
		}
		assertEquals(Collections.singleton(2), set);
		assertTrue(set.add(3));
	}

	@Test(expected = UnsupportedOperationException.class)
	public void legacyIteratorRemainsUnmodifiable() {
		QueueOnIterateHashSet<Integer> set = new QueueOnIterateHashSet<>();
		set.add(1);
		Iterator<Integer> iterator = set.iterator();
		iterator.next();
		iterator.remove();
	}
}
