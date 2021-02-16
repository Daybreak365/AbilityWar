package daybreak.abilitywar.utils.base.math;

import daybreak.abilitywar.utils.base.math.LocationUtil.Locations;
import org.bukkit.Location;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

public class VectorUtil {

	public static final Zero ZERO = new Zero();

	private static class Zero extends Vector {

		private Zero() {
			super();
		}

		@Override
		public @NotNull Vector setX(int x) { return this; }

		@Override
		public @NotNull Vector setX(float x) { return this; }

		@Override
		public @NotNull Vector setX(double x) { return this; }

		@Override
		public @NotNull Vector setY(int y) { return this; }

		@Override
		public @NotNull Vector setY(double y) { return this; }

		@Override
		public @NotNull Vector setY(float y) { return this; }

		@Override
		public @NotNull Vector setZ(int z) { return this; }

		@Override
		public @NotNull Vector setZ(double z) { return this; }

		@Override
		public @NotNull Vector setZ(float z) { return this; }

		@Override
		public @NotNull Vector add(@NotNull Vector vec) { return this; }

		@Override
		public @NotNull Vector subtract(@NotNull Vector vec) { return this; }

		@Override
		public @NotNull Vector multiply(@NotNull Vector vec) { return this; }

		@Override
		public @NotNull Vector divide(@NotNull Vector vec) { return this; }

		@Override
		public @NotNull Vector multiply(int m) { return this; }

		@Override
		public @NotNull Vector multiply(double m) { return this; }

		@Override
		public @NotNull Vector multiply(float m) { return this; }

		@Override
		public @NotNull Vector midpoint(@NotNull Vector other) { return this; }
	}

	private VectorUtil() {}

	public static Vector validateVector(Vector vector) {
		final double x = vector.getX(), y = vector.getY(), z = vector.getZ();
		if (Double.isInfinite(x) || Double.isNaN(x)) vector.setX(0);
		if (Double.isInfinite(y) || Double.isNaN(y)) vector.setY(0);
		if (Double.isInfinite(z) || Double.isNaN(z)) vector.setZ(0);
		return vector;
	}

	public static Vector rotateAroundAxis(Vector vector, Vector axis, double angle) {
		if (angle < 0) {
			final double abs = Math.abs(angle);
			angle = 360 - (abs % 360);
		}
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
			if (angle < 0) {
				final double abs = Math.abs(angle);
				angle = 360 - (abs % 360);
			}
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
