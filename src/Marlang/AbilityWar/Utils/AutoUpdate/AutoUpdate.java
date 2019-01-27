package Marlang.AbilityWar.Utils.AutoUpdate;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import Marlang.AbilityWar.AbilityWar;
import Marlang.AbilityWar.Utils.Messager;

public class AutoUpdate {

	private static final String Author = "Marlang365";
	private static final String Repository = "test";
	
	public static boolean Check() {
		try {
			if (!IsLatest()) {
				String LatestVersion = getLatestVersion();
				Messager.sendMessage(ChatColor.translateAlternateColorCodes('&', "&f최신 버전이 발견되었습니다&7: &b" + LatestVersion));

				URL fileURL = AbilityWar.getPlugin().getClass().getProtectionDomain().getCodeSource().getLocation();
				
				Bukkit.getPluginManager().disablePlugin(AbilityWar.getPlugin());
				
				URL url = getLatestRelease();
				HttpURLConnection connection = (HttpURLConnection) url.openConnection();
				connection.setRequestMethod("GET");
				
				Messager.sendMessage(ChatColor.translateAlternateColorCodes('&', "&f업데이트를 시작합니다."));
				
				
				FileOutputStream out = new FileOutputStream(getPluginPath(fileURL), false);
				
				Download(connection, out, 1024);
				
				Messager.sendMessage(ChatColor.translateAlternateColorCodes('&', "&f업데이트를 완료하였습니다. 서버를 종료합니다."));
				Bukkit.shutdown();
				out.close();
				
				return true;
			} else {
				Messager.sendMessage(ChatColor.translateAlternateColorCodes('&', "&f플러그인이 최신 버전입니다."));
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		
		return false;
	}

	private static String getPluginPath(URL fileURL) {
		String path = fileURL.getPath();
		String[] split = path.split("/");
		String Jar = split[split.length - 1];
		
		return "plugins/" + Jar;
	}
	
	private static URL getLatestRelease() throws Exception {
		URL url = new URL("https://api.github.com/repos/" + Author + "/" + Repository + "/releases/latest");
		BufferedReader br = new BufferedReader(new InputStreamReader(url.openStream()));
		
		String line;
		String result = "";
		
		while((line = br.readLine()) != null) {
			result = result.concat(line);
		}
		
		JSONParser parser = new JSONParser();
		JSONObject object = (JSONObject) parser.parse(result);
		
		JSONArray parse_assets = (JSONArray) object.get("assets");
		if(parse_assets.size() >= 1) {
			JSONObject latest = (JSONObject) parse_assets.get(0);
			String downloadURL = (String) latest.get("browser_download_url");
			
			return new URL(downloadURL);
		} else {
			throw new Exception();
		}
	}
	
	private static void Download(HttpURLConnection connection, OutputStream output, int bufferSize) throws IOException {
		InputStream input = connection.getInputStream();
		
		byte[] buf = new byte[bufferSize];
		int n = input.read(buf);
		while (n >= 0) {
			output.write(buf, 0, n);
			n = input.read(buf);
		}
		output.flush();
	}

	private static boolean IsLatest() throws Exception {
		String version = getLatestVersion();
		if (!version.isEmpty()) {
			return AbilityWar.getPlugin().getDescription().getVersion().equalsIgnoreCase(version);
		} else {
			throw new Exception();
		}
	}
	
	private static String getLatestVersion() throws Exception {
		URL url = new URL("https://api.github.com/repos/" + Author + "/" + Repository + "/releases/latest");
		BufferedReader br = new BufferedReader(new InputStreamReader(url.openStream()));
		
		String line;
		String result = "";
		
		while((line = br.readLine()) != null) {
			result = result.concat(line);
		}
		
		JSONParser parser = new JSONParser();
		JSONObject object = (JSONObject) parser.parse(result);
		
		String Version = (String) object.get("tag_name");
		
		return Version;
	}
	
}
