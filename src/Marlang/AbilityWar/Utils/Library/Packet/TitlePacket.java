package Marlang.AbilityWar.Utils.Library.Packet;

import java.lang.reflect.Constructor;

import org.bukkit.entity.Player;

import Marlang.AbilityWar.Utils.VersionCompat.ServerVersion;

public class TitlePacket extends AbstractPacket {
	
	private String Title;
	private String SubTitle;
	private int fadeIn;
	private int stay;
	private int fadeOut;

	/**
	 * 제목 메시지
	 * @param Title 	제목
	 * @param SubTitle 	부제목
	 * @param fadeIn 	FadeIn 시간 (틱 단위)
	 * @param stay 		Stay 시간 (틱 단위)
	 * @param fadeOut 	FadeOut 시간 (틱 단위)
	 */
	public TitlePacket(String Title, String SubTitle, int fadeIn, int stay, int fadeOut) {
		this.Title = Title;
		this.SubTitle = SubTitle;
		this.fadeIn = fadeIn;
		this.stay = stay;
		this.fadeOut = fadeOut;
	}

	public void Send(Player p) {
		if(ServerVersion.getVersion() >= 8) {
			try {
				if(ServerVersion.getVersion() >= 11) {
					p.sendTitle(Title, SubTitle, fadeIn, stay, fadeOut);
				} else if(ServerVersion.getVersion() >= 9) {
					Object Title = getNMSClass("IChatBaseComponent").getDeclaredClasses()[0]
							.getMethod("a", String.class)
							.invoke(null, "{\"text\": \"" + this.Title + "\"}");
					Object SubTitle = getNMSClass("IChatBaseComponent").getDeclaredClasses()[0]
							.getMethod("a", String.class)
							.invoke(null, "{\"text\": \"" + this.SubTitle + "\"}");
					
					Constructor<?> Constructor = getNMSClass("PacketPlayOutTitle").getConstructor(
							getNMSClass("PacketPlayOutTitle").getDeclaredClasses()[0],
							getNMSClass("IChatBaseComponent"), int.class, int.class, int.class);
					Object TimePacket = Constructor.newInstance(
							getNMSClass("PacketPlayOutTitle").getDeclaredClasses()[0].getField("TIMES").get(null),
							Title, this.fadeIn, this.stay, this.fadeOut);
					Object TitlePacket = Constructor.newInstance(
							getNMSClass("PacketPlayOutTitle").getDeclaredClasses()[0].getField("TITLE").get(null),
							Title, this.fadeIn, this.stay, this.fadeOut);
					Object SubTitlePacket = Constructor.newInstance(
							getNMSClass("PacketPlayOutTitle").getDeclaredClasses()[0].getField("SUBTITLE").get(null),
							SubTitle, this.fadeIn, this.stay, this.fadeOut);

					sendPacket(p, TimePacket);
					sendPacket(p, TitlePacket);
					sendPacket(p, SubTitlePacket);
				} else {
					Object Title = getNMSClass("ChatSerializer").getMethod("a", String.class)
							.invoke(null, "{\"text\": \"" + this.Title + "\"}");
					Object SubTitle = getNMSClass("ChatSerializer").getMethod("a", String.class)
							.invoke(null, "{\"text\": \"" + this.SubTitle + "\"}");
					
					Constructor<?> Constructor = getNMSClass("PacketPlayOutTitle").getConstructor(
							getNMSClass("EnumTitleAction"),
							getNMSClass("IChatBaseComponent"), int.class, int.class, int.class);
					Object TimePacket = Constructor.newInstance(
							getNMSClass("EnumTitleAction").getField("TIMES").get(null),
							Title, this.fadeIn, this.stay, this.fadeOut);
					Object TitlePacket = Constructor.newInstance(
							getNMSClass("EnumTitleAction").getField("TITLE").get(null),
							Title, this.fadeIn, this.stay, this.fadeOut);
					Object SubTitlePacket = Constructor.newInstance(
							getNMSClass("EnumTitleAction").getField("SUBTITLE").get(null),
							SubTitle, this.fadeIn, this.stay, this.fadeOut);

					sendPacket(p, TimePacket);
					sendPacket(p, TitlePacket);
					sendPacket(p, SubTitlePacket);
				}
			} catch(Exception ex) {}
		} else {
			p.sendMessage(Title + " " + SubTitle);
		}
	}
	
}
