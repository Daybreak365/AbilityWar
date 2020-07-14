package daybreak.abilitywar.utils.base.minecraft.compat.nms;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

public interface iHologram {

	void display(Player player);

	void hide(Player player);

	void teleport(World world, double x, double y, double z, float yaw, float pitch);

	Location getLocation();

	void setText(String text);

	String getText();

}
