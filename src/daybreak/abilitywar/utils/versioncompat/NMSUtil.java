package daybreak.abilitywar.utils.versioncompat;

import org.bukkit.entity.Player;

import java.lang.reflect.InvocationTargetException;

public class NMSUtil {

	public static void sendPacket(Player player, Object packet) {
		try {
			Object handle = player.getClass().getMethod("getHandle").invoke(player);
			Object playerConnection = handle.getClass().getField("playerConnection").get(handle);
			playerConnection.getClass().getMethod("a", packet.getClass()).invoke(playerConnection, packet);
		} catch (NoSuchMethodException | NoSuchFieldException | IllegalAccessException | InvocationTargetException ignored) {
		}
	}

	public static Class<?> getNMSClass(String className) {
		try {
			return Class.forName("net.minecraft.server." + ServerVersion.getStringVersion() + "." + className);
		} catch (Exception ex) {
			return null;
		}
	}

}
