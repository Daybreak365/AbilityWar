package Marlang.AbilityWar.Utils.AutoUpdate;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
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

	private final String Author;
	private final String Repository;
	private final Branch PluginBranch;
	private final Branch ServerBranch;
	
	public AutoUpdate(String Author, String Repository, Branch PluginBranch, Branch ServerBranch) {
		this.Author = Author;
		this.Repository = Repository;
		this.PluginBranch = PluginBranch;
		this.ServerBranch = ServerBranch;
	}
	
	public static enum Branch {
		
		Master("master", "1.12"),
		Alpha("1.13", "1.13");
		
		String Name;
		String Version;
		
		private Branch(String Name, String Version) {
			this.Name = Name;
			this.Version = Version;
		}

		public String getName() {
			return Name;
		}
		
		public String getVersion() {
			return Version;
		}
		
		public static Branch getBranchByVersion(Integer Version) {
			switch(Version) {
				case 12:
					return Branch.Master;
				case 13:
					return Branch.Alpha;
				default:
					return null;
			}
		}
		
	}
	
	public boolean Check() {
		try {
			if(ServerBranch != null) {
				if(PluginBranch.equals(ServerBranch)) { //동일 버전일 경우
					if (!IsLatest(PluginBranch)) {
						String LatestTag = getLatestTag(PluginBranch);
						String LatestVersion = getLatestVersion(PluginBranch);
						Messager.sendMessage(Messager.formatTitle(ChatColor.DARK_GREEN, ChatColor.GREEN, "업데이트"));
						Messager.sendMessage(ChatColor.translateAlternateColorCodes('&', "&b" + LatestTag + " &f업데이트 &f(&7v" + LatestVersion + "&f)"));
						String[] split = getLatestPatch(PluginBranch).split("\\n");
						for(String s : split) {
							Messager.sendMessage(s);
						}
						Messager.sendMessage(ChatColor.translateAlternateColorCodes('&', "&2--------------------------------------------------------------"));

						URL fileURL = AbilityWar.getPlugin().getClass().getProtectionDomain().getCodeSource().getLocation();
						
						Bukkit.getPluginManager().disablePlugin(AbilityWar.getPlugin());
						
						URL url = getLatestRelease(PluginBranch);
						HttpURLConnection connection = (HttpURLConnection) url.openConnection();
						connection.setRequestMethod("GET");
						
						Messager.sendMessage(ChatColor.translateAlternateColorCodes('&', "&f업데이트를 시작합니다."));
						
						
						FileOutputStream out = new FileOutputStream(getPluginPath(fileURL), false);
						
						Download(connection, out, 1024);
						
						createPatchNote(PluginBranch);
						
						Messager.sendMessage(ChatColor.translateAlternateColorCodes('&', "&f업데이트를 완료하였습니다. 서버를 종료합니다."));
						Bukkit.shutdown();
						out.close();
						
						return true;
					} else {
						Messager.sendMessage(ChatColor.translateAlternateColorCodes('&', "&f플러그인이 최신 버전입니다."));
					}
				} else { //버전이 다를 경우
					Messager.sendMessage(ChatColor.translateAlternateColorCodes('&', "&f플러그인 버전과 서버 버전이 다릅니다. &b" + PluginBranch.getVersion() + " &7<=> &b" + ServerBranch.getVersion()));

					URL fileURL = AbilityWar.getPlugin().getClass().getProtectionDomain().getCodeSource().getLocation();
					
					Bukkit.getPluginManager().disablePlugin(AbilityWar.getPlugin());
					
					URL url = getLatestRelease(ServerBranch);
					HttpURLConnection connection = (HttpURLConnection) url.openConnection();
					connection.setRequestMethod("GET");
					
					Messager.sendMessage(ChatColor.translateAlternateColorCodes('&', "&f플러그인 호환 및 업데이트 작업을 시작합니다."));
					
					
					FileOutputStream out = new FileOutputStream(getPluginPath(fileURL), false);
					
					Download(connection, out, 1024);
					
					createPatchNote(ServerBranch);
					
					Messager.sendMessage(ChatColor.translateAlternateColorCodes('&', "&f호환 및 업데이트 작업을 완료하였습니다. 서버를 종료합니다."));
					Bukkit.shutdown();
					out.close();
					
					return true;
				}
			} else {
				if (!IsLatest(PluginBranch)) {
					String LatestTag = getLatestTag(PluginBranch);
					String LatestVersion = getLatestVersion(PluginBranch);
					Messager.sendMessage(Messager.formatTitle(ChatColor.DARK_GREEN, ChatColor.GREEN, "업데이트"));
					Messager.sendMessage(ChatColor.translateAlternateColorCodes('&', "&b" + LatestTag + " &f업데이트 &f(&7v" + LatestVersion + "&f)"));
					String[] split = getLatestPatch(PluginBranch).split("\\n");
					for(String s : split) {
						Messager.sendMessage(s);
					}
					Messager.sendMessage(ChatColor.translateAlternateColorCodes('&', "&2--------------------------------------------------------------"));

					URL fileURL = AbilityWar.getPlugin().getClass().getProtectionDomain().getCodeSource().getLocation();
					
					Bukkit.getPluginManager().disablePlugin(AbilityWar.getPlugin());
					
					URL url = getLatestRelease(PluginBranch);
					HttpURLConnection connection = (HttpURLConnection) url.openConnection();
					connection.setRequestMethod("GET");
					
					Messager.sendMessage(ChatColor.translateAlternateColorCodes('&', "&f업데이트를 시작합니다."));
					
					
					FileOutputStream out = new FileOutputStream(getPluginPath(fileURL), false);
					
					Download(connection, out, 1024);
					
					createPatchNote(PluginBranch);
					
					Messager.sendMessage(ChatColor.translateAlternateColorCodes('&', "&f업데이트를 완료하였습니다. 서버를 종료합니다."));
					Bukkit.shutdown();
					out.close();
					
					return true;
				} else {
					Messager.sendMessage(ChatColor.translateAlternateColorCodes('&', "&f플러그인이 지원하지 않는 버전을 사용하고 있습니다."));
					Bukkit.getPluginManager().disablePlugin(AbilityWar.getPlugin());
					
					return true;
				}
			}
		} catch (Exception ex) {
			Messager.sendMessage(ChatColor.translateAlternateColorCodes('&', "&f플러그인 최신 업데이트를 확인할 수 없습니다."));
		}
		
		return false;
	}

	private String getPluginPath(URL fileURL) {
		String path = fileURL.getPath();
		String[] split = path.split("/");
		String Jar = split[split.length - 1];
		
		return "plugins/" + Jar;
	}
	
	private URL getLatestRelease(Branch branch) throws Exception {
		URL url = new URL("https://api.github.com/repos/" + Author + "/" + Repository + "/releases");
		BufferedReader br = new BufferedReader(new InputStreamReader(url.openStream(), "UTF-8"));
		
		String line;
		String result = "";
		
		while((line = br.readLine()) != null) {
			result = result.concat(line);
		}
		
		
		JSONParser parser = new JSONParser();
		JSONArray array = (JSONArray) parser.parse(result);
		
		JSONObject object = null;
		
		for(Integer i = 0; i < array.size(); i++) {
			JSONObject o = (JSONObject) array.get(i);
			
			String BranchName = (String) o.get("target_commitish");
			if(BranchName.equalsIgnoreCase(branch.getName())) {
				object = o;
				break;
			}
		}
		
		if(object != null) {
			JSONArray parse_assets = (JSONArray) object.get("assets");
			if(parse_assets.size() >= 1) {
				JSONObject latest = (JSONObject) parse_assets.get(0);
				String downloadURL = (String) latest.get("browser_download_url");
				
				return new URL(downloadURL);
			} else {
				throw new Exception();
			}
		} else {
			throw new Exception();
		}
	}
	
	private void createPatchNote(Branch branch) throws Exception {
		File PatchNote = new File("AbilityWar 패치사항.yml");
		if(!PatchNote.exists()) PatchNote.createNewFile();
		
		BufferedWriter bw = new BufferedWriter(new FileWriter(PatchNote, false));
		bw.write(getLatestPatch(branch));
		bw.flush();
		bw.close();
	}
	
	private String getLatestPatch(Branch branch) throws Exception {
		URL url = new URL("https://api.github.com/repos/" + Author + "/" + Repository + "/releases");
		BufferedReader br = new BufferedReader(new InputStreamReader(url.openStream(), "UTF-8"));
		
		String line;
		String result = "";
		
		while((line = br.readLine()) != null) {
			result = result.concat(line);
		}
		
		JSONParser parser = new JSONParser();
		JSONArray array = (JSONArray) parser.parse(result);
		
		JSONObject object = null;
		
		for(Integer i = 0; i < array.size(); i++) {
			JSONObject o = (JSONObject) array.get(i);
			
			String BranchName = (String) o.get("target_commitish");
			if(BranchName.equalsIgnoreCase(branch.getName())) {
				object = o;
				break;
			}
		}
		
		if(object != null) {
			String Note = (String) object.get("body");
			return Note;
		} else {
			throw new Exception();
		}
	}
	
	private void Download(HttpURLConnection connection, OutputStream output, int bufferSize) throws IOException {
		InputStream input = connection.getInputStream();
		
		byte[] buf = new byte[bufferSize];
		int n = input.read(buf);
		while (n >= 0) {
			output.write(buf, 0, n);
			n = input.read(buf);
		}
		output.flush();
	}

	private boolean IsLatest(Branch branch) throws Exception {
		String version = getLatestVersion(branch);
		if (!version.isEmpty()) {
			return AbilityWar.getPlugin().getDescription().getVersion().equalsIgnoreCase(version);
		} else {
			throw new Exception();
		}
	}

	private String getLatestVersion(Branch branch) throws Exception {
		URL url = new URL("https://api.github.com/repos/" + Author + "/" + Repository + "/releases");
		BufferedReader br = new BufferedReader(new InputStreamReader(url.openStream(), "UTF-8"));
		
		String line;
		String result = "";
		
		while((line = br.readLine()) != null) {
			result = result.concat(line);
		}

		JSONParser parser = new JSONParser();
		JSONArray array = (JSONArray) parser.parse(result);
		
		JSONObject object = null;
		
		for(Integer i = 0; i < array.size(); i++) {
			JSONObject o = (JSONObject) array.get(i);
			
			String BranchName = (String) o.get("target_commitish");
			if(BranchName.equalsIgnoreCase(branch.getName())) {
				object = o;
				break;
			}
		}
		
		if(object != null) {
			String Version = (String) object.get("name");
			
			return Version;
		} else {
			throw new Exception();
		}
	}

	private String getLatestTag(Branch branch) throws Exception {
		URL url = new URL("https://api.github.com/repos/" + Author + "/" + Repository + "/releases");
		BufferedReader br = new BufferedReader(new InputStreamReader(url.openStream(), "UTF-8"));
		
		String line;
		String result = "";
		
		while((line = br.readLine()) != null) {
			result = result.concat(line);
		}

		JSONParser parser = new JSONParser();
		JSONArray array = (JSONArray) parser.parse(result);
		
		JSONObject object = null;
		
		for(Integer i = 0; i < array.size(); i++) {
			JSONObject o = (JSONObject) array.get(i);
			
			String BranchName = (String) o.get("target_commitish");
			if(BranchName.equalsIgnoreCase(branch.getName())) {
				object = o;
				break;
			}
		}
		
		if(object != null) {
			String Version = (String) object.get("tag_name");
			
			return Version;
		} else {
			throw new Exception();
		}
	}
	
}
