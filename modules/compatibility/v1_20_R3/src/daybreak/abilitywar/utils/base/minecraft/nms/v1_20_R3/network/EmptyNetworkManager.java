package daybreak.abilitywar.utils.base.minecraft.nms.v1_20_R3.network;


import net.minecraft.network.NetworkManager;
import net.minecraft.network.PacketListener;
import net.minecraft.network.PacketSendListener;
import net.minecraft.network.protocol.EnumProtocolDirection;
import net.minecraft.network.protocol.Packet;

import javax.annotation.Nullable;
import java.net.SocketAddress;

public class EmptyNetworkManager extends NetworkManager {


	public EmptyNetworkManager(EnumProtocolDirection flag) throws IllegalAccessException {
		super(flag);
		this.channel = new EmptyChannel(null);
		this.address = new SocketAddress() {};
	}

	@Override
	public void flushChannel() {}

	@Override
	public boolean isConnecting() {
		return true;
	}

	@Override
	public void send(Packet<?> packet) {}

	@Override
	public void send(Packet<?> packet, @Nullable PacketSendListener packetsendlistener) {}

	@Override
	public void send(Packet<?> packet, @Nullable PacketSendListener packetsendlistener, boolean flag) {}

	@Override
	public void setListener(PacketListener packetlistener) {}

	@Override
	public boolean isConnected() {
		return true;
	}

}