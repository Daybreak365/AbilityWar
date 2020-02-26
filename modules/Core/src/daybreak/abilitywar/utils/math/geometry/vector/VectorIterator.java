package daybreak.abilitywar.utils.math.geometry.vector;

import daybreak.abilitywar.utils.math.geometry.location.LocationIterator;
import org.bukkit.Location;
import org.bukkit.util.Vector;

import java.util.Iterator;

public abstract class VectorIterator implements Iterator<Vector> {

	public final LocationIterator toLocationIterator(Location center) {
		return new LocationIterator() {
			@Override
			public boolean hasNext() {
				return VectorIterator.this.hasNext();
			}

			@Override
			public Location next() {
				return center.clone().add(VectorIterator.this.next());
			}
		};
	}

	public final Iterable<Vector> iterable() {
		return new Iterable<Vector>() {
			@Override
			public Iterator<Vector> iterator() {
				return VectorIterator.this;
			}
		};
	}

}
