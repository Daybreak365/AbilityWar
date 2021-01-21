package daybreak.abilitywar.utils.base.minecraft.nms;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

public interface IHologram {

	void display(Player player) throws IllegalStateException;
	void hide(Player player) throws IllegalStateException;

	void teleport(World world, double x, double y, double z, float yaw, float pitch) throws IllegalStateException;
	default void teleport(Location location) throws IllegalStateException {
		this.teleport(location.getWorld(), location.getX(), location.getY(), location.getZ(), location.getYaw(), location.getPitch());
	}
	Location getLocation();

	void setText(String text) throws IllegalStateException;
	String getText();

	void unregister() throws IllegalStateException;

	boolean isUnregistered();

}
