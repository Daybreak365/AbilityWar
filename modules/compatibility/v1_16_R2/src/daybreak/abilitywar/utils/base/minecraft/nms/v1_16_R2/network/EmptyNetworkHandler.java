package daybreak.abilitywar.utils.base.minecraft.nms.v1_16_R2.network;

import net.minecraft.server.v1_16_R2.EntityPlayer;
import net.minecraft.server.v1_16_R2.MinecraftServer;
import net.minecraft.server.v1_16_R2.NetworkManager;
import net.minecraft.server.v1_16_R2.Packet;
import net.minecraft.server.v1_16_R2.PlayerConnection;

public class EmptyNetworkHandler extends PlayerConnection {

	public EmptyNetworkHandler(MinecraftServer minecraftServer, NetworkManager networkManager, EntityPlayer entityPlayer) {
		super(minecraftServer, networkManager, entityPlayer);
	}

	@Override
	public void sendPacket(Packet<?> packet) {}
}