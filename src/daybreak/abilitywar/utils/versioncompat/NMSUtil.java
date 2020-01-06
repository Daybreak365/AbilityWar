package daybreak.abilitywar.utils.versioncompat;

import org.bukkit.entity.Player;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.logging.Level;
import java.util.logging.Logger;

public class NMSUtil {

	private static Method sendPacket = null;
	private static Method a = null;

	public static Object getHandle(Object object) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
		return object.getClass().getMethod("getHandle").invoke(object);
	}

	public static void sendPacket(Player player, Object packet) {
		try {
			Object handle = getHandle(player);
			Object playerConnection = handle.getClass().getField("playerConnection").get(handle);
			if (sendPacket == null) {
				sendPacket = playerConnection.getClass().getMethod("sendPacket", NMSUtil.getNMSClass("Packet"));
			}
			sendPacket.invoke(playerConnection, packet);
		} catch (NoSuchMethodException | NoSuchFieldException | IllegalAccessException | InvocationTargetException ignored) {
		}
	}

	public static void a(Player player, Object packet) {
		try {
			Object handle = getHandle(player);
			Object playerConnection = handle.getClass().getField("playerConnection").get(handle);
			if (a == null) {
				a = playerConnection.getClass().getMethod("a", packet.getClass());
			}
			a.invoke(playerConnection, packet);
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
				a(player, constructor.newInstance(PERFORM_RESPAWN));
			} catch (IllegalAccessException | InstantiationException | InvocationTargetException | NoSuchMethodException ex) {
				logger.log(Level.SEVERE, player.getName() + " 플레이어를 리스폰하는 도중 오류가 발생하였습니다.");
			}
		}

		private static final Class<?> PacketPlayOutTitle = NMSUtil.getNMSClass("PacketPlayOutTitle");
		private static Constructor<?> constructor = null;
		private static final Class<?> IChatBaseComponent = NMSUtil.getNMSClass("IChatBaseComponent");
		private static Method newIChatBaseComponent = null;
		private static Object TIMES = null;
		private static Object ACTIONBAR = null;

		static {
			try {
				constructor = PacketPlayOutTitle.getConstructor(
						PacketPlayOutTitle.getDeclaredClasses()[0],
						IChatBaseComponent, int.class, int.class, int.class);
				newIChatBaseComponent = IChatBaseComponent.getDeclaredClasses()[0].getMethod("a", String.class);
				TIMES = PacketPlayOutTitle.getDeclaredClasses()[0].getField("TIMES").get(null);
				ACTIONBAR = PacketPlayOutTitle.getDeclaredClasses()[0].getField("ACTIONBAR").get(null);
			} catch (NoSuchMethodException | NoSuchFieldException | IllegalAccessException e) {
				logger.log(Level.SEVERE, "액션바 메시지 전송 기능을 초기화하는 도중 오류가 발생하였습니다.");
			}
		}

		public static void sendActionbar(Player player, String message, int fadeIn, int stay, int fadeOut) {
			try {
				Object actionbar = newIChatBaseComponent.invoke(null, "{\"text\": \"" + message + "\"}");
				sendPacket(player, constructor.newInstance(TIMES, actionbar, fadeIn, stay, fadeOut));
				sendPacket(player, constructor.newInstance(ACTIONBAR, actionbar, fadeIn, stay, fadeOut));
			} catch (IllegalAccessException | InvocationTargetException | InstantiationException | NullPointerException e) {
				logger.log(Level.SEVERE, "액션바 메시지를 보내는 도중 " + e.getClass().getSimpleName() + " 오류가 발생하였습니다.");
			}
		}

	}

}
