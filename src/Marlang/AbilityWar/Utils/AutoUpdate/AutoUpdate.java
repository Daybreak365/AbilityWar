package Marlang.AbilityWar.Utils.AutoUpdate;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.plugin.Plugin;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import Marlang.AbilityWar.Utils.Messager;

/**
 * Github Auto Update
 * @author _Marlang 말랑
 */
public class AutoUpdate {
	
	private final String Author;
	private final String Repository;
	private final Plugin Plugin;
	private final Branch PluginBranch;
	private final Branch ServerBranch;
	
	public AutoUpdate(String Author, String Repository, Plugin Plugin, Branch PluginBranch) {
		this.Author = Author;
		this.Repository = Repository;
		this.Plugin = Plugin;
		this.PluginBranch = PluginBranch;
		this.ServerBranch = Branch.getBranchByVersion(ServerVersion.getVersion());
	}

	public boolean Check() {
		try {
			if(ServerBranch != null) {
				if(PluginBranch.equals(ServerBranch)) { //동일 버전일 경우
					UpdateObject Update = getLatestUpdate(PluginBranch.getName());
					if (!isPluginLatest(Update)) {
						Messager.sendMessage(Messager.formatTitle(ChatColor.DARK_GREEN, ChatColor.GREEN, "업데이트"));
						Messager.sendMessage(ChatColor.translateAlternateColorCodes('&', "&b" + Update.getTag() + " &f업데이트 &f(&7v" + Update.getVersion() + "&f)"));
						for(String s : Update.getPatchNote()) {
							Messager.sendMessage(s);
						}
						Messager.sendMessage(ChatColor.translateAlternateColorCodes('&', "&2--------------------------------------------------------------"));
						
						Bukkit.getPluginManager().disablePlugin(Plugin);
						
						Messager.sendMessage(ChatColor.translateAlternateColorCodes('&', "&f업데이트를 시작합니다."));
						
						Download(Update);
						
						Messager.sendMessage(ChatColor.translateAlternateColorCodes('&', "&f업데이트를 완료하였습니다. 서버를 종료합니다."));
						Bukkit.shutdown();
						
						return true;
					} else {
						Messager.sendMessage(ChatColor.translateAlternateColorCodes('&', "&f플러그인이 최신 버전입니다."));
					}
				} else { //버전이 다를 경우
					Messager.sendMessage(ChatColor.translateAlternateColorCodes('&', "&f플러그인 버전과 서버 버전이 다릅니다. &b" + PluginBranch.getVersion() + " &7<=> &b" + ServerBranch.getVersion()));

					UpdateObject Update = getLatestUpdate(ServerBranch.getName());
					
					Bukkit.getPluginManager().disablePlugin(Plugin);
					
					Messager.sendMessage(ChatColor.translateAlternateColorCodes('&', "&f플러그인 호환 및 업데이트 작업을 시작합니다."));
					
					Download(Update);
					
					Messager.sendMessage(ChatColor.translateAlternateColorCodes('&', "&f호환 및 업데이트 작업을 완료하였습니다. 서버를 종료합니다."));
					Bukkit.shutdown();
					
					return true;
				}
			} else {
				UpdateObject Update = getLatestUpdate(PluginBranch.getName());
				if (!isPluginLatest(Update)) {
					Messager.sendMessage(Messager.formatTitle(ChatColor.DARK_GREEN, ChatColor.GREEN, "업데이트"));
					Messager.sendMessage(ChatColor.translateAlternateColorCodes('&', "&b" + Update.getTag() + " &f업데이트 &f(&7v" + Update.getVersion() + "&f)"));
					for(String s : Update.getPatchNote()) {
						Messager.sendMessage(s);
					}
					Messager.sendMessage(ChatColor.translateAlternateColorCodes('&', "&2--------------------------------------------------------------"));
					
					Bukkit.getPluginManager().disablePlugin(Plugin);
					
					Messager.sendMessage(ChatColor.translateAlternateColorCodes('&', "&f업데이트를 시작합니다."));
					
					Download(Update);
					
					Messager.sendMessage(ChatColor.translateAlternateColorCodes('&', "&f업데이트를 완료하였습니다. 서버를 종료합니다."));
					Bukkit.shutdown();
					
					return true;
				} else {
					Messager.sendMessage(ChatColor.translateAlternateColorCodes('&', "&f플러그인이 지원하지 않는 버전을 이용하고 있습니다."));
					Bukkit.getPluginManager().disablePlugin(Plugin);
				}
			}
		} catch (Exception ex) {
			Messager.sendMessage(ChatColor.translateAlternateColorCodes('&', "&f플러그인 최신 업데이트를 확인할 수 없습니다."));
		}
		
		return false;
	}
	
	private boolean isPluginLatest(UpdateObject Update) throws Exception {
		return Plugin.getDescription().getVersion().equalsIgnoreCase(Update.getVersion());
	}
	
	private void Download(UpdateObject Update) throws IOException {
		HttpURLConnection connection = (HttpURLConnection) Update.getDownloadURL().openConnection();
		connection.setRequestMethod("GET");
		
		InputStream input = connection.getInputStream();
		FileOutputStream output = new FileOutputStream(getJarPath(), false);
		
		byte[] data = new byte[1024];
		
		final int fileSize = Update.getFileSize();
		long Downloaded = 0;
		
		ArrayList<Integer> Percent = new ArrayList<Integer>();
		
		Integer Count;
		while ((Count = input.read(data)) >= 0) {
			Downloaded += Count;
			
			output.write(data, 0, Count);
			
			final Integer Percentage = (int) ((Downloaded * 100) / fileSize);
			if(!Percent.contains(Percentage) && (Percentage % 10) == 0) {
				Percent.add(Percentage);
				Messager.sendMessage("업데이트 " + Percentage + "% 완료");
			}
		}
		
		output.flush();
		output.close();
	}
	
	private String getJarPath() {
		URL fileURL = Plugin.getClass().getProtectionDomain().getCodeSource().getLocation();

		String path = fileURL.getPath();
		String[] split = path.split("/");
		String Jar = split[split.length - 1];
		
		return "plugins/" + Jar;
	}
	
	private UpdateObject getLatestUpdate(String Branch) throws Exception {
		URL releases = new URL("https://api.github.com/repos/" + Author + "/" + Repository + "/releases");
		BufferedReader br = new BufferedReader(new InputStreamReader(releases.openStream(), "UTF-8"));
		
		String line;
		String result = "";
		
		while((line = br.readLine()) != null) {
			result = result.concat(line);
		}
		
		JSONParser parser = new JSONParser();
		JSONArray array = (JSONArray) parser.parse(result);
		
		for(Integer i = 0; i < array.size(); i++) {
			JSONObject object = (JSONObject) array.get(i);
			
			String BranchName = (String) object.get("target_commitish");
			if(BranchName.equalsIgnoreCase(Branch)) {
				String Version = (String) object.get("name");
				String Tag = (String) object.get("tag_name");

				JSONArray parse_assets = (JSONArray) object.get("assets");
				JSONObject latest = (JSONObject) parse_assets.get(0);
				String url = (String) latest.get("browser_download_url");
				URL downloadURL = new URL(url);
				
				String[] patchNote = ((String) object.get("body")).split("\\n");
				
				return new UpdateObject(Version, Tag, downloadURL, patchNote);
			}
		}
		
		throw new Exception();
	}
	
	public class UpdateObject {
		
		private String Version;
		private String Tag;
		private URL downloadURL;
		private String[] patchNote;
		
		public UpdateObject(String Version, String Tag, URL downloadURL, String... patchNote) {
			this.Version = Version;
			this.Tag = Tag;
			this.downloadURL = downloadURL;
			this.patchNote = patchNote;
		}
		
		public String getVersion() {
			return Version;
		}
		
		public String getTag() {
			return Tag;
		}
		
		public URL getDownloadURL() {
			return downloadURL;
		}
		
		public String[] getPatchNote() {
			return patchNote;
		}
		
		public int getFileSize() throws IOException {
			return downloadURL.openConnection().getContentLength();
		}
		
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
	
}
