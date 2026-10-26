package daybreak.abilitywar.utils.base.minecraft.nms.v1_19_R2;

import io.netty.channel.Channel;
import java.lang.reflect.Field;
import net.minecraft.network.NetworkManager;
import net.minecraft.server.level.EntityPlayer;
import net.minecraft.server.network.PlayerConnection;

public final class ChannelUtil {
    private static final Field CONNECTION;
    static {
        try {
            CONNECTION = PlayerConnection.class.getDeclaredField("b");
            CONNECTION.setAccessible(true);
        } catch (ReflectiveOperationException exception) {
            throw new ExceptionInInitializerError(exception);
        }
    }
    public static Channel getChannel(final EntityPlayer handle) {
        try {
            return ((NetworkManager) CONNECTION.get(handle.connection)).channel;
        } catch (IllegalAccessException exception) {
            throw new IllegalStateException(exception);
        }
    }
}
