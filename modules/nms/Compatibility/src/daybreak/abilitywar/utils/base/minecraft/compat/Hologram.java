package daybreak.abilitywar.utils.base.minecraft.compat;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

public interface Hologram {

	void display(Player player);

	void hide(Player player);

	void teleport(World world, double x, double y, double z, float yaw, float pitch);

	Location getLocation();

	void setText(String text);

	String getText();

}
