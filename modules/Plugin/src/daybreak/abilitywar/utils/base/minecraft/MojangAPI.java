package daybreak.abilitywar.utils.base.minecraft;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;

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

}
