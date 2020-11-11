package daybreak.abilitywar.ability.list.grapplinghook.v1_13_R2;

import net.minecraft.server.v1_13_R2.EntityArmorStand;
import net.minecraft.server.v1_13_R2.EntityHuman;
import net.minecraft.server.v1_13_R2.EnumHand;
import net.minecraft.server.v1_13_R2.EnumInteractionResult;
import net.minecraft.server.v1_13_R2.Vec3D;
import net.minecraft.server.v1_13_R2.World;
import org.bukkit.Location;

public class EntityStand extends EntityArmorStand {

	EntityStand(World world, Location location) {
		super(world, location.getX(), location.getY(), location.getZ());
		setNoGravity(true);
		setInvisible(true);
		setMarker(true);
		world.addEntity(this);
	}

	@Override
	public EnumInteractionResult a(EntityHuman entityhuman, Vec3D vec3d, EnumHand enumhand) {
		return EnumInteractionResult.PASS;
	}

}
