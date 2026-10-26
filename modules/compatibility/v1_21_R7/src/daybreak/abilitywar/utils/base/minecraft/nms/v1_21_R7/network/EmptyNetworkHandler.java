package daybreak.abilitywar.utils.base.minecraft.nms.v1_21_R7.network;

import net.minecraft.network.Connection;
import net.minecraft.network.protocol.Packet;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.CommonListenerCookie;
import net.minecraft.server.network.ServerGamePacketListenerImpl;

public class EmptyNetworkHandler extends ServerGamePacketListenerImpl {

	public EmptyNetworkHandler(MinecraftServer minecraftServer, Connection networkManager, ServerPlayer entityPlayer) {
		super(minecraftServer, networkManager, entityPlayer, CommonListenerCookie.createInitial(entityPlayer.getGameProfile(), false));
	}

	@Override
	public void send(Packet<?> packet) {
	}
}
