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

public class Addons {

	private static final Map<String, AddonInfo> addonInfos = new HashMap<>();

	public static Collection<AddonInfo> getAddonInfos() {
		return Collections.unmodifiableCollection(addonInfos.values());
	}

	public static AddonInfo getAddonInfo(final String name) {
		return addonInfos.get(name);
	}

	static {
		try {
			final BufferedReader reader = new BufferedReader(new InputStreamReader(new URL("https://raw.githubusercontent.com/DayBreak365/AbilityWar/master/addons.txt").openStream(), StandardCharsets.UTF_8));
			{
				String line;
				while ((line = reader.readLine()) != null) {
					if (line.isEmpty()) continue;
					try {
						final JsonObject json = JsonParser.parseReader(new BufferedReader(new InputStreamReader(new URL(line).openStream(), StandardCharsets.UTF_8))).getAsJsonObject();
						final JsonElement typeElement = json.get("format");
						if (!typeElement.isJsonPrimitive()) continue;
						final InfoFormat type = Enums.getIfPresent(InfoFormat.class, typeElement.getAsString().toUpperCase()).orNull();
						if (type == null) continue;
						final AddonInfo info = type.createInfo(json);
						addonInfos.put(info.getDisplayName(), info);
					} catch (Exception ignored) {}
				}
			}
		} catch (Exception ignored) {}
	}

}
