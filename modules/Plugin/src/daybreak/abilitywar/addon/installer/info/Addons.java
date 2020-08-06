package daybreak.abilitywar.addon.installer.info;

import com.google.common.base.Enums;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class Addons {

	private static Addons INSTANCE = null;

	public static void load() {
		if (INSTANCE == null) {
			INSTANCE = new Addons();
		}
	}

	public static Addons getInstance() throws IllegalStateException {
		if (INSTANCE == null) throw new IllegalStateException("추천 애드온 목록이 아직 불러와지지 않았습니다.");
		return INSTANCE;
	}

	private final Map<String, AddonInfo> addonInfos = new HashMap<>();

	public Collection<AddonInfo> getAddonInfos() {
		return Collections.unmodifiableCollection(addonInfos.values());
	}

	public AddonInfo getAddonInfo(final String name) {
		return addonInfos.get(name);
	}

	private Addons() {
		final Map<ExecutorService, Future<AddonInfo>> services = new HashMap<>();
		try {
			final BufferedReader reader = new BufferedReader(new InputStreamReader(new URL("https://raw.githubusercontent.com/DayBreak365/AbilityWar/master/addons.txt").openStream(), StandardCharsets.UTF_8));
			{
				String line;
				while ((line = reader.readLine()) != null) {
					try {
						if (line.isEmpty()) continue;
						final JsonObject json = JsonParser.parseReader(new BufferedReader(new InputStreamReader(new URL(line).openStream(), StandardCharsets.UTF_8))).getAsJsonObject();
						final JsonElement typeElement = json.get("format");
						if (!typeElement.isJsonPrimitive()) continue;
						final InfoFormat type = Enums.getIfPresent(InfoFormat.class, typeElement.getAsString().toUpperCase()).orNull();
						if (type == null) continue;
						final ExecutorService service = Executors.newSingleThreadExecutor();
						services.put(service, service.submit(new Callable<AddonInfo>() {
							@Override
							public AddonInfo call() throws Exception {
								return type.createInfo(json);
							}
						}));
					} catch (Exception ignored) {}
				}

				for (Entry<ExecutorService, Future<AddonInfo>> entry : services.entrySet()) {
					try {
						final AddonInfo addonInfo = entry.getValue().get();
						addonInfos.put(addonInfo.getDisplayName(), addonInfo);
						entry.getKey().shutdown();
					} catch (Exception ignored) {}
				}
			}
		} catch (Exception ignored) {}
	}

}
