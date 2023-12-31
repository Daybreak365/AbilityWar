package daybreak.abilitywar.utils.base.minecraft.nms.v1_20_R3;

import io.netty.channel.Channel;
import net.minecraft.network.NetworkManager;
import net.minecraft.server.level.EntityPlayer;
import net.minecraft.server.network.ServerCommonPacketListenerImpl;

public class ChannelUtil {

    public static Channel getChannel(final EntityPlayer handle) {
        try {
            return ((NetworkManager) ServerCommonPacketListenerImpl.class.getDeclaredField("c").get(handle.connection)).channel;
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

}
