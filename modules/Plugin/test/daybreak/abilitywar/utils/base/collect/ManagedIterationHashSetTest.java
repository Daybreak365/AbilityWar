package daybreak.abilitywar.utils.base.collect;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.*;

public class ManagedIterationHashSetTest {

	private static ManagedIterationHashSet<Integer> set(Integer... values) {
		ManagedIterationHashSet<Integer> set = new ManagedIterationHashSet<>();
		set.addAll(Arrays.asList(values));
		return set;
	}

	@Test
	public void abandonedIteratorDoesNotRetainChanges() {
		ManagedIterationHashSet<Integer> set = set(1, 2);
		set.iterator();
		for (Integer ignored : set) break;
		assertTrue(set.add(3));
		assertTrue(set.remove(1));
		assertEquals(new HashSet<>(Arrays.asList(2, 3)), set);
	}

	@Test
	public void snapshotSurvivesMutationWithoutChangingItsVisitedElements() {
		ManagedIterationHashSet<Integer> set = set(1, 2, 3);
		List<Integer> original = new ArrayList<>(set);
		Iterator<Integer> iterator = set.iterator();
		set.clear();
		set.add(4);
		List<Integer> visited = new ArrayList<>();
		iterator.forEachRemaining(visited::add);
		assertEquals(original, visited);
		assertEquals(Collections.singleton(4), set);
	}

	@Test(expected = UnsupportedOperationException.class)
	public void snapshotIteratorRemainsUnmodifiable() {
		Iterator<Integer> iterator = set(1).iterator();
		iterator.next();
		iterator.remove();
	}

	@Test
	public void managedTraversalDefersClearAndRetainsMutationOrder() {
		ManagedIterationHashSet<Integer> set = set(1, 2);
		List<Integer> expected = new ArrayList<>(set);
		List<Integer> visited = new ArrayList<>();
		set.forEach(value -> {
			visited.add(value);
			assertEquals(2, set.size());
			set.add(3);
			set.clear();
			set.add(4);
		});
		assertEquals(expected, visited);
		assertEquals(Collections.singleton(4), set);
	}

	@Test
	public void nestedTraversalDefersChangesUntilTheOutermostCallbackReturns() {
		ManagedIterationHashSet<Integer> set = set(1);
		Collections.unmodifiableSet(set).forEach(value -> {
			set.add(2);
			set.forEach(nested -> {
				assertEquals(Integer.valueOf(1), nested);
				set.remove(1);
				set.add(3);
			});
			assertEquals(Collections.singleton(1), set);
		});
		assertEquals(new HashSet<>(Arrays.asList(2, 3)), set);
	}

	@Test
	public void callbackFailureStillAppliesDeferredChangesAndReleasesTraversal() {
		ManagedIterationHashSet<Integer> set = set(1);
		RuntimeException failure = new RuntimeException("callback failed");
		try {
			set.forEach(value -> {
				set.remove(1);
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

	@Test
	public void bulkOperationsWorkWithoutRemovingThroughAnUnmodifiableIterator() {
		ManagedIterationHashSet<Integer> set = set(1, 2, 3, 4);
		assertTrue(set.removeIf(value -> value % 2 == 0));
		assertTrue(set.retainAll(Arrays.asList(1, 7)));
		assertFalse(set.addAll(set));
		assertFalse(set.retainAll(set));
		assertTrue(set.removeAll(set));
		assertTrue(set.isEmpty());
	}

	@Test
	public void failingQueuedOperationDoesNotLeaveLaterChangesPending() {
		ManagedIterationHashSet<Integer> set = set(1);
		RuntimeException failure = new RuntimeException("predicate failed");
		try {
			set.forEach(value -> {
				set.removeIf(candidate -> { throw failure; });
				set.remove(1);
				set.add(2);
			});
			fail("Expected predicate failure");
		} catch (RuntimeException actual) {
			assertSame(failure, actual);
		}
		assertEquals(Collections.singleton(2), set);
		assertTrue(set.add(3));
		set.forEach(value -> {});
		assertEquals(new HashSet<>(Arrays.asList(2, 3)), set);
	}

	@Test
	public void cleanupFailureDoesNotHideOriginalCallbackFailure() {
		ManagedIterationHashSet<Integer> set = set(1);
		RuntimeException callbackFailure = new RuntimeException("callback failed");
		RuntimeException predicateFailure = new RuntimeException("predicate failed");
		try {
			set.forEach(value -> {
				set.removeIf(candidate -> { throw predicateFailure; });
				set.add(2);
				throw callbackFailure;
			});
			fail("Expected callback failure");
		} catch (RuntimeException actual) {
			assertSame(callbackFailure, actual);
			assertArrayEquals(new Throwable[] {predicateFailure}, actual.getSuppressed());
		}
		assertEquals(new HashSet<>(Arrays.asList(1, 2)), set);
	}

	@Test
	public void nestedTraversalInsideQueuedPredicateDoesNotDrainLaterOperationsEarly() {
		ManagedIterationHashSet<Integer> set = set(1, 2);
		set.forEach(value -> {
			if (value == 1) {
				set.removeIf(candidate -> {
					if (candidate == 1) {
						set.forEach(nested -> set.add(3));
						assertFalse(set.contains(3));
						assertFalse(set.contains(4));
					}
					return false;
				});
				set.add(4);
			}
		});
		assertEquals(new HashSet<>(Arrays.asList(1, 2, 3, 4)), set);
	}

	@Test
	public void deferredBulkOperationsRetainTheirOrder() {
		ManagedIterationHashSet<Integer> set = set(1, 2);
		set.forEach(value -> {
			if (value == 1) {
				set.addAll(Arrays.asList(3, 4));
				set.removeAll(Arrays.asList(1, 3));
				set.retainAll(Arrays.asList(2, 4));
				set.removeIf(candidate -> candidate == 2);
				assertEquals(new HashSet<>(Arrays.asList(1, 2)), set);
			}
		});
		assertEquals(Collections.singleton(4), set);
	}

	@Test
	public void shortCircuitCollectionOperationsDoNotLockTheirInputs() {
		ManagedIterationHashSet<Integer> set = set(1, 2);
		assertFalse(new HashSet<>(Arrays.asList(3, 4)).equals(set));
		assertNotNull(set.toString());
		assertTrue(set.add(5));
		assertEquals(3, set.size());
	}

	@Test
	@SuppressWarnings("unchecked")
	public void cloningDuringTraversalDoesNotSharePendingChangesOrTraversalState() {
		ManagedIterationHashSet<Integer> set = set(1);
		List<Set<Integer>> copies = new ArrayList<>();
		set.forEach(value -> {
			set.add(2);
			ManagedIterationHashSet<Integer> copy = (ManagedIterationHashSet<Integer>) set.clone();
			assertTrue(copy.add(3));
			copies.add(copy);
		});
		assertEquals(new HashSet<>(Arrays.asList(1, 2)), set);
		assertEquals(new HashSet<>(Arrays.asList(1, 3)), copies.get(0));
	}
}
