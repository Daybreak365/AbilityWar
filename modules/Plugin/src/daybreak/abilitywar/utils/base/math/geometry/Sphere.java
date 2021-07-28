package daybreak.abilitywar.utils.base.math.geometry;

import daybreak.abilitywar.utils.base.math.FastMath;
import daybreak.abilitywar.utils.base.math.geometry.location.LocationIterator;
import org.bukkit.Location;
import org.bukkit.util.Vector;

import java.util.NoSuchElementException;

import static com.google.common.base.Preconditions.checkArgument;

public class Sphere extends Shape {

	private static final double TWICE_PI = 2 * Math.PI;

	public static LocationIterator iteratorOf(Location center, double r, int amount) {
		checkArgument(amount >= 1, "The amount must be 1 or greater.");
		checkArgument(r > 0, "The radius must be positive");
		checkArgument(!Double.isNaN(r) && Double.isFinite(r));
		return new LocationIterator() {
			private final double divided = Math.PI / amount;
			private double theta = 0, phi = 0;

			@Override
			public boolean hasNext() {
				return phi <= TWICE_PI;
			}

			@Override
			public Location next() {
				if (phi > TWICE_PI) throw new NoSuchElementException();
				final Location next = center.clone().add(r * FastMath.cos(theta) * FastMath.cos(phi), r * FastMath.sin(theta) * FastMath.cos(phi), r * FastMath.sin(phi));
				if ((theta += divided) >= Math.PI) {
					this.phi += divided;
					this.theta = 0;
				}
				return next;
			}
		};
	}

	public static Sphere of(double radius, int amount) {
		return new Sphere(radius, amount);
	}

	private Sphere(double radius, int amount) {
		super(amount);
		checkArgument(amount >= 1, "The amount must be 1 or greater.");
		checkArgument(radius > 0, "The radius must be positive");
		checkArgument(!Double.isNaN(radius) && Double.isFinite(radius));
		final double divided = Math.PI / amount;
		for (double radians = divided; radians <= Math.PI; radians += divided) {
			double sin = FastMath.sin(radians), z = radius * FastMath.cos(radians);
			for (double phi = divided; phi < TWICE_PI; phi += divided) {
				add(new Vector(radius * sin * FastMath.cos(phi), radius * sin * FastMath.sin(phi), z));
			}
		}
	}

	private Sphere(int amount) {
		super(amount);
	}

	@Override
	public Sphere clone() {
		Sphere sphere = new Sphere(size());
		for (Vector vector : this) {
			sphere.add(vector.clone());
		}
		return sphere;
	}

}
