package abilitywar.ability.list.grapplinghook.v1_20_R1;

import net.minecraft.world.EnumHand;
import net.minecraft.world.EnumInteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.decoration.EntityArmorStand;
import net.minecraft.world.entity.player.EntityHuman;
import net.minecraft.world.level.IWorldWriter;
import net.minecraft.world.level.World;
import net.minecraft.world.phys.Vec3D;
import org.bukkit.Location;

public class EntityStand extends EntityArmorStand {

	EntityStand(World world, Location location) {
		super(world, location.getX(), location.getY(), location.getZ());
		((Entity) this).setNoGravity(true);
		setInvisible(true);
		setMarker(true);
		((IWorldWriter) world).addFreshEntity(this);
	}

	@Override
	public EnumInteractionResult interactAt(EntityHuman entityhuman, Vec3D vec3d, EnumHand enumhand) {
		return EnumInteractionResult.CONSUME_PARTIAL;
	}

}
