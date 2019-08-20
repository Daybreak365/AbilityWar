package DayBreak.AbilityWar.Utils.Library.TItle;

import java.lang.reflect.Constructor;

import org.bukkit.entity.Player;

import DayBreak.AbilityWar.Utils.VersionCompat.ServerVersion;

/**
 * 액션바 메시지
 * @author DayBreak 새벽
 */
public class Actionbar extends AbstractTitle {
	
	private String Message;
	private int fadeIn;
	private int stay;
	private int fadeOut;

	/**
	 * 액션바 메시지
	 * @param Message 	메시지
	 * @param fadeIn 	FadeIn 시간 (틱 단위)
	 * @param stay 		Stay 시간 (틱 단위)
	 * @param fadeOut 	FadeOut 시간 (틱 단위)
	 */
	public Actionbar(String Message, int fadeIn, int stay, int fadeOut) {
		this.Message = Message;
		this.fadeIn = fadeIn;
		this.stay = stay;
		this.fadeOut = fadeOut;
	}

	private static final Class<?> PacketPlayOutTitle = getNMSClass("PacketPlayOutTitle");
	private static final Class<?> IChatBaseComponent = getNMSClass("IChatBaseComponent");
	private static final Class<?> Packet = getNMSClass("Packet");

	private static Class<?> getNMSClass(String className) {
		try {
			return Class.forName("net.minecraft.server." + ServerVersion.getStringVersion() + "." + className);
		} catch (Exception ex) {
			return null;
		}
	}

	public void sendTo(Player p) {
		try {
			Object Actionbar = IChatBaseComponent.getDeclaredClasses()[0].getMethod("a", String.class)
					.invoke(null, "{\"text\": \"" + this.Message + "\"}");

			Constructor<?> Constructor = PacketPlayOutTitle.getConstructor(
					PacketPlayOutTitle.getDeclaredClasses()[0],
					IChatBaseComponent, int.class, int.class, int.class);
			Object TimePacket = Constructor.newInstance(
					PacketPlayOutTitle.getDeclaredClasses()[0].getField("TIMES").get(null),
					Actionbar, this.fadeIn, this.stay, this.fadeOut);
			Object ActionbarPacket = Constructor.newInstance(
					PacketPlayOutTitle.getDeclaredClasses()[0].getField("ACTIONBAR").get(null),
					Actionbar, fadeIn, stay, fadeOut);

			sendPacket(p, TimePacket);
			sendPacket(p, ActionbarPacket);
		} catch(Exception ex) {}
	}

	private static void sendPacket(Player player, Object packet) {
		try {
			Object handle = player.getClass().getMethod("getHandle").invoke(player);
			Object playerConnection = handle.getClass().getField("playerConnection").get(handle);
			playerConnection.getClass().getMethod("sendPacket", Packet).invoke(playerConnection, packet);
		} catch (Exception ex) {}
	}
	
}
