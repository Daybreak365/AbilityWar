package daybreak.abilitywar.utils.base.collect;

import com.google.common.collect.AbstractIterator;
import com.google.common.collect.UnmodifiableIterator;
import java.util.Iterator;
import java.util.Set;
import java.util.stream.Stream;
import org.jetbrains.annotations.NotNull;

public class SetUnion {
	private SetUnion() {}

	@NotNull
	public static <E> SetView<E> union(final Set<? extends E> set1, final Set<? extends E> set2) {
		return new SetView<E>() {
			@Override
			public int size() {
				int size = set1.size();
				for (final E e : set2) {
					if (!set1.contains(e)) ++size;
				}
				return size;
			}
			@NotNull
			@Override
			public UnmodifiableIterator<E> iterator() {
				return new AbstractIterator<E>() {
					private final Iterator<? extends E> itr1 = set1.iterator(), itr2 = set2.iterator();
					@Override
					protected E computeNext() {
						if (itr1.hasNext()) {
							return itr1.next();
						} else {
							while (itr2.hasNext()) {
								final E e = itr2.next();
								if (!set1.contains(e)) {
									return e;
								}
							}
							return endOfData();
						}
					}
				};
			}

			@Override
			public boolean isEmpty() {
				return set1.isEmpty() && set2.isEmpty();
			}

			@Override
			public boolean contains(Object o) {
				return set1.contains(o) || set2.contains(o);
			}

			@Override
			public Stream<E> stream() {
				return Stream.concat(set1.stream(), set2.stream().filter((e) -> !set1.contains(e)));
			}

			@Override
			public Stream<E> parallelStream() {
				return stream().parallel();
			}
		};
	}

	@NotNull
	public static <E> SetView<E> union(final Set<? extends E> set1, final Set<? extends E> set2, final Set<? extends E> set3) {
		return new SetView<E>() {
			@Override
			public int size() {
				int size = set1.size();
				for (final E e : set2) {
					if (!set1.contains(e)) ++size;
				}
				for (final E e : set3) {
					if (!set1.contains(e) && !set2.contains(e)) ++size;
				}
				return size;
			}
			@NotNull
			@Override
			public UnmodifiableIterator<E> iterator() {
				return new AbstractIterator<E>() {
					private final Iterator<? extends E> itr1 = set1.iterator(), itr2 = set2.iterator(), itr3 = set3.iterator();
					@Override
					protected E computeNext() {
						if (itr1.hasNext()) {
							return itr1.next();
						} else {
							while (itr2.hasNext()) {
								final E e = itr2.next();
								if (!set1.contains(e)) {
									return e;
								}
							}
							while (itr3.hasNext()) {
								final E e = itr3.next();
								if (!set1.contains(e) && !set2.contains(e)) {
									return e;
								}
							}
							return endOfData();
						}
					}
				};
			}

			@Override
			public boolean isEmpty() {
				return set1.isEmpty() && set2.isEmpty() && set3.isEmpty();
			}

			@Override
			public boolean contains(Object o) {
				return set1.contains(o) || set2.contains(o) || set3.contains(o);
			}

			@Override
			public Stream<E> stream() {
				return Stream.concat(Stream.concat(set1.stream(), set2.stream().filter((e) -> !set1.contains(e))), set3.stream().filter((e) -> !set1.contains(e) && !set2.contains(e)));
			}

			@Override
			public Stream<E> parallelStream() {
				return stream().parallel();
			}
		};
	}

}
