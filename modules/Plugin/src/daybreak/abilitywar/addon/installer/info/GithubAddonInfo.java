package daybreak.abilitywar.addon.installer.info;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import daybreak.abilitywar.addon.AddonClassLoader;
import daybreak.abilitywar.addon.AddonLoader;
import daybreak.abilitywar.game.GameManager;
import daybreak.abilitywar.utils.base.Messager;
import daybreak.abilitywar.utils.base.io.FileUtil;
import daybreak.abilitywar.utils.base.minecraft.MojangAPI;
import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.jar.JarInputStream;
import java.util.zip.ZipEntry;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class GithubAddonInfo implements AddonInfo {

	private final ImmutableList<AddonInfo.AddonVersion> versions;
	private final ImmutableMap<String, AddonInfo.AddonVersion> versionMap;
	private final String name, displayName, developer, icon;
	private final List<Link> developerLinks = new ArrayList<>();
	private final List<String> description = new ArrayList<>();

	GithubAddonInfo(final JsonObject json) throws IllegalStateException, IOException {
		final BufferedReader reader = new BufferedReader(new InputStreamReader(new URL("https://api.github.com/repos/" + json.get("author").getAsString() + "/" + json.get("repository").getAsString() + "/releases").openStream(), StandardCharsets.UTF_8));
		final StringBuilder result = new StringBuilder();
		{
			String line;
			while ((line = reader.readLine()) != null) {
				result.append(line);
			}
		}
		final JsonArray array = JsonParser.parseString(result.toString()).getAsJsonArray();
		final ImmutableMap.Builder<String, AddonInfo.AddonVersion> mapBuilder = ImmutableMap.builderWithExpectedSize(array.size());
		final ImmutableList.Builder<AddonInfo.AddonVersion> listBuilder = ImmutableList.builderWithExpectedSize(array.size());
		for (JsonElement element : array) {
			try {
				final AddonVersion addonVersion = new AddonVersion(element.getAsJsonObject());
				mapBuilder.put(addonVersion.tag, addonVersion);
				listBuilder.add(addonVersion);
			} catch (Exception ignored) {}
		}
		this.versions = listBuilder.build();
		this.versionMap = mapBuilder.build();
		if (this.versionMap.isEmpty()) throw new IllegalStateException("사용 가능한 버전이 존재하지 않습니다.");
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
			this.tag = object.get("tag_name").getAsString();
			this.prerelease = object.get("prerelease").getAsBoolean();
			final JsonObject asset = object.get("assets").getAsJsonArray().get(0).getAsJsonObject();
			this.downloadURL = new URL(asset.get("browser_download_url").getAsString());
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
				connection.disconnect();
			}
			this.fileSize = asset.get("size").getAsInt();
			this.updates = object.get("body").getAsString().split("\\n");
			for (int i = 0; i < updates.length; i++) {
				updates[i] = updates[i].replaceAll("\\r", "");
			}
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
			Messager.sendConsoleMessage(GithubAddonInfo.this.displayName + " " + tag + "(" + name + ") 설치 시작");
			for (Player receiver : Bukkit.getOnlinePlayers()) {
				receiver.sendMessage(Messager.defaultPrefix + GithubAddonInfo.this.displayName + " " + tag + "(" + name + ") 설치 시작");
			}
			final HttpURLConnection connection = (HttpURLConnection) downloadURL.openConnection();
			try (final InputStream input = connection.getInputStream()) {
				final FileOutputStream output;
				if (AddonLoader.checkAddon(GithubAddonInfo.this.name)) {
					final ClassLoader classLoader = AddonLoader.getAddon(GithubAddonInfo.this.name).getClass().getClassLoader();
					if (classLoader instanceof AddonClassLoader) {
						output = new FileOutputStream(((AddonClassLoader) classLoader).getPluginFile(), false);
					} else {
						output = new FileOutputStream(FileUtil.getFile("Addon/" + GithubAddonInfo.this.name + ".jar"), false);
					}
				} else {
					output = new FileOutputStream(FileUtil.getFile("Addon/" + GithubAddonInfo.this.name + ".jar"), false);
				}
				final byte[] data = new byte[1024];
				int count;
				while ((count = input.read(data)) >= 0) {
					output.write(data, 0, count);
				}
				output.close();
			}
			connection.disconnect();
			Bukkit.reload();
			Messager.sendConsoleMessage(GithubAddonInfo.this.displayName + " " + tag + "(" + name + ") 설치 완료");
			for (Player receiver : Bukkit.getOnlinePlayers()) {
				receiver.sendMessage(Messager.defaultPrefix + GithubAddonInfo.this.displayName + " " + tag + "(" + name + ") 설치 완료");
			}
		}

	}

}
