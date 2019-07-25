package DayBreak.AbilityWar.Utils.VersionCompat;

import java.io.IOException;
import java.lang.reflect.Field;
import java.net.URLClassLoader;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.SimpleCommandMap;
import org.bukkit.event.Event;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.RegisteredListener;

import DayBreak.AbilityWar.Utils.Messager;
import DayBreak.AbilityWar.Utils.AutoUpdate.AutoUpdate;

/**
 * Server Version
 * @author DayBreak 새벽
 */
public class ServerVersion {

	private ServerVersion() {}
	
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
				setAPIVersion(plugin, "1.13");
			}
		} else {
			Messager.sendMessage(ChatColor.translateAlternateColorCodes('&', "&f플러그인이 지원하지 않는 버전을 이용하고 있습니다."));
			unload(plugin);
		}
	}
	
	private static void setAPIVersion(Plugin plugin, String Version) {
		try {
			PluginDescriptionFile desc = plugin.getDescription();
			Field apiVersion = PluginDescriptionFile.class.getDeclaredField("apiVersion");
			apiVersion.setAccessible(true);
			apiVersion.set(desc, Version);
			apiVersion.setAccessible(false);
		} catch (Exception e) {
			//Ignore: Should Never Happen
		}
	}

	/**
	 * 플러그인을 Unload합니다.
	 */
	@SuppressWarnings("unchecked")
	private static void unload(Plugin plugin) {

		String name = plugin.getName();

		PluginManager pluginManager = Bukkit.getPluginManager();

		SimpleCommandMap commandMap = null;

		List<Plugin> plugins = null;

		Map<String, Plugin> names = null;
		Map<String, Command> commands = null;
		Map<Event, SortedSet<RegisteredListener>> listeners = null;

		boolean reloadlisteners = true;

		if (pluginManager != null) {
			
			pluginManager.disablePlugin(plugin);
			
			try {

				Field pluginsField = Bukkit.getPluginManager().getClass().getDeclaredField("plugins");
				pluginsField.setAccessible(true);
				plugins = (List<Plugin>) pluginsField.get(pluginManager);

				Field lookupNamesField = Bukkit.getPluginManager().getClass().getDeclaredField("lookupNames");
				lookupNamesField.setAccessible(true);
				names = (Map<String, Plugin>) lookupNamesField.get(pluginManager);

				try {
					Field listenersField = Bukkit.getPluginManager().getClass().getDeclaredField("listeners");
					listenersField.setAccessible(true);
					listeners = (Map<Event, SortedSet<RegisteredListener>>) listenersField.get(pluginManager);
				} catch (Exception e) {
					reloadlisteners = false;
				}

				Field commandMapField = Bukkit.getPluginManager().getClass().getDeclaredField("commandMap");
				commandMapField.setAccessible(true);
				commandMap = (SimpleCommandMap) commandMapField.get(pluginManager);

				Field knownCommandsField = SimpleCommandMap.class.getDeclaredField("knownCommands");
				knownCommandsField.setAccessible(true);
				commands = (Map<String, Command>) knownCommandsField.get(commandMap);

			} catch (NoSuchFieldException | IllegalAccessException e) {
				Messager.sendErrorMessage();
				return;
			}
		}

		pluginManager.disablePlugin(plugin);

		if (plugins != null && plugins.contains(plugin))
			plugins.remove(plugin);

		if (names != null && names.containsKey(name))
			names.remove(name);

		if (listeners != null && reloadlisteners) {
			for (SortedSet<RegisteredListener> set : listeners.values()) {
				for (Iterator<RegisteredListener> it = set.iterator(); it.hasNext();) {
					RegisteredListener value = it.next();
					if (value.getPlugin() == plugin) {
						it.remove();
					}
				}
			}
		}

		if (commandMap != null) {
			for (Iterator<Map.Entry<String, Command>> it = commands.entrySet().iterator(); it.hasNext();) {
				Map.Entry<String, Command> entry = it.next();
				if (entry.getValue() instanceof PluginCommand) {
					PluginCommand c = (PluginCommand) entry.getValue();
					if (c.getPlugin() == plugin) {
						c.unregister(commandMap);
						it.remove();
					}
				}
			}
		}
		
		ClassLoader cl = plugin.getClass().getClassLoader();

		if (cl instanceof URLClassLoader) {

			try {

				Field pluginField = cl.getClass().getDeclaredField("plugin");
				pluginField.setAccessible(true);
				pluginField.set(cl, null);

				Field pluginInitField = cl.getClass().getDeclaredField("pluginInit");
				pluginInitField.setAccessible(true);
				pluginInitField.set(cl, null);

			} catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException ex) {
				Logger.getLogger(AutoUpdate.class.getName()).log(Level.SEVERE, null, ex);
			}

			try {
				((URLClassLoader) cl).close();
			} catch (IOException ex) {
				Messager.sendErrorMessage();
			}

		}
	}
	
}
