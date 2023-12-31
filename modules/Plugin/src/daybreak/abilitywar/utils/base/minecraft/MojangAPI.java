package daybreak.abilitywar.utils.base.minecraft;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

/**
 * Mojang API Wrapper
 *
 * @author Daybreak 새벽
 */
public class MojangAPI {

	private MojangAPI() {
	}

	public static String getNickname(String uuid) throws IOException {
		final StringBuilder response = new StringBuilder();
		try (final BufferedReader reader = new BufferedReader(new InputStreamReader(new URL("https://sessionserver.mojang.com/session/minecraft/profile/" + uuid).openStream(), StandardCharsets.UTF_8))) {
			String line;
			while ((line = reader.readLine()) != null) {
				response.append(line);
			}
		}
		final JsonObject object = JsonParser.parseString(response.toString()).getAsJsonObject();
		return object.get("name").getAsString();
	}

	private static final Cache<String, SkinInfo> skins = CacheBuilder.newBuilder()
			.expireAfterAccess(Duration.ofMinutes(30))
			.build();

	public static SkinInfo getSkin(String name) throws IOException {
		final SkinInfo cachedSkin = skins.getIfPresent(name);
		if (cachedSkin != null) return cachedSkin;
		final URL url0 = new URL("https://api.mojang.com/users/profiles/minecraft/" + name);
		final InputStreamReader reader_0 = new InputStreamReader(url0.openStream());
		final String uuid = JsonParser.parseReader(reader_0).getAsJsonObject().get("id").getAsString();

		final URL url1 = new URL("https://sessionserver.mojang.com/session/minecraft/profile/" + uuid + "?unsigned=false");
		final InputStreamReader reader_1 = new InputStreamReader(url1.openStream());
		final JsonObject textureProperty = JsonParser.parseReader(reader_1).getAsJsonObject().get("properties").getAsJsonArray().get(0).getAsJsonObject();
		final String texture = textureProperty.get("value").getAsString();
		final String signature = textureProperty.get("signature").getAsString();
		final SkinInfo info = new SkinInfo(name, texture, signature);
		skins.put(name, info);
		return info;
	}

}
