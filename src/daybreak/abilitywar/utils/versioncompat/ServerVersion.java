package daybreak.abilitywar.utils.versioncompat;

import daybreak.abilitywar.utils.Messager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;

import java.lang.reflect.Field;

/**
 * Server Version
 * @author DayBreak 새벽
 */
public class ServerVersion {

	private ServerVersion() {}

	private static final Messager messager = new Messager();
	private static String VersionString = getVersionString();
	private static int Version = getSimpleVersion();
	
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
	
	/**
	 * 서버 버전을 간단한 Int로 받아옵니다. Ex. 1.14.3 => 14
	 */
	private static int getSimpleVersion() {
		int Version = -1;
		String[] versionArray = VersionString.split("_");
		if (versionArray.length >= 2) {
			try {
				Version = Integer.valueOf(versionArray[1]);
			} catch (NumberFormatException ex) {
				//Ignore: Should Never Happen
			}
		}

		return Version;
	}
	
	public static int getVersion() {
		return Version;
	}
	
	public static String getStringVersion() {
		return VersionString;
	}
	
	/**
	 * 버전 호환 작업
	 */
	public static void VersionCompat(Plugin plugin) {
		if(getVersion() >= 12) {
			if(getVersion() >= 13) {
				setAPIVersion(plugin, "1." + getVersion());
			}
		} else {
			messager.sendConsoleMessage(ChatColor.translateAlternateColorCodes('&', "&f플러그인이 지원하지 않는 버전을 이용하고 있습니다."));
			Bukkit.getPluginManager().disablePlugin(plugin);
		}
	}
	
	private static void setAPIVersion(Plugin plugin, String Version) {
		try {
			PluginDescriptionFile desc = plugin.getDescription();
			Field apiVersion = PluginDescriptionFile.class.getDeclaredField("apiVersion");
			apiVersion.setAccessible(true);
			apiVersion.set(desc, Version);
			apiVersion.setAccessible(false);
		} catch (IllegalAccessException | NoSuchFieldException e) {
			//Ignore: Should Never Happen
		}
	}

}
