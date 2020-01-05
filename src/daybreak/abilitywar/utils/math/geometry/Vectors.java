package daybreak.abilitywar.utils.math.geometry;

import daybreak.abilitywar.utils.math.FastMath;
import org.bukkit.Location;
import org.bukkit.util.Vector;

import java.util.ArrayList;

public class Vectors extends ArrayList<Vector> implements Cloneable {

	private static final Vector INVERTED_X_AXIS = new Vector(-1, 0, 0);
	private static final Vector INVERTED_Y_AXIS = new Vector(0, -1, 0);
	private static final Vector INVERTED_Z_AXIS = new Vector(0, 0, -1);

	public Vectors rotateAroundAxis(Vector axis, double angle) {
		double radians = Math.toRadians(angle);
		for (Vector vector : this) {
			double x = vector.getX(), y = vector.getY(), z = vector.getZ();
			double axisX = axis.getX(), axisY = axis.getY(), axisZ = axis.getZ();

			double cos = Math.cos(radians);
			double sin = Math.sin(radians);
			double dot = vector.dot(axis);

			vector
					.setX(axisX * dot * (1d - cos)
							+ x * cos
							+ (-axisZ * y + axisY * z) * sin)
					.setY(axisY * dot * (1d - cos)
							+ y * cos
							+ (axisZ * x - axisX * z) * sin)
					.setZ(axisZ * dot * (1d - cos)
							+ z * cos
							+ (-axisY * x + axisX * y) * sin);
		}
		return this;
	}

	public Vectors rotateAroundAxisX(double angle) {
		if (angle > 0) {
			double radians = Math.toRadians(angle), sin = FastMath.sin(radians), cos = FastMath.cos(radians);
			for (Vector vector : this) {
				double y = vector.getY(), z = vector.getZ();
				vector.setY(cos * y - sin * z).setZ(sin * y + cos * z);
			}
		} else if (angle < 0) {
			rotateAroundAxis(INVERTED_X_AXIS, -angle);
		}
		return this;
	}

	public Vectors rotateAroundAxisY(double angle) {
		if (angle > 0) {
			double radians = Math.toRadians(angle), sin = FastMath.sin(radians), cos = FastMath.cos(radians);
			for (Vector vector : this) {
				double x = vector.getX(), z = vector.getZ();
				vector.setX(cos * x + sin * z).setZ(-sin * x + cos * z);
			}
		} else if (angle < 0) {
			rotateAroundAxis(INVERTED_Y_AXIS, -angle);
		}
		return this;
	}

	public Vectors rotateAroundAxisZ(double angle) {
		if (angle > 0) {
			double radians = Math.toRadians(angle), sin = FastMath.sin(radians), cos = FastMath.cos(radians);
			for (Vector vector : this) {
				double x = vector.getX(), y = vector.getY();
				vector.setX(cos * x - sin * y).setY(sin * x + cos * y);
			}
		} else if (angle < 0) {
			rotateAroundAxis(INVERTED_Z_AXIS, -angle);
		}
		return this;
	}

	public Locations getAsLocations(Location center) {
		Locations locations = new Locations();
		for (Vector vector : this) locations.add(center.clone().add(vector));
		return locations;
	}

	@Override
	public Vectors clone() {
		Vectors vectors = new Vectors();
		for (Vector vector : this) {
			vectors.add(vector.clone());
		}
		return vectors;
	}

}
