package daybreak.abilitywar.utils.base.math.geometry;

import daybreak.abilitywar.utils.base.collect.Pair;
import org.bukkit.util.Vector;

public class Wing extends Shape {

	private static final double space = 0.2;

	private Wing(final boolean[][] shape, final boolean inverted) {
		super(shape.length * (shape.length > 0 ? shape[0].length : 0));

		final int lines = shape.length;
		for (int i = 0; i < lines; i++) {
			boolean[] line = shape[i];
			final int points = line.length;
			for (int j = 0; j < points; j++) {
				if (line[j]) {
					add(new Vector(space * (inverted ? -(points - j) : (points - j)), space * (lines - i), 0));
				}
			}
		}
	}

	private Wing(int amount) {
		super(amount);
	}

	public static Pair<Wing, Wing> of(boolean[][] shape) {
		return Pair.of(new Wing(shape, false), new Wing(shape, true));
	}

	@Override
	public Wing clone() {
		Wing wing = new Wing(size());
		for (Vector vector : this) {
			wing.add(vector.clone());
		}
		return wing;
	}

}
