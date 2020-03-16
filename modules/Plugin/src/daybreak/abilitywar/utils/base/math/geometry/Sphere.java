package daybreak.abilitywar.utils.base.math.geometry;

import daybreak.abilitywar.utils.base.math.FastMath;
import daybreak.abilitywar.utils.base.math.geometry.location.LocationIterator;
import org.bukkit.Location;
import org.bukkit.util.Vector;

import java.util.NoSuchElementException;

import static com.google.common.base.Preconditions.checkArgument;

public class Sphere extends Shape {

	private static final double TWICE_PI = 2 * Math.PI;

	public static LocationIterator iteratorOf(Location center, double radius, int amount) {
		checkArgument(amount >= 1, "The amount must be 1 or greater.");
		checkArgument(radius > 0, "The radius must be positive");
		checkArgument(!Double.isNaN(radius) && Double.isFinite(radius));
		return new LocationIterator() {
			private double divided = Math.PI / amount, radians = divided, phi = divided, sin, z;

			@Override
			public boolean hasNext() {
				return radians <= Math.PI;
			}

			@Override
			public Location next() {
				if (radians > Math.PI) throw new NoSuchElementException();
				final Location next = center.clone().add(radius * sin * FastMath.cos(phi), radius * sin * FastMath.sin(phi), z);
				if ((phi += divided) >= TWICE_PI) {
					this.radians += divided;
					this.phi = divided;
					this.sin = FastMath.sin(radians);
					this.z = radius * FastMath.cos(radians);
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
