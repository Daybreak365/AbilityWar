package daybreak.abilitywar.utils.base.collect;

import com.google.common.collect.UnmodifiableIterator;
import java.util.AbstractSet;
import java.util.Collection;
import java.util.function.Predicate;
import org.jetbrains.annotations.NotNull;

public abstract class SetView<E> extends AbstractSet<E> {
	SetView() {}

	@Deprecated
	public final boolean add(E e) { throw new UnsupportedOperationException(); }

	@Deprecated
	public final boolean remove(Object object) { throw new UnsupportedOperationException(); }

	@Deprecated
	public final boolean addAll(@NotNull Collection<? extends E> elements) { throw new UnsupportedOperationException(); }

	@Deprecated
	public final boolean removeAll(Collection<?> elements) { throw new UnsupportedOperationException(); }

	@Deprecated
	public final boolean removeIf(Predicate<? super E> filter) { throw new UnsupportedOperationException(); }

	@Deprecated
	public final boolean retainAll(@NotNull Collection<?> elements) { throw new UnsupportedOperationException(); }

	@Deprecated
	public final void clear() { throw new UnsupportedOperationException(); }

	@NotNull
	public abstract UnmodifiableIterator<E> iterator();
}