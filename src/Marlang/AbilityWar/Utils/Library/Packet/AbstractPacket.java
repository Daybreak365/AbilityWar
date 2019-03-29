package Marlang.AbilityWar.Utils.Library.Packet;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import Marlang.AbilityWar.Utils.VersionCompat.ServerVersion;

abstract public class AbstractPacket {
	
	abstract public void Send(Player p);
	
	public void Broadcast() {
		for(Player p : Bukkit.getOnlinePlayers()) {
			this.Send(p);
		}
	}
	
	private Integer Version = ServerVersion.getVersion();
	
	protected Class<?> getNMSClass(String className) {
		try {
			return Class.forName("net.minecraft.server." + ServerVersion.getStringVersion() + "." + className);
		} catch (Exception ex) {
			return null;
		}
	}
	
	protected void sendPacket(Player player, Object packet) {
		try {
			Object handle = player.getClass().getMethod("getHandle").invoke(player);
			Object playerConnection = handle.getClass().getField("playerConnection").get(handle);
			playerConnection.getClass().getMethod("sendPacket", getNMSClass("Packet")).invoke(playerConnection, packet);
		} catch (Exception ex) {}
	}
	
	protected Integer getVersion() {
		return Version;
	}
	
}
