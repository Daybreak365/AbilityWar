package daybreak.abilitywar.utils.base;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public class Random extends java.util.Random {

	public <E> E pick(final @NotNull E[] array) {
		return array[nextInt(array.length)];
	}

	public <E> E pick(final @NotNull List<E> list) {
		return list.get(nextInt(list.size()));
	}

	public <E extends Enum<E>> E pick(final @NotNull Class<E> enumClass) {
		return pick(enumClass.getEnumConstants());
	}

}
