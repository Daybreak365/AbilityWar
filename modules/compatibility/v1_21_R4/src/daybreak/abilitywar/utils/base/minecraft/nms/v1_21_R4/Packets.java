package daybreak.abilitywar.utils.base.minecraft.nms.v1_21_R4;

import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.network.protocol.game.ClientboundTeleportEntityPacket;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.PositionMoveRotation;
import java.util.Collections;

final class Packets {
	private Packets() {}
	static ClientboundAddEntityPacket spawn(Entity entity) {
		return new ClientboundAddEntityPacket(entity.getId(), entity.getUUID(), entity.getX(), entity.getY(), entity.getZ(),
				entity.getXRot(), entity.getYRot(), entity.getType(), 0, entity.getDeltaMovement(), entity.getYHeadRot());
	}
	static ClientboundTeleportEntityPacket teleport(Entity entity) {
		return new ClientboundTeleportEntityPacket(entity.getId(), PositionMoveRotation.of(entity), Collections.emptySet(), entity.onGround());
	}
}
