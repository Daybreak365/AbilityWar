package daybreak.abilitywar.config.ability;

import daybreak.abilitywar.ability.AbilityBase;
import daybreak.abilitywar.ability.AbilityManifest;
import daybreak.abilitywar.config.Cache;
import daybreak.abilitywar.config.CommentedConfiguration;
import daybreak.abilitywar.config.Configuration;
import daybreak.abilitywar.utils.base.logging.Logger;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.logging.Level;
import org.bukkit.configuration.InvalidConfigurationException;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * 능력 콘피그
 *
 * @author Daybreak 새벽
 */
public class AbilitySettings {

	private static final Logger logger = Logger.getLogger(Configuration.class.getName());

	private final File configFile;
	private final CommentedConfiguration config;
	private final Set<SettingObject<?>> settings = new HashSet<>();
	private final Map<SettingObject<?>, Cache> cache = new HashMap<>();
	private long lastModified;
	private boolean error = false;

	public AbilitySettings(File configFile) {
		this.configFile = configFile;
		if (!configFile.exists()) {
			if (configFile.getParentFile() != null && !configFile.getParentFile().exists()) {
				configFile.getParentFile().mkdirs();
			}
			try {
				configFile.createNewFile();
			} catch (IOException e) {
				this.error = true;
			}
		}
		this.lastModified = configFile.lastModified();
		CommentedConfiguration config;
		try {
			config = new CommentedConfiguration(configFile);
		} catch (IOException | InvalidConfigurationException e) {
			config = null;
			this.error = true;
		}
		this.config = config;
		try {
			_update();
		} catch (IOException | InvalidConfigurationException e) {
			this.error = true;
		}
	}

	private void registerSetting(SettingObject<?> object) {
		settings.add(object);
	}

	public boolean isError() {
		return error;
	}

	public void update() {
		try {
			_update();
		} catch (IOException | InvalidConfigurationException e) {
			logger.log(Level.SEVERE, "콘피그를 업데이트하는 도중 오류가 발생하였습니다.");
		}
	}

	private void _update() throws IOException, InvalidConfigurationException {
		config.load();

		for (Entry<SettingObject<?>, Cache> entry : cache.entrySet()) {
			Cache cache = entry.getValue();
			if (cache.isModifiedValue()) {
				config.set(entry.getKey().getPath(), cache.getValue());
			}
		}

		cache.clear();
		lastModified = configFile.lastModified();
		for (SettingObject<?> setting : settings) {
			Object value = config.get(setting.getPath());
			if (value != null) {
				cache.put(setting, new Cache(false, value));
			} else {
				config.set(setting.getPath(), setting.getDefaultValue());
				cache.put(setting, new Cache(false, setting.getDefaultValue()));
			}
		}
		config.save();
	}

	public abstract class SettingObject<T> {

		private final String key;
		private final String path;
		private final T defaultValue;
		private final String[] comments;

		public SettingObject(Class<? extends AbilityBase> abilityClass, String key, T defaultValue, String... comments) {
			AbilityManifest manifest = checkNotNull(abilityClass).getAnnotation(AbilityManifest.class);
			if (manifest != null) {
				this.path = "능력." + manifest.name() + "." + checkNotNull(key);
			} else {
				throw new IllegalArgumentException(abilityClass.getName() + " 클래스에 AbilityManifest 어노테이션이 존재하지 않습니다.");
			}

			this.key = key;
			this.defaultValue = checkNotNull(defaultValue);
			this.comments = comments;
			registerSetting(this);
		}

		public String getKey() {
			return key;
		}

		public String getPath() {
			return path;
		}

		public T getDefaultValue() {
			return defaultValue;
		}

		public String[] getComments() {
			return comments;
		}

		public abstract boolean condition(T value);

		@SuppressWarnings("unchecked")
		public T getValue() {
			if (isError()) {
				logger.log(Level.SEVERE, "콘피그가 불러와지는 도중 오류가 발생했습니다.");
				throw new IllegalStateException("콘피그가 불러와지는 도중 오류가 발생했습니다.");
			}
			if (lastModified != configFile.lastModified()) {
				try {
					_update();
				} catch (IOException | InvalidConfigurationException e) {
					logger.log(Level.SEVERE, "콘피그를 다시 불러오는 도중 오류가 발생하였습니다.");
				}
			}

			if (!cache.containsKey(this)) {
				Object value = config.get(path);
				if (value != null) {
					cache.put(this, new Cache(false, value));
				} else {
					config.set(path, defaultValue);
					cache.put(this, new Cache(false, defaultValue));
				}
			}
			Object value = cache.get(this).getValue();

			if (value != null && value.getClass().isAssignableFrom(defaultValue.getClass())) {
				T castedValue = (T) value;
				if (condition(castedValue)) {
					return castedValue;
				} else {
					return defaultValue;
				}
			} else {
				return defaultValue;
			}
		}

		public boolean setValue(T value) {
			if (condition(value)) {
				cache.put(this, new Cache(true, value));
				return true;
			}
			return false;
		}

		@Override
		public String toString() {
			return String.valueOf(getValue());
		}

	}

}
