package daybreak.abilitywar.utils.base.minecraft.nms.v1_20_R3.network;


import net.minecraft.network.NetworkManager;
import net.minecraft.network.protocol.Packet;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.EntityPlayer;
import net.minecraft.server.network.CommonListenerCookie;
import net.minecraft.server.network.PlayerConnection;
import net.minecraft.world.entity.player.EntityHuman;

public class EmptyNetworkHandler extends PlayerConnection {

	public EmptyNetworkHandler(MinecraftServer minecraftServer, NetworkManager networkManager, EntityPlayer entityPlayer) {
		super(minecraftServer, networkManager, entityPlayer, new CommonListenerCookie(((EntityHuman) entityPlayer).getGameProfile(), 0, entityPlayer.clientInformation()));
	}

	@Override
	public void send(Packet<?> packet) {
	}
}