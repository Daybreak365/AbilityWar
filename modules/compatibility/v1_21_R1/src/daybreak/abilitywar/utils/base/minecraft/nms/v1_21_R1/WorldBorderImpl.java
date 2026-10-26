package daybreak.abilitywar.utils.base.minecraft.nms.v1_21_R1;

import daybreak.abilitywar.utils.base.minecraft.nms.IWorldBorder;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

public class WorldBorderImpl implements IWorldBorder {
	final org.bukkit.WorldBorder border = Bukkit.createWorldBorder();
	WorldBorderImpl(World world) { setWorld(world); }
	@Override public void setWorld(World world) {
		final Location center = border.getCenter();
		border.setCenter(new Location(world, center.getX(), 0, center.getZ()));
	}
	@Override public void setCenter(double x, double z) { border.setCenter(x, z); }
	@Override public void setDamageAmount(double amount) { border.setDamageAmount(amount); }
	@Override public void setDamageBuffer(double buffer) { border.setDamageBuffer(buffer); }
	@Override public void setSize(double size) { border.setSize(size); }
	@Override public void setWarningDistance(int distance) { border.setWarningDistance(distance); }
	@Override public void setWarningTime(int time) { border.setWarningTime(time); }
}
