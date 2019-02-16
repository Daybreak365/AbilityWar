package Marlang.AbilityWar.Addon;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;

import org.bukkit.plugin.Plugin;

import Marlang.AbilityWar.AbilityWar;
import Marlang.AbilityWar.Utils.Messager;
import Marlang.AbilityWar.Utils.Data.FileManager;

public class AddonLoader {
	
	Plugin plugin;
	
	public AddonLoader(Plugin plugin) {
		this.plugin = plugin;
	}
	
	private ArrayList<Addon> Addons = new ArrayList<Addon>();
	
	public void loadAddons() {
		for(File file : FileManager.getFolder("Addon").listFiles()) {
			try {
				Addons.add(loadAddon(file));
			} catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
				Messager.sendDebugMessage(file.getName() + " 파일이 올바른 애드온이 아닙니다.");
			} catch (IOException e) {
				Messager.sendDebugMessage(file.getName() + " 파일을 불러올 수 없습니다.");
			} catch (Exception e) {
				Messager.sendDebugMessage(file.getName() + " 애드온을 불러오는 도중 예상치 못한 오류가 발생하였습니다.");
			}
		}
		
		for(Addon addon : Addons) {
			addon.onEnable();
		}
	}
	
	private Addon loadAddon(File file) throws Exception {
		JarFile jar = new JarFile(file);
		
		URL[] url = { file.toURI().toURL() };
		URLClassLoader loader = new URLClassLoader(url, AbilityWar.class.getClassLoader());
		
		Class<?> mainClass = loader.loadClass(getMain(jar));
		
		if(mainClass.getSuperclass().equals(Addon.class)) {
			Addon addon = (Addon) mainClass.newInstance();
			
			Enumeration<JarEntry> entries = jar.entries();
			
			while(entries.hasMoreElements()) {
				JarEntry entry = entries.nextElement();
				if(!entry.isDirectory() && entry.getName().endsWith(".class")) {
					String className = entry.getName().replaceAll("/", ".").replace(".class", "");
					loader.loadClass(className);
				}
			}
			loader.close();
			
			return addon;
		} else {
			loader.close();
			
			throw new IOException();
		}
	}
	
	private String getMain(JarFile jarFile) throws IOException {
		ZipEntry entry = jarFile.getEntry("addon.yml");
		if(entry != null) {
			InputStream input = jarFile.getInputStream(entry);
			BufferedReader br = new BufferedReader(new InputStreamReader(input));
			
			String AddonMain = "";
			
			String line = null;
			while((line = br.readLine()) != null) {
				if(line.contains("main:")) {
					AddonMain = line.replaceAll("main:", "").replaceAll(" ", "");
				}
			}
			
			if(!AddonMain.isEmpty()) {
				return AddonMain;
			}
		}

		throw new IOException();
	}
	
}
