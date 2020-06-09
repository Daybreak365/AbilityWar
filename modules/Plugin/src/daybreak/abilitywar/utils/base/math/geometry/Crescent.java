package daybreak.abilitywar.utils.base.math.geometry;

import daybreak.abilitywar.utils.base.math.FastMath;
import org.bukkit.util.Vector;

import static com.google.common.base.Preconditions.checkArgument;

public class Crescent extends Shape {

	private static final double
			RADIANS_15 = Math.toRadians(15),
			RADIANS_150 = Math.toRadians(150),
			RADIANS_165 = Math.toRadians(165),
			RADIANS_195 = Math.toRadians(195),
			RADIANS_210 = Math.toRadians(210),
			RADIANS_345 = Math.toRadians(345),
			RADIANS_360 = Math.toRadians(360);

	private Crescent(double radius, double frequency) {
		super();
		checkArgument(frequency >= 1, "The frequency must be 1 or greater.");
		checkArgument(radius > 0, "The radius must be positive");
		checkArgument(!Double.isNaN(radius) && Double.isFinite(radius));
		double divided = RADIANS_150 / frequency;
		for (double radians = RADIANS_15; radians <= RADIANS_165; radians += divided) {
			add(new Vector(FastMath.cos(radians) * radius, 0, FastMath.sin(radians) * radius));
		}
		divided = RADIANS_210 / frequency;
		final double halfRadius = radius / 2d;
		for (double radians = 0; radians <= RADIANS_195; radians += divided) {
			add(new Vector(FastMath.cos(radians) * radius, 0, FastMath.sin(radians) * radius + halfRadius));
		}
		for (double radians = RADIANS_345; radians <= RADIANS_360; radians += divided) {
			add(new Vector(FastMath.cos(radians) * radius, 0, FastMath.sin(radians) * radius + halfRadius));
		}
	}

	private Crescent(int amount) {
		super(amount);
	}

	public static Crescent of(double radius, double frequency) {
		return new Crescent(radius, frequency);
	}

	@Override
	public Crescent clone() {
		Crescent crescent = new Crescent(size());
		for (Vector vector : this) {
			crescent.add(vector.clone());
		}
		return crescent;
	}

}
