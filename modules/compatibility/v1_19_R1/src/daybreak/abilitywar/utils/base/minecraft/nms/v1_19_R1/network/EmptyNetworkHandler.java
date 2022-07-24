package daybreak.abilitywar.utils.base.minecraft.nms.v1_19_R1.network;


import net.minecraft.network.NetworkManager;
import net.minecraft.network.protocol.Packet;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.EntityPlayer;
import net.minecraft.server.network.PlayerConnection;

public class EmptyNetworkHandler extends PlayerConnection {

	public EmptyNetworkHandler(MinecraftServer minecraftServer, NetworkManager networkManager, EntityPlayer entityPlayer) {
		super(minecraftServer, networkManager, entityPlayer);
	}

	@Override
	public void send(Packet<?> packet) {
	}
}