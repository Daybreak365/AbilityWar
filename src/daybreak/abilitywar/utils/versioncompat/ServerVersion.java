package daybreak.abilitywar.utils.versioncompat;

import daybreak.abilitywar.utils.Messager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;

import java.lang.reflect.Field;

/**
 * Server Version
 *
 * @author Daybreak 새벽
 */
public class ServerVersion {

	private ServerVersion() {
	}

	private static final Messager messager = new Messager();
	private static String versionString;
	private static int version;

	static {
		String[] versionArray = Bukkit.getServer().getClass().getName().replace('.', ',').split(",");
		if (versionArray.length >= 4) {
			versionString = versionArray[3];
		} else {
			versionString = "";
		}

		int version = -1;
		versionArray = versionString.split("_");
		if (versionArray.length >= 2) {
			try {
				version = Integer.parseInt(versionArray[1]);
			} catch (NumberFormatException ignored) {
				//Ignore: Should Never Happen
			}
		}
		ServerVersion.version = version;
	}

	public static int getVersion() {
		return version;
	}

	public static String getStringVersion() {
		return versionString;
	}

	/**
	 * 버전 호환 작업
	 */
	public static void compatVersion(Plugin plugin) {
		if (getVersion() >= 12) {
			if (getVersion() >= 13) {
				setAPIVersion(plugin, "1." + getVersion());
			}
		} else {
			messager.sendConsoleMessage(ChatColor.translateAlternateColorCodes('&', "&f플러그인이 지원하지 않는 버전을 이용하고 있습니다."));
			Bukkit.getPluginManager().disablePlugin(plugin);
		}
	}

	private static void setAPIVersion(Plugin plugin, String version) {
		try {
			PluginDescriptionFile desc = plugin.getDescription();
			Field apiVersion = PluginDescriptionFile.class.getDeclaredField("apiVersion");
			apiVersion.setAccessible(true);
			apiVersion.set(desc, version);
			apiVersion.setAccessible(false);
		} catch (IllegalAccessException | NoSuchFieldException ignored) {
			//Ignore: Should Never Happen
		}
	}

}
