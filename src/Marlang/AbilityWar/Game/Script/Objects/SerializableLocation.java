package Marlang.AbilityWar.Game.Script.Objects;

import java.io.Serializable;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

public class SerializableLocation implements Serializable {
	
	private static final long serialVersionUID = -3969083133241402948L;

	private final String WorldName;
	private final double X;
	private final double Y;
	private final double Z;
	private final float Yaw;
	private final float Pitch;
	
	public SerializableLocation(Location location) {
		this.WorldName = location.getWorld().getName();
		this.X = location.getX();
		this.Y = location.getY();
		this.Z = location.getZ();
		this.Yaw = location.getYaw();
		this.Pitch = location.getPitch();
	}
	
	public Location getLocation() throws NullPointerException {
		World world = Bukkit.getWorld(WorldName);
		if(world != null) {
			return new Location(world, X, Y, Z, Yaw, Pitch);
		} else {
			throw new NullPointerException();
		}
	}
	
}
