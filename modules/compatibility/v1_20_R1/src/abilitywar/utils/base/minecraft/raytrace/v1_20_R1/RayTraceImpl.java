package abilitywar.utils.base.minecraft.raytrace.v1_20_R1;

import daybreak.abilitywar.utils.base.minecraft.raytrace.IRayTrace;
import net.minecraft.world.level.IBlockAccess;
import net.minecraft.world.level.RayTrace;
import net.minecraft.world.level.RayTrace.BlockCollisionOption;
import net.minecraft.world.level.RayTrace.FluidCollisionOption;
import net.minecraft.world.phys.MovingObjectPosition.EnumMovingObjectType;
import net.minecraft.world.phys.MovingObjectPositionBlock;
import net.minecraft.world.phys.Vec3D;
import org.bukkit.World;
import org.bukkit.craftbukkit.v1_19_R1.CraftWorld;

public class RayTraceImpl implements IRayTrace {
	@Override
	public boolean hitsBlock(World world, double ax, double ay, double az, double bx, double by, double bz) {
		final MovingObjectPositionBlock rayTrace = ((IBlockAccess) ((CraftWorld) world).getHandle()).clip(new RayTrace(new Vec3D(ax, ay, az), new Vec3D(bx, by, bz), BlockCollisionOption.COLLIDER, FluidCollisionOption.NONE, null));
		return rayTrace != null && rayTrace.getType() != EnumMovingObjectType.MISS;
	}
}
