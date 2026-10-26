package daybreak.abilitywar.utils.base.minecraft.nms.v26_2;

import io.netty.channel.Channel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerCommonPacketListenerImpl;
import net.minecraft.network.Connection;
import java.lang.reflect.Field;

public final class ChannelUtil {
	private static final Field CONNECTION;
	static {
		try {
			CONNECTION = ServerCommonPacketListenerImpl.class.getDeclaredField("connection");
			CONNECTION.setAccessible(true);
		} catch (ReflectiveOperationException e) { throw new ExceptionInInitializerError(e); }
	}
	public static Channel getChannel(ServerPlayer player) {
		try { return ((Connection) CONNECTION.get(player.connection)).channel; }
		catch (IllegalAccessException e) { throw new IllegalStateException(e); }
	}
}
