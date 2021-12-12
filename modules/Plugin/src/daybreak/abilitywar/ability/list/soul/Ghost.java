package daybreak.abilitywar.ability.list.soul;

import org.bukkit.Location;

public interface Ghost {
	default void move(Location location, float speed) {
		move(location.getX(), location.getY(), location.getZ(), speed);
	}
	void move(double x, double y, double z, float speed);
	void remove();
	Location getLocation();
}
