package daybreak.abilitywar.utils.base.minecraft.nms.v1_18_R1.network;


import io.netty.channel.embedded.EmbeddedChannel;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.protocol.EnumProtocolDirection;
import net.minecraft.network.protocol.Packet;

import java.net.SocketAddress;

public class EmptyNetworkManager extends NetworkManager {

	private boolean connected = true;

	public EmptyNetworkManager(EnumProtocolDirection flag) throws IllegalAccessException {
		super(flag);
		this.channel = new EmbeddedChannel();
		this.address = new SocketAddress() {};
	}

	@Override
	public boolean isConnected() {
		return connected;
	}

	public void setConnected(final boolean connected) {
		this.connected = connected;
	}

	@Override
	public void send(Packet packet) {
	}
}