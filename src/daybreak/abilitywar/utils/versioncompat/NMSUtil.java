package daybreak.abilitywar.utils.versioncompat;

import org.bukkit.entity.Player;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.logging.Level;
import java.util.logging.Logger;

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

	public static class PlayerUtil {

		private static final Logger logger = Logger.getLogger(PlayerUtil.class.getName());

		private static final Class<?> PacketPlayInClientCommand = NMSUtil.getNMSClass("PacketPlayInClientCommand");
		private static final Class<?> EnumClientCommand = PacketPlayInClientCommand.getDeclaredClasses()[0];
		private static Object PERFORM_RESPAWN;

		static {
			try {
				PERFORM_RESPAWN = EnumClientCommand.getField("PERFORM_RESPAWN").get(null);
			} catch (NoSuchFieldException | IllegalAccessException e) {
				logger.log(Level.SEVERE, "리스폰 기능을 초기화하는 도중 오류가 발생하였습니다.");
			}
		}

		public static void respawn(Player player) {
			try {
				Constructor<?> constructor = PacketPlayInClientCommand.getConstructor(EnumClientCommand);
				NMSUtil.sendPacket(player, constructor.newInstance(PERFORM_RESPAWN));
			} catch (IllegalAccessException | InstantiationException | InvocationTargetException | NoSuchMethodException ex) {
				logger.log(Level.SEVERE, player.getName() + " 플레이어를 리스폰하는 도중 오류가 발생하였습니다.");
			}
		}

	}

}
