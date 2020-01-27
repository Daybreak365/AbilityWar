package daybreak.abilitywar.utils.math.geometry;

import daybreak.abilitywar.utils.math.FastMath;
import org.bukkit.Location;
import org.bukkit.util.Vector;

import java.util.Iterator;
import java.util.NoSuchElementException;

public class Circle extends Shape {

	private static final double TWICE_PI = 2 * Math.PI;

	public static Iterable<Location> iterableOf(Location center, double radius, int amount) {
		return new Iterable<Location>() {
			@Override
			public Iterator<Location> iterator() {
				return new Iterator<Location>() {
					private int cursor = 0;

					@Override
					public boolean hasNext() {
						return cursor < amount;
					}

					@Override
					public Location next() {
						if (cursor >= amount) throw new NoSuchElementException();
						cursor++;
						double radians = (TWICE_PI / amount) * cursor;
						return center.clone().add(FastMath.cos(radians) * radius, 0, FastMath.sin(radians) * radius);
					}
				};
			}
		};
	}

	public static Circle of(double radius, int amount) {
		return new Circle(radius, amount);
	}

	private Circle(double radius, int amount) {
		super(amount);
		final double divided = TWICE_PI / amount;
		for (double radians = divided; size() <= amount; radians += divided) {
			add(new Vector(FastMath.cos(radians) * radius, 0, FastMath.sin(radians) * radius));
		}
	}

	private Circle(int amount) {
		super(amount);
	}

	@Override
	public Circle clone() {
		Circle circle = new Circle(size());
		for (Vector vector : this) {
			circle.add(vector.clone());
		}
		return circle;
	}

}
