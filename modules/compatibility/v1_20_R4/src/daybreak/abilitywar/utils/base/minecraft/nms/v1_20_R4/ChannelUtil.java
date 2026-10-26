package daybreak.abilitywar.utils.base.minecraft.nms.v1_20_R4;

import io.netty.channel.Channel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerCommonPacketListenerImpl;
import net.minecraft.network.Connection;
import java.lang.reflect.Field;

public final class ChannelUtil {
	private static final Field CONNECTION;
	static {
		try {
			CONNECTION = findConnection();
			CONNECTION.setAccessible(true);
		} catch (ReflectiveOperationException e) { throw new ExceptionInInitializerError(e); }
	}
	private static Field findConnection() throws NoSuchFieldException {
		for (Field field : ServerCommonPacketListenerImpl.class.getDeclaredFields()) {
			if (field.getType() == Connection.class) return field;
		}
		throw new NoSuchFieldException("Server listener connection");
	}

	public static Channel getChannel(ServerPlayer player) {
		try { return ((Connection) CONNECTION.get(player.connection)).channel; }
		catch (IllegalAccessException e) { throw new IllegalStateException(e); }
	}
}
