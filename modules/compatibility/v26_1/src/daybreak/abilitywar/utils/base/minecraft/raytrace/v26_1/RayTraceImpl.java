package daybreak.abilitywar.utils.base.minecraft.raytrace.v26_1;

import daybreak.abilitywar.utils.base.minecraft.raytrace.IRayTrace;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.ClipContext.Block;
import net.minecraft.world.level.ClipContext.Fluid;
import net.minecraft.world.phys.HitResult.Type;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import org.bukkit.World;
import org.bukkit.craftbukkit.CraftWorld;

public class RayTraceImpl implements IRayTrace {
	@Override
	public boolean hitsBlock(World world, double ax, double ay, double az, double bx, double by, double bz) {
		final BlockHitResult rayTrace = ((BlockGetter) ((CraftWorld) world).getHandle()).clip(new ClipContext(new Vec3(ax, ay, az), new Vec3(bx, by, bz), Block.COLLIDER, Fluid.NONE, CollisionContext.empty()));
		return rayTrace != null && rayTrace.getType() != Type.MISS;
	}
}
