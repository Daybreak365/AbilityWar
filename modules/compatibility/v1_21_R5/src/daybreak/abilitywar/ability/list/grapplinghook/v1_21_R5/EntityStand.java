package daybreak.abilitywar.ability.list.grapplinghook.v1_21_R5;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.LevelWriter;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.bukkit.Location;

public class EntityStand extends ArmorStand {

	EntityStand(Level world, Location location) {
		super(world, location.getX(), location.getY(), location.getZ());
		((Entity) this).setNoGravity(true);
		setInvisible(true);
		setMarker(true);
		((LevelWriter) world).addFreshEntity(this);
	}

	@Override
	public InteractionResult interactAt(Player entityhuman, Vec3 vec3d, InteractionHand enumhand) {
		return InteractionResult.CONSUME;
	}

}
