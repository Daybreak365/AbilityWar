package daybreak.abilitywar.utils.base.minecraft.nms.v1_21_R1.network;

import io.netty.channel.embedded.EmbeddedChannel;
import net.minecraft.network.PacketSendListener;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketFlow;
import java.net.InetSocketAddress;

public class EmptyNetworkManager extends Connection {
	public EmptyNetworkManager(PacketFlow flow) {
		super(flow);
		channel = new EmbeddedChannel();
		address = new InetSocketAddress("127.0.0.1", 0);
	}
	@Override public void send(Packet<?> packet) {}
	@Override public void send(Packet<?> packet, PacketSendListener listener) {}
	@Override public void send(Packet<?> packet, PacketSendListener listener, boolean flush) {}
	@Override public boolean isConnected() { return true; }
}
