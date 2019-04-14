package DayBreak.AbilityWar.Addon;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;

import DayBreak.AbilityWar.AbilityWar;
import DayBreak.AbilityWar.Utils.Messager;
import DayBreak.AbilityWar.Utils.Data.FileManager;

/**
 * 애드온에 직접적으로 엑세스하여 처리하는 로더입니다.
 * @author DayBreak 새벽
 */
public class AddonLoader {
	
	private AddonLoader() {
		//외부에서 AddonLoader를 인스턴스화하지 못하도록 방지
	}
	
	private static final ArrayList<Addon> Addons = new ArrayList<Addon>();
	
	public static List<Addon> getAddons() {
		return Addons;
	}

	/**
	 * 불러와진 애드온들의 설명 목록을 반환합니다.
	 */
	public static List<DescriptionFile> getDescriptions() {
		List<DescriptionFile> desc = new ArrayList<DescriptionFile>();
		for(Addon addon : Addons) {
			desc.add(addon.getDescription());
		}
		
		return desc;
	}
	
	/**
	 * 애드온 폴더에 있는 모든 애드온을 불러옵니다.
	 */
	public static void loadAddons() {
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
	
	public static void onEnable() {
		try {
			for(Addon addon : Addons) {
				addon.onEnable();
			}
		} catch(Exception ex) {
			ex.printStackTrace();
		}
	}
	
	public static void onDisable() {
		try {
			for(Addon addon : Addons) {
				addon.onDisable();
			}
		} catch(Exception ex) {
			ex.printStackTrace();
		}
	}
	
	/**
	 * 애드온을 불러옵니다.
	 * @param file			애드온의 기반이 되는 파일
	 * @return				불러온 애드온
	 * @throws Exception	애드온을 불러오는 도중 오류가 발생하였을 경우
	 */
	private static Addon loadAddon(File file) throws Exception {
		JarFile jar = new JarFile(file);
		URL[] url = { file.toURI().toURL() };
		URLClassLoader loader = new URLClassLoader(url, AbilityWar.class.getClassLoader());
		
		DescriptionFile description = new DescriptionFile(jar);
		Class<?> mainClass = loader.loadClass(description.getMain());
		
		if(mainClass.getSuperclass() != null && mainClass.getSuperclass().equals(Addon.class)) {
			Addon addon = (Addon) mainClass.newInstance();
			//Initialize
			Field descriptionField = mainClass.getSuperclass().getDeclaredField("description");
			descriptionField.setAccessible(true);
			descriptionField.set(addon, description);
			descriptionField.setAccessible(false);

			Field classLoaderField = mainClass.getSuperclass().getDeclaredField("classLoader");
			classLoaderField.setAccessible(true);
			classLoaderField.set(addon, loader);
			classLoaderField.setAccessible(false);
			//Initialize
			
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
			
			throw new InstantiationException();
		}
	}
	
	public static class DescriptionFile {

		private final String Name;
		private final String Main;
		private final String Version;
		
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
					this.Name = AddonName;
					this.Main = AddonMain;
					this.Version = AddonVersion;
				} else {
					throw new IOException();
				}
			} else {
				throw new IOException();
			}
		}
		
		public String getName() {
			return Name;
		}
		
		public String getMain() {
			return Main;
		}
		
		public String getVersion() {
			return Version;
		}
		
	}
	
}
