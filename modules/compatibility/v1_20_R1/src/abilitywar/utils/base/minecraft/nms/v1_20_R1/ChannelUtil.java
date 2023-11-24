package abilitywar.utils.base.minecraft.nms.v1_20_R1;

import io.netty.channel.Channel;
import net.minecraft.server.level.EntityPlayer;
import net.minecraft.server.network.ServerCommonPacketListenerImpl;

public class ChannelUtil {

    public static Channel getChannel(final EntityPlayer handle) {
        try {
            return (Channel) ServerCommonPacketListenerImpl.class.getDeclaredField("connection").get(handle.connection);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

}
