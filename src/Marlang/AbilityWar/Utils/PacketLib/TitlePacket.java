package Marlang.AbilityWar.Utils.PacketLib;

import java.lang.reflect.Constructor;

import org.bukkit.entity.Player;

public class TitlePacket extends AbstractPacket {
	
	private String Title;
	private String SubTitle;
	private int fadeIn;
	private int stay;
	private int fadeOut;
	
	public TitlePacket(String Title, String SubTitle, int fadeIn, int stay, int fadeOut) {
		this.Title = Title;
		this.SubTitle = SubTitle;
		this.fadeIn = fadeIn;
		this.stay = stay;
		this.fadeOut = fadeOut;
	}

	public void Send(Player p) {
		try {
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
		} catch(Exception ex) {}
	}
	
}
