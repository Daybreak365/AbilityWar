package daybreak.abilitywar.utils.math.geometry.location;

import org.bukkit.Location;

import java.util.Iterator;

public abstract class LocationIterator implements Iterator<Location> {

	public final Iterable<Location> iterable() {
		return new Iterable<Location>() {
			@Override
			public Iterator<Location> iterator() {
				return LocationIterator.this;
			}
		};
	}

}
