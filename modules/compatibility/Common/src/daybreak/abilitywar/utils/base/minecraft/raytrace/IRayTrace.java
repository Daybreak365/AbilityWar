package daybreak.abilitywar.utils.base.minecraft.raytrace;

import org.bukkit.World;

public interface IRayTrace {

	boolean hitsBlock(World world, double ax, double ay, double az, double bx, double by, double bz);

}
