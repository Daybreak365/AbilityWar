package daybreak.abilitywar.ability.list.grapplinghook.v1_17_R1;

import net.minecraft.world.EnumHand;
import net.minecraft.world.EnumInteractionResult;
import net.minecraft.world.entity.decoration.EntityArmorStand;
import net.minecraft.world.entity.player.EntityHuman;
import net.minecraft.world.level.World;
import net.minecraft.world.phys.Vec3D;
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
		return EnumInteractionResult.c;
	}

}
