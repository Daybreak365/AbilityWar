package daybreak.abilitywar.utils.base.minecraft.nms.v1_21_R7;

import io.netty.channel.Channel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerCommonPacketListenerImpl;
import net.minecraft.network.Connection;
import java.lang.reflect.Field;

public final class ChannelUtil {
	private static final Field CONNECTION;
	static {
		try {
			Field connection = null;
			for (Field field : ServerCommonPacketListenerImpl.class.getDeclaredFields()) {
				if (field.getType() == Connection.class) {
					if (connection != null) throw new NoSuchFieldException("Ambiguous Connection field");
					connection = field;
				}
			}
			if (connection == null) throw new NoSuchFieldException("Connection field");
			connection.setAccessible(true);
			CONNECTION = connection;
		} catch (ReflectiveOperationException e) { throw new ExceptionInInitializerError(e); }
	}
	public static Channel getChannel(ServerPlayer player) {
		try { return ((Connection) CONNECTION.get(player.connection)).channel; }
		catch (IllegalAccessException e) { throw new IllegalStateException(e); }
	}
}
