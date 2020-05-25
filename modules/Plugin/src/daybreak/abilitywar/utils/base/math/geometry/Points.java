package daybreak.abilitywar.utils.base.math.geometry;

import org.bukkit.util.Vector;

public class Points extends Shape {

	private Points(final double space, final boolean[][] shape) {
		super(shape.length * (shape.length > 0 ? shape[0].length : 0));
		final int lines = shape.length;
		for (int i = 0; i < lines; i++) {
			boolean[] line = shape[i];
			final int lineLength = line.length;
			for (int j = 0; j < lineLength; j++) {
				if (line[j]) {
					add(new Vector(space * (lineLength - j) - (space * (lineLength / 2.0)), space * (lines - i), 0));
				}
			}
		}
	}

	public static Points of(final double space, final boolean[][] shape) {
		return new Points(space, shape);
	}

}
