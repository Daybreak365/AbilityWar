package daybreak.abilitywar.utils.base.minecraft.nms.v1_16_R2.network;

import net.minecraft.server.v1_16_R2.EnumProtocolDirection;
import net.minecraft.server.v1_16_R2.NetworkManager;
import net.minecraft.server.v1_16_R2.Packet;

import java.lang.reflect.Field;
import java.net.SocketAddress;

public class EmptyNetworkManager extends NetworkManager {

    private static final Field NETWORK_ADDRESS;

    static {
        try {
            NETWORK_ADDRESS = NetworkManager.class.getDeclaredField("socketAddress");
        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        }
    }

    public EmptyNetworkManager(EnumProtocolDirection flag) throws IllegalAccessException {
        super(flag);
        this.channel = new EmptyChannel(null);
        NETWORK_ADDRESS.setAccessible(true);
        NETWORK_ADDRESS.set(this, new SocketAddress() {});
    }

    @Override
    public boolean isConnected() {
        return true;
    }

    @Override
    public void sendPacket(Packet packet) {
    }
}