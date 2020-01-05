package daybreak.abilitywar.utils.math.geometry;

import daybreak.abilitywar.utils.math.LocationUtil;
import org.bukkit.Location;

import java.util.ArrayList;

public class Locations extends ArrayList<Location> {

	public Locations floor(double referenceY) {
		for (Location location : this) {
			location.setY(LocationUtil.getFloorYAt(location.getWorld(), referenceY, location.getBlockX(), location.getBlockZ()) + 1);
		}
		return this;
	}

	public Locations highest() {
		for (Location location : this) {
			location.setY(location.getWorld().getHighestBlockYAt(location) + 1);
		}
		return this;
	}

}
