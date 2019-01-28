package Marlang.AbilityWar.Utils;

import java.lang.reflect.Constructor;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import Marlang.AbilityWar.Utils.AutoUpdate.ServerVersion;

public class PacketUtil {
	
	private static Integer Version = ServerVersion.getVersion();
	
	private static Class<?> getNMSClass(String className) {
		try {
			return Class.forName("net.minecraft.server." + getVersionString() + "." + className);
		} catch (Exception ex) {
			return null;
		}
	}

	private static void sendPacket(Player player, Object packet) {
		try {
			Object handle = player.getClass().getMethod("getHandle").invoke(player);
			Object playerConnection = handle.getClass().getField("playerConnection").get(handle);
			playerConnection.getClass().getMethod("sendPacket", getNMSClass("Packet")).invoke(playerConnection, packet);
		} catch (Exception ex) {}
	}

	/**
	 * 서버 버전을 String으로 받아옵니다. Ex. v1_12_R1
	 */
	private static String getVersionString() {
		String[] versionArray = Bukkit.getServer().getClass().getName().replace('.', ',').split(",");
		if (versionArray.length >= 4) {
			return versionArray[3];
		} else {
			return "";
		}
	}

	public static class TitleObject {

		String Title;
		String SubTitle;

		public TitleObject(String Title, String SubTitle) {
			this.Title = Title;
			this.SubTitle = SubTitle;
		}

		public void Send(Player p, int fadeIn, int stay, int fadeOut) {
			try {
				if (Version >= 11) {
					p.sendTitle(getTitle(), getSubTitle(), fadeIn, stay, fadeOut);
				} else if(Version >= 9) {
					Object Title = getNMSClass("IChatBaseComponent").getDeclaredClasses()[0]
							.getMethod("a", String.class)
							.invoke(null, "{\"text\": \"" + getTitle() + "\"}");
					Object SubTitle = getNMSClass("IChatBaseComponent").getDeclaredClasses()[0]
							.getMethod("a", String.class)
							.invoke(null, "{\"text\": \"" + getSubTitle() + "\"}");

					Constructor<?> Constructor = getNMSClass("PacketPlayOutTitle").getConstructor(
							getNMSClass("PacketPlayOutTitle").getDeclaredClasses()[0],
							getNMSClass("IChatBaseComponent"), int.class, int.class, int.class);
					Object TitlePacket = Constructor.newInstance(
							getNMSClass("PacketPlayOutTitle").getDeclaredClasses()[0].getField("TITLE").get(null),
							Title, fadeIn, stay, fadeOut);
					Object SubTitlePacket = Constructor.newInstance(
							getNMSClass("PacketPlayOutTitle").getDeclaredClasses()[0].getField("SUBTITLE").get(null),
							SubTitle, fadeIn, stay, fadeOut);

					sendPacket(p, TitlePacket);
					sendPacket(p, SubTitlePacket);
				}
			} catch(Exception ex) {}

		}

		private String getTitle() {
			return Title;
		}

		private String getSubTitle() {
			return SubTitle;
		}

	}

	public static class ActionbarObject {

		String Message;

		public ActionbarObject(String Message) {
			this.Message = Message;
		}

		public void Send(Player p) {
			try {
				Object Message = getNMSClass("IChatBaseComponent").getDeclaredClasses()[0]
						.getMethod("a", String.class)
						.invoke(null, "{\"text\": \"" + getMessage() + "\"}");
				
				Constructor<?> Constructor = getNMSClass("PacketPlayOutChat").getConstructor(
						getNMSClass("IChatBaseComponent"), getNMSClass("ChatMessageType"));
				Object ActionbarPacket = Constructor.newInstance(Message, getNMSClass("ChatMessageType").getField("GAME_INFO").get(null));

				sendPacket(p, ActionbarPacket);
			} catch(Exception ex) {}

		}

		private String getMessage() {
			return Message;
		}
		
	}

}
