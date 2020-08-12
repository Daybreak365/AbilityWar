package daybreak.abilitywar.utils.base.minecraft;

import com.google.gson.JsonArray;
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
		try (final BufferedReader reader = new BufferedReader(new InputStreamReader(new URL("https://api.mojang.com/user/profiles/" + uuid + "/names").openStream(), StandardCharsets.UTF_8))) {
			String line;
			while ((line = reader.readLine()) != null) {
				response.append(line);
			}
		}
		final JsonArray array = JsonParser.parseString(response.toString()).getAsJsonArray();
		return array.get(array.size() - 1).getAsJsonObject().get("name").getAsString();
	}

}
