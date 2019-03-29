package Marlang.AbilityWar.Utils.Data;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

/**
 * Mojang »çÀÌÆ® API
 * @author _Marlang ¸»¶û
 */
public class MojangAPI {
	
	private MojangAPI() {}
	
	public static String getNickname(String UUID) throws Exception {
		URL nicknames = new URL("https://api.mojang.com/user/profiles/" + UUID + "/names");
		BufferedReader br = new BufferedReader(new InputStreamReader(nicknames.openStream(), "UTF-8"));
		
		String result = "";

		String line;
		while((line = br.readLine()) != null) {
			result = result.concat(line);
		}
		
		JSONParser parser = new JSONParser();
		
		JSONArray array = (JSONArray) parser.parse(result);
		
		JSONObject nickname = (JSONObject) array.get(array.size() - 1);
		return (String) nickname.get("name");
	}
	
}
