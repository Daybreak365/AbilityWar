package Marlang.AbilityWar.Addon;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;

import Marlang.AbilityWar.AbilityWar;
import Marlang.AbilityWar.Utils.Messager;
import Marlang.AbilityWar.Utils.Data.FileManager;

public class AddonLoader {
	
	private ArrayList<Addon> Addons = new ArrayList<Addon>();
	
	public List<DescriptionFile> getDescriptions() {
		List<DescriptionFile> desc = new ArrayList<DescriptionFile>();
		for(Addon addon : Addons) {
			desc.add(addon.getDescription());
		}
		
		return desc;
	}
	
	public void loadAddons() {
		for(File file : FileManager.getFolder("Addon").listFiles()) {
			try {
				Addons.add(loadAddon(file));
			} catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
				Messager.sendDebugMessage(file.getName() + " 파일이 올바른 애드온이 아닙니다.");
			} catch (IOException e) {
				Messager.sendDebugMessage(file.getName() + " 파일을 불러올 수 없습니다.");
			} catch (Exception e) {
				e.printStackTrace();
				Messager.sendDebugMessage(file.getName() + " 애드온을 불러오는 도중 예상치 못한 오류가 발생하였습니다.");
			}
		}
	}
	
	public void onEnable() {
		for(Addon addon : Addons) {
			addon.onEnable();
		}
	}
	
	public void onDisable() {
		for(Addon addon : Addons) {
			addon.onDisable();
		}
	}
	
	private Addon loadAddon(File file) throws Exception {
		JarFile jar = new JarFile(file);
		
		URL[] url = { file.toURI().toURL() };
		URLClassLoader loader = new URLClassLoader(url, AbilityWar.class.getClassLoader());
		
		DescriptionFile description = new DescriptionFile(jar);
		Class<?> mainClass = loader.loadClass(description.getAddonMain());
		
		if(mainClass.getSuperclass().equals(Addon.class)) {
			Addon addon = (Addon) mainClass.newInstance();
			//DescriptionFile Initialize
			Method setDescription = mainClass.getSuperclass().getDeclaredMethod("setDescription", DescriptionFile.class);
			setDescription.setAccessible(true);
			setDescription.invoke(addon, description);
			setDescription.setAccessible(false);
			//DescriptionFile Initialize
			
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
	
	public class DescriptionFile {

		private final String AddonName;
		private final String AddonMain;
		private final String AddonVersion;
		
		private DescriptionFile(JarFile jarFile) throws IOException {
			ZipEntry entry = jarFile.getEntry("addon.yml");
			if(entry != null) {
				InputStream input = jarFile.getInputStream(entry);
				BufferedReader br = new BufferedReader(new InputStreamReader(input));

				String AddonName = "";
				String AddonMain = "";
				String AddonVersion = "";
				
				String line = null;
				while((line = br.readLine()) != null) {
					if(line.contains("main:")) {
						AddonMain = line.replaceAll("main:", "").replaceAll(" ", "");
					} else if(line.contains("name:")) {
						AddonName = line.replaceAll("name:", "").replaceAll(" ", "");
					} else if(line.contains("version:")) {
						AddonVersion = line.replaceAll("version:", "").replaceAll(" ", "");
					}
				}
				
				if(!AddonName.isEmpty() && !AddonMain.isEmpty() && !AddonVersion.isEmpty()) {
					this.AddonName = AddonName;
					this.AddonMain = AddonMain;
					this.AddonVersion = AddonVersion;
				} else {
					throw new IOException();
				}
			} else {
				throw new IOException();
			}
		}
		
		public String getAddonName() {
			return AddonName;
		}
		
		public String getAddonMain() {
			return AddonMain;
		}
		
		public String getAddonVersion() {
			return AddonVersion;
		}
		
	}
	
}
