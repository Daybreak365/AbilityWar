package daybreak.abilitywar.utils.base.minecraft.nms.v1_9_R1.network;

import net.minecraft.server.v1_9_R1.EnumProtocolDirection;
import net.minecraft.server.v1_9_R1.NetworkManager;
import net.minecraft.server.v1_9_R1.Packet;

import java.lang.reflect.Field;
import java.net.SocketAddress;

public class EmptyNetworkManager extends NetworkManager {

    private static final Field NETWORK_ADDRESS;

    static {
        try {
            NETWORK_ADDRESS = NetworkManager.class.getDeclaredField("l");
        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        }
    }

    private boolean connected = true;

    public EmptyNetworkManager(EnumProtocolDirection flag) throws IllegalAccessException {
        super(flag);
        this.channel = new EmptyChannel(null);
        NETWORK_ADDRESS.setAccessible(true);
        NETWORK_ADDRESS.set(this, new SocketAddress() {});
    }

    @Override
    public boolean isConnected() {
        return connected;
    }

    public void setConnected(final boolean connected) {
         this.connected = connected;
    }

    @Override
    public void sendPacket(Packet packet) {
    }
}