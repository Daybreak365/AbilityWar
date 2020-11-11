package daybreak.abilitywar.utils.base.minecraft.raytrace.v1_14_R1;

import daybreak.abilitywar.utils.base.minecraft.raytrace.IRayTrace;
import net.minecraft.server.v1_14_R1.MovingObjectPosition.EnumMovingObjectType;
import net.minecraft.server.v1_14_R1.MovingObjectPositionBlock;
import net.minecraft.server.v1_14_R1.RayTrace;
import net.minecraft.server.v1_14_R1.RayTrace.BlockCollisionOption;
import net.minecraft.server.v1_14_R1.RayTrace.FluidCollisionOption;
import net.minecraft.server.v1_14_R1.Vec3D;
import org.bukkit.World;
import org.bukkit.craftbukkit.v1_14_R1.CraftWorld;

public class RayTraceImpl implements IRayTrace {
	@Override
	public boolean hitsBlock(World world, double ax, double ay, double az, double bx, double by, double bz) {
		final MovingObjectPositionBlock rayTrace = ((CraftWorld) world).getHandle().rayTrace(new RayTrace(new Vec3D(ax, ay, az), new Vec3D(bx, by, bz), BlockCollisionOption.COLLIDER, FluidCollisionOption.NONE, null));
		return rayTrace != null && rayTrace.getType() != EnumMovingObjectType.MISS;
	}
}
