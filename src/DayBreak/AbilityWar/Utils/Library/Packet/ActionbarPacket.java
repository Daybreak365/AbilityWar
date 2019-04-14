package DayBreak.AbilityWar.Utils.Library.Packet;

import java.lang.reflect.Constructor;

import org.bukkit.entity.Player;

import DayBreak.AbilityWar.Utils.VersionCompat.ServerVersion;

/**
 * 액션바 패킷
 * @author DayBreak 새벽
 */
public class ActionbarPacket extends AbstractPacket {
	
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
	public ActionbarPacket(String Message, int fadeIn, int stay, int fadeOut) {
		this.Message = Message;
		this.fadeIn = fadeIn;
		this.stay = stay;
		this.fadeOut = fadeOut;
	}

	public void Send(Player p) {
		if(ServerVersion.getVersion() >= 8) {
			try {
				Object Actionbar = getNMSClass("IChatBaseComponent").getDeclaredClasses()[0]
						.getMethod("a", String.class)
						.invoke(null, "{\"text\": \"" + this.Message + "\"}");

				Constructor<?> Constructor = getNMSClass("PacketPlayOutTitle").getConstructor(
						getNMSClass("PacketPlayOutTitle").getDeclaredClasses()[0],
						getNMSClass("IChatBaseComponent"), int.class, int.class, int.class);
				Object TimePacket = Constructor.newInstance(
						getNMSClass("PacketPlayOutTitle").getDeclaredClasses()[0].getField("TIMES").get(null),
						Actionbar, this.fadeIn, this.stay, this.fadeOut);
				Object ActionbarPacket = Constructor.newInstance(
						getNMSClass("PacketPlayOutTitle").getDeclaredClasses()[0].getField("ACTIONBAR").get(null),
						Actionbar, fadeIn, stay, fadeOut);

				sendPacket(p, TimePacket);
				sendPacket(p, ActionbarPacket);
			} catch(Exception ex) {}
		}
	}
	
}
