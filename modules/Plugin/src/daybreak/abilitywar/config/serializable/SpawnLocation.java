package daybreak.abilitywar.config.serializable;

import java.util.HashMap;
import java.util.Map;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.MemorySection;

public class SpawnLocation {

	public final String world;
	public final double x, y, z;
	public final float yaw, pitch;

	public SpawnLocation(final String world, final double x, final double y, final double z, final float yaw, final float pitch) {
		this.world = world;
		this.x = x;
		this.y = y;
		this.z = z;
		this.yaw = yaw;
		this.pitch = pitch;
	}

	public SpawnLocation(final Map<?, ?> memorySection) throws ClassCastException, NullPointerException {
		this.world = (String) memorySection.get("world");
		this.x = (Double) memorySection.get("x");
		this.y = (Double) memorySection.get("y");
		this.z = (Double) memorySection.get("z");
		this.yaw = ((Double) memorySection.get("yaw")).floatValue();
		this.pitch = ((Double) memorySection.get("pitch")).floatValue();
	}

	public SpawnLocation(final MemorySection memorySection) throws ClassCastException, NullPointerException {
		this.world = memorySection.getString("world");
		this.x = memorySection.getDouble("x");
		this.y = memorySection.getDouble("y");
		this.z = memorySection.getDouble("z");
		this.yaw = (float) memorySection.getDouble("yaw");
		this.pitch = (float) memorySection.getDouble("pitch");
	}

	public SpawnLocation(final Location location) {
		this.world = location.getWorld().getName();
		this.x = location.getX();
		this.y = location.getY();
		this.z = location.getZ();
		this.yaw = location.getYaw();
		this.pitch = location.getPitch();
	}

	public Location toBukkitLocation() {
		final World world = Bukkit.getWorld(this.world);
		return world != null ? new Location(world, x, y, z, yaw, pitch) : Bukkit.getWorlds().get(0).getSpawnLocation();
	}

	public Map<String, Object> toMap() {
		final Map<String, Object> map = new HashMap<>();
		map.put("world", world);
		map.put("x", x);
		map.put("y", y);
		map.put("z", z);
		map.put("yaw", yaw);
		map.put("pitch", pitch);
		return map;
	}

}
