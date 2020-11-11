package daybreak.abilitywar.ability.list.grapplinghook.v1_15_R1;

import net.minecraft.server.v1_15_R1.World;
import net.minecraft.server.v1_15_R1.EntityArmorStand;
import net.minecraft.server.v1_15_R1.EntityHuman;
import net.minecraft.server.v1_15_R1.EnumHand;
import net.minecraft.server.v1_15_R1.EnumInteractionResult;
import net.minecraft.server.v1_15_R1.Vec3D;
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
