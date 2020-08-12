package daybreak.abilitywar.addon.installer.info;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import daybreak.abilitywar.addon.AddonClassLoader;
import daybreak.abilitywar.addon.AddonLoader;
import daybreak.abilitywar.game.GameManager;
import daybreak.abilitywar.utils.base.Messager;
import daybreak.abilitywar.utils.base.io.FileUtil;
import daybreak.abilitywar.utils.base.minecraft.MojangAPI;
import daybreak.abilitywar.utils.base.minecraft.version.ServerVersion;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.jar.JarInputStream;
import java.util.zip.ZipEntry;
import org.bukkit.Bukkit;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;

public class CustomAddonInfo implements AddonInfo {

	private final ImmutableList<AddonInfo.AddonVersion> versions;
	private final ImmutableMap<String, AddonInfo.AddonVersion> versionMap;
	private final String name, displayName, developer, icon;
	private final List<Link> developerLinks = new ArrayList<>();
	private final List<String> description = new ArrayList<>();

	CustomAddonInfo(final JsonObject json) throws IllegalStateException, IOException {
		final JsonArray array = json.get("versions").getAsJsonArray();
		final ImmutableMap.Builder<String, AddonInfo.AddonVersion> mapBuilder = ImmutableMap.builderWithExpectedSize(array.size());
		final ImmutableList.Builder<AddonInfo.AddonVersion> listBuilder = ImmutableList.builderWithExpectedSize(array.size());

		for (int i = array.size() - 1; i >= 0; i--) {
			try {
				final AddonVersion addonVersion = new AddonVersion(array.get(i).getAsJsonObject());
				mapBuilder.put(addonVersion.tag, addonVersion);
				listBuilder.add(addonVersion);
			} catch (Exception ignored) {}
		}
		this.versions = listBuilder.build();
		this.versionMap = mapBuilder.build();
		if (this.versions.isEmpty()) throw new IllegalStateException("사용 가능한 버전이 존재하지 않습니다.");
		this.name = json.get("name").getAsString();
		this.displayName = json.get("displayName").getAsString();
		this.developer = json.get("developer").getAsString();
		this.icon = MojangAPI.getNickname(json.get("icon").getAsString().replaceAll("-", ""));
		if (json.has("links") && json.get("links").isJsonArray()) {
			for (JsonElement link : json.get("links").getAsJsonArray()) {
				developerLinks.add(new Link(link.getAsJsonObject()));
			}
		}
		if (json.has("description") && json.get("description").isJsonArray()) {
			for (JsonElement line : json.get("description").getAsJsonArray()) {
				description.add(line.getAsString());
			}
		}
	}

	@Override
	public ImmutableList<AddonInfo.AddonVersion> getVersions() {
		return versions;
	}

	@Override
	public AddonInfo.AddonVersion getVersion(String tag) {
		return versionMap.get(tag);
	}

	@Override
	public AddonInfo.AddonVersion getLatest() {
		return versions.get(0);
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public String getDisplayName() {
		return displayName;
	}

	@Override
	public String getDeveloper() {
		return developer;
	}

	@Override
	public List<Link> getDeveloperLinks() {
		return developerLinks;
	}

	@Override
	public String getIcon() {
		return icon;
	}

	@Override
	public List<String> getDescription() {
		return description;
	}

	public class AddonVersion implements AddonInfo.AddonVersion {

		private final String name, tag;
		private String version = null;
		private final boolean prerelease;
		private final URL downloadURL;
		private final int fileSize;
		private final String[] updates;

		private AddonVersion(final JsonObject object) throws ArrayIndexOutOfBoundsException, IOException {
			this.name = object.get("name").getAsString();
			this.tag = object.get("tag").getAsString();
			this.prerelease = object.get("prerelease").getAsBoolean();
			this.downloadURL = new URL(object.get("downloadURL").getAsString());
			{
				final HttpURLConnection connection = (HttpURLConnection) downloadURL.openConnection();
				try (final JarInputStream jarInputStream = new JarInputStream(connection.getInputStream())) {
					ZipEntry entry;
					while ((entry = jarInputStream.getNextEntry()) != null) {
						if (entry.getName().equals("addon.yml")) break;
					}
					if (entry != null) {
						try (final Scanner scanner = new Scanner(jarInputStream)) {
							while (scanner.hasNextLine()) {
								String line = scanner.nextLine();
								if (line.startsWith("version:")) {
									if (line.length() <= 8) break;
									line = line.substring(8).trim();
									if (!line.isEmpty()) {
										this.version = line;
									}
									break;
								}
							}
						}
					}
				}
				this.fileSize = connection.getContentLength();
				connection.disconnect();
			}
			final List<String> updates = new ArrayList<>();
			for (JsonElement element : object.get("updates").getAsJsonArray()) {
				updates.add(element.getAsString());
			}
			this.updates = updates.toArray(new String[0]);
		}

		@Override
		public String getName() {
			return name;
		}

		@Override
		public String getTag() {
			return tag;
		}

		@Override
		public String getVersion() {
			return Strings.nullToEmpty(version);
		}

		@Override
		public boolean isPrerelease() {
			return prerelease;
		}

		@Override
		public URL getFileURL() {
			return downloadURL;
		}

		@Override
		public int getFileSize() {
			return fileSize;
		}

		@Override
		public String[] getUpdates() {
			return updates;
		}

		@Override
		public void install() throws IOException {
			GameManager.stopGame();
			Messager.sendConsoleMessage(CustomAddonInfo.this.displayName + " " + tag + "(" + name + ") 설치 시작");
			for (Player receiver : Bukkit.getOnlinePlayers()) {
				receiver.sendMessage(Messager.defaultPrefix + CustomAddonInfo.this.displayName + " " + tag + "(" + name + ") 설치 시작");
			}
			final HttpURLConnection connection = (HttpURLConnection) downloadURL.openConnection();
			final BossBar bossBar = Bukkit.createBossBar(Messager.defaultPrefix + CustomAddonInfo.this.displayName + " " + tag + "(" + name + ") 설치", BarColor.WHITE, BarStyle.SEGMENTED_12);
			for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
				bossBar.addPlayer(onlinePlayer);
			}
			bossBar.setProgress(0);
			if (ServerVersion.getVersion() >= 10) bossBar.setVisible(true);
			try (final InputStream input = connection.getInputStream()) {
				final FileOutputStream output;
				if (AddonLoader.checkAddon(CustomAddonInfo.this.name)) {
					final ClassLoader classLoader = AddonLoader.getAddon(CustomAddonInfo.this.name).getClass().getClassLoader();
					if (classLoader instanceof AddonClassLoader) {
						output = new FileOutputStream(((AddonClassLoader) classLoader).getPluginFile(), false);
					} else {
						output = new FileOutputStream(FileUtil.getFile("Addon/" + CustomAddonInfo.this.name + ".jar"), false);
					}
				} else {
					output = new FileOutputStream(FileUtil.getFile("Addon/" + CustomAddonInfo.this.name + ".jar"), false);
				}
				final byte[] data = new byte[1024];
				int count;
				double sum = 0;
				while ((count = input.read(data)) >= 0) {
					output.write(data, 0, count);
					sum += count;
					bossBar.setProgress(Math.max(0.0, Math.min(1.0, sum / fileSize)));
				}
				output.close();
			} catch (Exception ex) {
				if (ServerVersion.getVersion() >= 10) bossBar.setVisible(false);
				bossBar.removeAll();
				throw ex;
			}
			connection.disconnect();
			Messager.sendConsoleMessage("서버를 다시 불러오는 중...");
			for (Player receiver : Bukkit.getOnlinePlayers()) {
				receiver.sendMessage(Messager.defaultPrefix + "서버를 다시 불러오는 중...");
			}
			bossBar.setColor(BarColor.RED);
			bossBar.setTitle("서버를 다시 불러오는 중...");
			Bukkit.reload();
			if (ServerVersion.getVersion() >= 10) bossBar.setVisible(false);
			bossBar.removeAll();
			Messager.sendConsoleMessage(CustomAddonInfo.this.displayName + " " + tag + "(" + name + ") 설치 완료");
			for (Player receiver : Bukkit.getOnlinePlayers()) {
				receiver.sendMessage(Messager.defaultPrefix + CustomAddonInfo.this.displayName + " " + tag + "(" + name + ") 설치 완료");
			}
		}

	}

}
