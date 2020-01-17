package daybreak.abilitywar.utils.math.geometry;

import daybreak.abilitywar.utils.math.FastMath;
import daybreak.abilitywar.utils.math.VectorUtil.Vectors;
import org.bukkit.util.Vector;

public class Circle {

	private double radius;
	private int amount;

	public Circle(double radius, int amount) {
		this.radius = radius;
		this.amount = amount;
	}

	public Circle setRadius(double radius) {
		if (radius > 0) this.radius = radius;
		return this;
	}

	public Circle setAmount(int amount) {
		if (amount > 0) this.amount = amount;
		return this;
	}

	public Vectors getVectors() {
		Vectors vectors = new Vectors();
		final int size = vectors.size();
		for (double angle = 360.0 / amount; angle < 360.0 || size <= amount; angle += (360.0 / amount)) {
			double radians = Math.toRadians(angle);
			double sin = FastMath.sin(radians), cos = FastMath.cos(radians);
			double x = cos * radius, z = sin * radius;
			vectors.add(new Vector(x, 0, z));
		}
		return vectors;
	}

}
