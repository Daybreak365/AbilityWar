package Marlang.AbilityWar.Utils.PacketLib;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import Marlang.AbilityWar.Utils.AutoUpdate.ServerVersion;

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
			return Class.forName("net.minecraft.server." + getVersionString() + "." + className);
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
	
	/**
	 * 서버 버전을 String으로 받아옵니다. Ex. v1_12_R1
	 */
	private String getVersionString() {
		String[] versionArray = Bukkit.getServer().getClass().getName().replace('.', ',').split(",");
		if (versionArray.length >= 4) {
			return versionArray[3];
		} else {
			return "";
		}
	}
	
	protected Integer getVersion() {
		return Version;
	}
	
}
