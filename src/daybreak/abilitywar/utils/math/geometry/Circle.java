package daybreak.abilitywar.utils.math.geometry;

import java.util.ArrayList;
import org.bukkit.Location;
import org.bukkit.util.Vector;


import static daybreak.abilitywar.utils.Validate.notNull;

public class Circle {

	private Location center;
	private double radius;
	private int amount = 10;
	private boolean highestLocation = false;
	private double xAxisRotation = 0.0;
	private double yAxisRotation = 0.0;
	private double zAxisRotation = 0.0;

	public Circle(Location center, double radius) {
		this.center = notNull(center).clone();
		this.radius = radius;
	}

	public Circle setCenter(Location center) {
		this.center = notNull(center);
		return this;
	}

	public Circle setRadius(double radius) {
		this.radius = radius;
		return this;
	}

	public Circle setAmount(int amount) {
		this.amount = amount;
		return this;
	}

	public Circle setHighestLocation(boolean highestLocation) {
		this.highestLocation = highestLocation;
		return this;
	}

	private void rotateAroundAxisX(double angle) {
		xAxisRotation += angle;
	}

	private void rotateAroundAxisY(double angle) {
		yAxisRotation += angle;
	}

	private void rotateAroundAxisZ(double angle) {
		zAxisRotation += angle;
	}

	public ArrayList<Location> getLocations() {
		ArrayList<Location> locations = new ArrayList<>();
		for (double degree = 0; degree < 360; degree += (360.0 / amount)) {
			double radians = Math.toRadians(degree);
			double cos = Math.cos(radians), sin = Math.sin(radians);
			double x = cos * radius, z = sin * radius;

			Location location = center.clone().add(x, 0, z);
			if (highestLocation)
				location.setY(location.getWorld().getHighestBlockYAt(location) + 1);
			locations.add(location);
		}
		return locations;
	}

	private Location getAxisXRotation(Vector v, double cos, double sin) {
		Location location = center.clone();
		if (xAxisRotation != 0.0) {
			location.add(v.setY(v.getY() * cos - v.getZ() * sin).setZ(v.getY() * sin + v.getZ() * cos));
		}
		if (yAxisRotation != 0.0) {
			v = location.toVector();
			location = center.clone();
			location.add(v.setX(v.getX() * cos + v.getZ() * sin).setZ(v.getX() * -sin + v.getZ() * cos));
		}
		if (zAxisRotation != 0.0) {
			v = location.toVector();
			location = center.clone();
			location.add(v.setX(v.getX() * cos - v.getY() * sin).setY(v.getX() * sin + v.getY() * cos));
		}
		return location;
	}

}
