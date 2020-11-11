package daybreak.abilitywar.ability.list.grapplinghook.v1_16_R3;

import net.minecraft.server.v1_16_R3.EntityArmorStand;
import net.minecraft.server.v1_16_R3.EntityHuman;
import net.minecraft.server.v1_16_R3.EnumHand;
import net.minecraft.server.v1_16_R3.EnumInteractionResult;
import net.minecraft.server.v1_16_R3.Vec3D;
import net.minecraft.server.v1_16_R3.World;
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
