package daybreak.abilitywar.utils.database;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;

/**
 * 모장 API Wrapper
 * @author Daybreak 새벽
 */
public class MojangAPI {

	private MojangAPI() {
	}

	private static final JsonParser parser = new JsonParser();

	public static String getNickname(String UUID) throws IOException {
		URL nicknames = new URL("https://api.mojang.com/user/profiles/" + UUID + "/names");
		BufferedReader br = new BufferedReader(new InputStreamReader(nicknames.openStream(), StandardCharsets.UTF_8));

		String result = "";

		String line;
		while ((line = br.readLine()) != null) {
			result = result.concat(line);
		}

		JsonArray array = parser.parse(result).getAsJsonArray();

		JsonObject nickname = array.get(array.size() - 1).getAsJsonObject();
		return nickname.get("name").getAsString();
	}

}
