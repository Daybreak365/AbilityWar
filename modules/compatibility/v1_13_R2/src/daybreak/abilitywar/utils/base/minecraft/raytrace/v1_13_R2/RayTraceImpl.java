package daybreak.abilitywar.utils.base.minecraft.raytrace.v1_13_R2;

import daybreak.abilitywar.utils.base.minecraft.raytrace.IRayTrace;
import net.minecraft.server.v1_13_R2.FluidCollisionOption;
import net.minecraft.server.v1_13_R2.MovingObjectPosition;
import net.minecraft.server.v1_13_R2.MovingObjectPosition.EnumMovingObjectType;
import net.minecraft.server.v1_13_R2.Vec3D;
import org.bukkit.World;
import org.bukkit.craftbukkit.v1_13_R2.CraftWorld;

public class RayTraceImpl implements IRayTrace {
	@Override
	public boolean hitsBlock(World world, double ax, double ay, double az, double bx, double by, double bz) {
		final MovingObjectPosition rayTrace = ((CraftWorld) world).getHandle().rayTrace(new Vec3D(ax, ay, az), new Vec3D(bx, by, bz), FluidCollisionOption.NEVER, true, false);
		return rayTrace != null && rayTrace.type != EnumMovingObjectType.MISS;
	}
}
