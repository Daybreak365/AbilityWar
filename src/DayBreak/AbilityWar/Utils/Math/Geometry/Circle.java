package DayBreak.AbilityWar.Utils.Math.Geometry;

import static DayBreak.AbilityWar.Utils.Validate.notNull;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Location;

public class Circle {

	private Location center;
	private final double radius;
	private int amount = 10;
	private boolean highestLocation = false;

	public Circle(final Location center, final double radius) {
		this.center = notNull(center).clone();
		this.radius = radius;
	}

	public Circle setCenter(final Location center) {
		this.center = notNull(center);
		return this;
	}

	public Circle setAmount(final int amount) {
		this.amount = amount;
		return this;
	}

	public Circle setHighestLocation(final boolean highestLocation) {
		this.highestLocation = highestLocation;
		return this;
	}

	public List<Location> getLocations() {
		List<Location> locations = new ArrayList<Location>();
		for (double degree = 0; degree < 360; degree += (360 / amount)) {
			double radians = Math.toRadians(degree);
			double X = Math.cos(radians) * radius;
			double Z = Math.sin(radians) * radius;

			Location location = center.clone().add(X, 0, Z);
			if (highestLocation)
				location.setY(location.getWorld().getHighestBlockYAt(location) + 1);
			locations.add(location);
		}
		return locations;
	}

}
