package daybreak.abilitywar.utils.installer;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import daybreak.abilitywar.AbilityWar;
import daybreak.abilitywar.utils.base.Formatter;
import daybreak.abilitywar.utils.base.Messager;
import daybreak.abilitywar.utils.base.logging.Logger;
import daybreak.abilitywar.utils.base.minecraft.version.ServerVersion;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.logging.Level;
import org.bukkit.Bukkit;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.command.Command;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.SimpleCommandMap;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.plugin.InvalidDescriptionException;
import org.bukkit.plugin.InvalidPluginException;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.RegisteredListener;

/**
 * 깃헙 자동 업데이트
 *
 * @author Daybreak 새벽
 */
public class Installer {

	private static final Logger logger = Logger.getLogger(Installer.class.getName());

	private final List<VersionObject> versions;
	private final HashMap<String, VersionObject> versionCache = new HashMap<>();

	public VersionObject getVersion(String name) {
		return versionCache.get(name);
	}

	public List<VersionObject> getVersions() {
		return versions;
	}

	private final Plugin plugin;

	public Installer(String author, String repository, Plugin plugin) throws IOException, InterruptedException, ExecutionException {
		this.plugin = plugin;

		String result = "";
		try (BufferedReader reader = new BufferedReader(new InputStreamReader(new URL("https://api.github.com/repos/" + author + "/" + repository + "/releases").openStream(), StandardCharsets.UTF_8))) {
			String line;
			while ((line = reader.readLine()) != null) {
				result = result.concat(line);
			}
		}

		final Map<ExecutorService, Future<VersionObject>> tasks = new LinkedHashMap<>();
		for (JsonElement element : JsonParser.parseString(result).getAsJsonArray()) {
			final ExecutorService service = Executors.newSingleThreadExecutor();
			tasks.put(service, service.submit(new Callable<VersionObject>() {
				@Override
				public VersionObject call() throws Exception {
					return new VersionObject(element.getAsJsonObject());
				}
			}));
		}
		final List<VersionObject> versions = new ArrayList<>(tasks.size());
		for (Map.Entry<ExecutorService, Future<VersionObject>> entry : tasks.entrySet()) {
			VersionObject update = entry.getValue().get();
			entry.getKey().shutdown();
			versions.add(update);
			versionCache.put(update.getVersion(), update);
		}
		this.versions = Collections.unmodifiableList(versions);
	}

	private enum DownloadResult {SUCCESS, FAILURE}

	public final class VersionObject {

		private final String version;
		private final boolean prerelease;
		private final String tag;
		private final URL downloadURL;
		private final int fileSize;
		private final int downloadCount;
		private final String[] updates;

		private VersionObject(JsonObject json) throws IOException {
			this.version = json.get("name").getAsString();
			this.prerelease = json.get("prerelease").getAsBoolean();
			this.tag = json.get("tag_name").getAsString();
			final JsonObject asset = json.get("assets").getAsJsonArray().get(0).getAsJsonObject();
			this.downloadURL = new URL(asset.get("browser_download_url").getAsString());
			{
				final HttpURLConnection connection = (HttpURLConnection) downloadURL.openConnection();
				this.fileSize = connection.getContentLength();
				connection.disconnect();
			}
			this.downloadCount = asset.get("download_count").getAsInt();
			this.updates = json.get("body").getAsString().split("\\n");
		}

		public String getVersion() {
			return version;
		}

		public boolean isPrerelease() {
			return prerelease;
		}

		public String getTag() {
			return tag;
		}

		public int getFileSize() {
			return fileSize;
		}

		public int getDownloadCount() {
			return downloadCount;
		}

		public String[] getUpdates() {
			return updates;
		}

		public void install() {
			try {
				Messager.sendConsoleMessage(Formatter.formatVersionInfo(this));
				unload(plugin);
				Bukkit.broadcastMessage(Messager.defaultPrefix + "AbilityWar " + tag + "(" + version + ") 설치를 시작합니다.");

				final HttpURLConnection connection = (HttpURLConnection) downloadURL.openConnection();
				try (final InputStream input = connection.getInputStream()) {
					final String[] split = AbilityWar.class.getProtectionDomain().getCodeSource().getLocation().getPath().split("/");
					final BossBar bossBar = Bukkit.createBossBar(Messager.defaultPrefix + "AbilityWar " + tag + "(" + version + ") 설치", BarColor.WHITE, BarStyle.SEGMENTED_12);
					for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
						bossBar.addPlayer(onlinePlayer);
					}
					bossBar.setProgress(0);
					if (ServerVersion.getVersion() >= 10) bossBar.setVisible(true);
					try (final FileOutputStream output = new FileOutputStream("plugins/" + split[split.length - 1], false)) {
						byte[] data = new byte[1024];
						int count;
						double sum = 0;
						while ((count = input.read(data)) >= 0) {
							output.write(data, 0, count);
							sum += count;
							bossBar.setProgress(Math.max(0.0, Math.min(1.0, sum / fileSize)));
						}
					} catch (Exception ex) {
						if (ServerVersion.getVersion() >= 10) bossBar.setVisible(false);
						bossBar.removeAll();
						throw ex;
					}
					if (ServerVersion.getVersion() >= 10) bossBar.setVisible(false);
					bossBar.removeAll();
				}
				connection.disconnect();
				Bukkit.broadcastMessage(Messager.defaultPrefix + "AbilityWar " + tag + "(" + version + ") 설치를 완료했습니다.");
				load(plugin);
			} catch (Exception ex) {
				logger.log(Level.SEVERE, "설치 도중 오류가 발생하였습니다.");
			}
		}
	}

	private void load(Plugin plugin) {
		load(plugin.getName());
	}

	private void load(String name) {

		Plugin target = null;

		File pluginDir = new File("plugins");

		if (!pluginDir.isDirectory()) {
			return;
		}

		File pluginFile = new File(pluginDir, name + ".jar");

		if (!pluginFile.isFile()) {
			for (File f : pluginDir.listFiles()) {
				if (f.getName().endsWith(".jar")) {
					try {
						PluginDescriptionFile desc = AbilityWar.getPlugin().getPluginLoader().getPluginDescription(f);
						if (desc.getName().equalsIgnoreCase(name)) {
							pluginFile = f;
							break;
						}
					} catch (InvalidDescriptionException e) {
					}
				}
			}
		}

		try {
			target = Bukkit.getPluginManager().loadPlugin(pluginFile);
		} catch (InvalidDescriptionException | InvalidPluginException e) {
		}

		target.onLoad();
		Bukkit.getPluginManager().enablePlugin(target);
	}

	@SuppressWarnings("unchecked")
	private void unload(Plugin plugin) {

		String name = plugin.getName();

		PluginManager pluginManager = Bukkit.getPluginManager();

		SimpleCommandMap commandMap = null;

		List<Plugin> plugins = null;

		Map<String, Plugin> names = null;
		Map<String, Command> commands = null;
		Map<Event, SortedSet<RegisteredListener>> listeners = null;

		boolean reloadlisteners = true;

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
			return;
		}

		pluginManager.disablePlugin(plugin);

		if (plugins != null)
			plugins.remove(plugin);

		if (names != null)
			names.remove(name);

		if (listeners != null && reloadlisteners) {
			for (SortedSet<RegisteredListener> set : listeners.values()) {
				for (Iterator<RegisteredListener> it = set.iterator(); it.hasNext(); ) {
					RegisteredListener value = it.next();
					if (value.getPlugin() == plugin) {
						it.remove();
					}
				}
			}
		}

		if (commandMap != null) {
			for (Iterator<Map.Entry<String, Command>> it = commands.entrySet().iterator(); it.hasNext(); ) {
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
				logger.log(Level.SEVERE, null, ex);
			}

			try {
				((URLClassLoader) cl).close();
			} catch (IOException ex) {
			}

		}
	}

}
