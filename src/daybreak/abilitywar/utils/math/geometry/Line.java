package daybreak.abilitywar.utils.math.geometry;

import daybreak.abilitywar.utils.math.geometry.location.LocationIterator;
import daybreak.abilitywar.utils.math.geometry.vector.VectorIterator;
import org.bukkit.Location;
import org.bukkit.util.Vector;

import java.util.NoSuchElementException;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

public class Line extends Shape {

	public static Vector vectorAt(Location from, Location to, int amount, int index) throws IndexOutOfBoundsException {
		checkNotNull(from);
		checkNotNull(to);
		checkArgument(amount >= 1, "amount must have a value of 1 or greater.");
		if (index > amount || index < 0) {
			throw new IndexOutOfBoundsException("index must be a number between 0 and amount.");
		}
		return to.toVector().subtract(from.toVector()).multiply(Math.min((1.0 / amount) * index, 1.0));
	}

	public static Line between(Location from, Location to, int amount) {
		checkArgument(amount >= 1, "amount must have a value of 1 or greater.");
		return new Line(checkNotNull(from), checkNotNull(to), amount);
	}

	public static LocationIterator iteratorBetween(Location from, Location to, int amount) {
		checkNotNull(from);
		checkNotNull(to);
		checkArgument(amount >= 1, "amount must have a value of 1 or greater.");
		return new LocationIterator() {
			final Vector vector = to.toVector().subtract(from.toVector());
			final double increment = 1.0 / amount;
			private int cursor = 0;

			@Override
			public boolean hasNext() {
				return cursor < amount;
			}

			@Override
			public Location next() {
				if (cursor >= amount) throw new NoSuchElementException();
				cursor++;
				return from.clone().add(vector.clone().multiply(Math.min(increment * cursor, 1.0)));
			}
		};
	}

	public static Line of(Vector vector, int amount) {
		checkArgument(amount >= 1, "amount must have a value of 1 or greater.");
		return new Line(checkNotNull(vector), amount);
	}

	public static VectorIterator iteratorOf(Vector vector, int amount) {
		checkNotNull(vector);
		checkArgument(amount >= 1, "amount must have a value of 1 or greater.");
		return new VectorIterator() {
			final double increment = 1.0 / amount;
			private int cursor = 0;

			@Override
			public boolean hasNext() {
				return cursor < amount;
			}

			@Override
			public Vector next() {
				if (cursor >= amount) throw new NoSuchElementException();
				cursor++;
				return vector.clone().multiply(Math.min(increment * cursor, 1.0));
			}
		};
	}

	private Line(Vector vector, int amount) {
		super(amount);
		final double increment = 1.0 / amount;
		for (int i = 0; i <= amount; i++) {
			add(vector.clone().multiply(Math.min(increment * i, 1.0)));
		}
	}

	private Line(Location from, Location to, int amount) {
		this(to.toVector().subtract(from.toVector()), amount);
	}

}
