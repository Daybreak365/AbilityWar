package daybreak.abilitywar.config;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.configuration.InvalidConfigurationException;

import daybreak.abilitywar.ability.AbilityBase;
import daybreak.abilitywar.ability.AbilityManifest;
import daybreak.abilitywar.utils.database.FileManager;

/**
 * 능력 세부 설정
 * 
 * @author DayBreak 새벽
 */
public class AbilitySettings {

	private static final Logger logger = Logger.getLogger(AbilityWarSettings.class.getName());
	private static final ArrayList<SettingObject<?>> settings = new ArrayList<SettingObject<?>>();

	private static void registerSetting(SettingObject<?> object) {
		if (!settings.contains(object)) {
			settings.add(object);
		}
	}
	
	private static File file = null;
	private static long lastModified;
	private static CommentedConfiguration config = null;

	public static boolean isLoaded() {
		return file != null && config != null;
	}

	public static void load() throws IOException, InvalidConfigurationException {
		if (!isLoaded()) {
			file = FileManager.createFile("abilitysettings.yml");
			lastModified = file.lastModified();
			config = new CommentedConfiguration(file);
			update();
		}
	}

	private static final HashMap<SettingObject<?>, Object> cache = new HashMap<>();

	public static void Update() {
		try {
			update();
		} catch (IOException | InvalidConfigurationException e) {
			logger.log(Level.SEVERE, "콘피그를 업데이트하는 도중 오류가 발생하였습니다.");
		}
	}

	private static void update() throws IOException, InvalidConfigurationException {
		config.load();
		cache.clear();
		lastModified = file.lastModified();
		for (SettingObject<?> setting : settings) {
			Object value = config.get(setting.getPath());
			if (value != null) {
				cache.put(setting, value);
			} else {
				config.set(setting.getPath(), setting.getDefault());
				cache.put(setting, setting.getDefault());
			}
		}
		config.save();
	}

	public abstract static class SettingObject<T> {

		private final String path;
		private final T defaultValue;
		private final String[] comments;

		public SettingObject(Class<? extends AbilityBase> abilityClass, String Path, T defaultValue, String... comments) {
			AbilityManifest manifest = abilityClass.getAnnotation(AbilityManifest.class);
			if (manifest != null) {
				this.path = "능력." + manifest.Name() + "." + Path;
			} else {
				throw new IllegalArgumentException(abilityClass.getName() + " 클래스에 AbilityManifest 어노테이션이 존재하지 않습니다.");
			}

			this.defaultValue = defaultValue;
			this.comments = comments;

			registerSetting(this);
		}

		public String getPath() {
			return path;
		}

		public T getDefault() {
			return defaultValue;
		}

		public String[] getComments() {
			return comments;
		}

		public abstract boolean Condition(T value);

		@SuppressWarnings("unchecked")
		public T getValue() {
			if (!isLoaded()) {
				logger.log(Level.SEVERE, "콘피그가 불러와지지 않은 상태에서 접근을 시도하였습니다.");
				throw new IllegalStateException("콘피그가 아직 불러와지지 않았습니다.");
			}
			if (lastModified != file.lastModified()) {
				try {
					update();
				} catch (IOException | InvalidConfigurationException e) {
					logger.log(Level.SEVERE, "콘피그를 다시 불러오는 도중 오류가 발생하였습니다.");
				}
			}
			
			Object object = cache.get(this);

			if (object != null && object.getClass().isAssignableFrom(getDefault().getClass())) {
				T castedObject = (T) object;
				if (Condition(castedObject)) {
					return castedObject;
				} else {
					return getDefault();
				}
			} else {
				return getDefault();
			}
		}

	}

}
