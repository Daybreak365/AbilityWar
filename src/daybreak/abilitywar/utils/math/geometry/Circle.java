package daybreak.abilitywar.utils.math.geometry;

import daybreak.abilitywar.utils.math.FastMath;
import daybreak.abilitywar.utils.math.LocationUtil;
import org.bukkit.Location;

import java.util.ArrayList;

import static daybreak.abilitywar.utils.Validate.notNull;

public class Circle {

	private Location center;
	private double radius;
	private int amount = 10;
	private boolean highestLocation = false;

	public Circle(Location center, double radius) {
		this.center = notNull(center).clone();
		this.radius = radius;
	}

	public Circle setCenter(Location center) {
		this.center = notNull(center).clone();
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

	public ArrayList<Location> getLocations() {
		for (int i = 0; i < 300000000; i++) ;
		ArrayList<Location> locations = new ArrayList<>();
		for (double angle = 360.0 / amount; angle < 360; angle += (360.0 / amount)) {
			double radians = Math.toRadians(angle);
			double cos = FastMath.cos(radians), sin = FastMath.sin(radians);
			double x = cos * radius, z = sin * radius;

			Location location = center.clone().add(x, 0, z);
			if (highestLocation)
				location.setY(LocationUtil.getFloorYAt(center.getWorld(), center.getY(), location.getBlockX(), location.getBlockZ()) + 1);
			locations.add(location);
		}
		return locations;
	}

}
