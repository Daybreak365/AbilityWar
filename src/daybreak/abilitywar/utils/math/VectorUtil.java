package daybreak.abilitywar.utils.math;

import daybreak.abilitywar.utils.math.LocationUtil.Locations;
import org.bukkit.Location;
import org.bukkit.util.Vector;

import java.util.ArrayList;

public class VectorUtil {

	public static Vector rotateAroundAxis(Vector vector, Vector axis, double angle) {
		double radians = Math.toRadians(angle);
		double x = vector.getX(), y = vector.getY(), z = vector.getZ();
		double axisX = axis.getX(), axisY = axis.getY(), axisZ = axis.getZ();

		double cos = Math.cos(radians);
		double sin = Math.sin(radians);
		double dot = vector.dot(axis);
		return vector.setX(axisX * dot * (1d - cos) + x * cos + (-axisZ * y + axisY * z) * sin).setY(axisY * dot * (1d - cos) + y * cos + (axisZ * x - axisX * z) * sin).setZ(axisZ * dot * (1d - cos) + z * cos + (-axisY * x + axisX * y) * sin);
	}

	public static Vector rotateAroundAxisX(Vector vector, double angle) {
		if (angle < 0) angle = 360 + (angle % 360.0);
		double radians = Math.toRadians(angle), sin = FastMath.sin(radians), cos = FastMath.cos(radians);
		double y = vector.getY(), z = vector.getZ();
		return vector.setY(cos * y - sin * z).setZ(sin * y + cos * z);
	}

	public static Vector rotateAroundAxisY(Vector vector, double angle) {
		if (angle < 0) angle = 360 + (angle % 360.0);
		double radians = Math.toRadians(angle), sin = FastMath.sin(radians), cos = FastMath.cos(radians);
		double x = vector.getX(), z = vector.getZ();
		return vector.setX(cos * x + sin * z).setZ(-sin * x + cos * z);
	}

	public static Vector rotateAroundAxisZ(Vector vector, double angle) {
		if (angle < 0) angle = 360 + (angle % 360.0);
		double radians = Math.toRadians(angle), sin = FastMath.sin(radians), cos = FastMath.cos(radians);
		double x = vector.getX(), y = vector.getY();
		return vector.setX(cos * x - sin * y).setY(sin * x + cos * y);
	}

	public static class Vectors extends ArrayList<Vector> implements Cloneable {

		public Vectors(int initialCapacity) {
			super(initialCapacity);
		}

		public Vectors() {
			super();
		}

		public Vectors rotateAroundAxis(Vector axis, double angle) {
			double radians = Math.toRadians(angle), sin = FastMath.sin(radians), cos = FastMath.cos(radians);
			for (Vector vector : this) {
				double x = vector.getX(), y = vector.getY(), z = vector.getZ(), axisX = axis.getX(), axisY = axis.getY(), axisZ = axis.getZ(), dot = vector.dot(axis);
				vector.setX(axisX * dot * (1d - cos) + x * cos + (-axisZ * y + axisY * z) * sin).setY(axisY * dot * (1d - cos) + y * cos + (axisZ * x - axisX * z) * sin).setZ(axisZ * dot * (1d - cos) + z * cos + (-axisY * x + axisX * y) * sin);
			}
			return this;
		}

		public Vectors rotateAroundAxisX(double angle) {
			if (angle < 0) angle = 360 + (angle % 360.0);
			double radians = Math.toRadians(angle), sin = FastMath.sin(radians), cos = FastMath.cos(radians);
			for (Vector vector : this) {
				double y = vector.getY(), z = vector.getZ();
				vector.setY(cos * y - sin * z).setZ(sin * y + cos * z);
			}
			return this;
		}

		public Vectors rotateAroundAxisY(double angle) {
			if (angle < 0) angle = 360 + (angle % 360.0);
			double radians = Math.toRadians(angle), sin = FastMath.sin(radians), cos = FastMath.cos(radians);
			for (Vector vector : this) {
				double x = vector.getX(), z = vector.getZ();
				vector.setX(cos * x + sin * z).setZ(-sin * x + cos * z);
			}
			return this;
		}

		public Vectors rotateAroundAxisZ(double angle) {
			if (angle < 0) angle = 360 + (angle % 360.0);
			double radians = Math.toRadians(angle), sin = FastMath.sin(radians), cos = FastMath.cos(radians);
			for (Vector vector : this) {
				double x = vector.getX(), y = vector.getY();
				vector.setX(cos * x - sin * y).setY(sin * x + cos * y);
			}
			return this;
		}

		public Locations toLocations(Location center) {
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

}
