package daybreak.abilitywar.config.game;

import com.google.common.collect.Table;
import com.google.common.collect.Tables;
import com.google.common.collect.TreeBasedTable;
import daybreak.abilitywar.config.Cache;
import daybreak.abilitywar.config.FileConfiguration;
import daybreak.abilitywar.config.interfaces.Configurable;
import daybreak.abilitywar.game.AbstractGame;
import daybreak.abilitywar.game.GameManifest;
import daybreak.abilitywar.utils.base.logging.Logger;
import org.bukkit.configuration.InvalidConfigurationException;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * 능력 콘피그
 *
 * @author Daybreak 새벽
 */
public class GameSettings {

	private static final Logger logger = Logger.getLogger(GameSettings.class);
	private static final Map<String, GameSettings> gameSettings = new HashMap<>();
	private final Table<String, String, Setting<?>> settings = TreeBasedTable.create();

	public GameSettings(final File configFile) {
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
		FileConfiguration config;
		try {
			config = new FileConfiguration(configFile);
		} catch (IOException | InvalidConfigurationException e) {
			config = null;
			this.error = true;
		}
		this.config = config;
		gameSettings.put(configFile.getName(), this);
	}

	private final File configFile;
	private final FileConfiguration config;

	public static Collection<GameSettings> getGameSettings() {
		return gameSettings.values();
	}

	private final Map<Setting<?>, Cache> cache = new HashMap<>();
	private long lastModified;
	private boolean error = false;

	public static GameSettings getGameSetting(String fileName) {
		return gameSettings.get(fileName);
	}

	public File getConfigFile() {
		return configFile;
	}

	private void registerSetting(String name, Setting<?> object) {
		settings.put(name, object.key, object);
	}

	public Table<String, String, Setting<?>> getSettings() {
		return Tables.unmodifiableTable(settings);
	}

	public Map<String, Setting<?>> getSettings(String name) {
		return Collections.unmodifiableMap(settings.row(name));
	}

	public Setting<?> getSetting(String name, String key) {
		return settings.get(name, key);
	}

	public boolean isError() {
		return error;
	}

	public void update() {
		try {
			_update();
		} catch (IOException | InvalidConfigurationException e) {
			logger.error("콘피그를 업데이트하는 도중 오류가 발생하였습니다.");
		}
	}

	private void _update() throws IOException, InvalidConfigurationException {
		config.load();

		for (Entry<Setting<?>, Cache> entry : cache.entrySet()) {
			final Cache cache = entry.getValue();
			if (cache.isModifiedValue()) {
				config.set(entry.getKey().getPath(), cache.getValue());
			}
		}

		cache.clear();
		lastModified = configFile.lastModified();
		for (Setting<?> setting : settings.values()) {
			final Object value = config.get(setting.getPath());
			if (value != null) {
				cache.put(setting, new Cache(false, value));
			} else {
				config.set(setting.getPath(), setting.getDefaultValue());
				cache.put(setting, new Cache(false, setting.getDefaultValue()));
			}
			//config.addComment(setting.getPath(), setting.getComments());
			//v3.3.4: CommentedConfiguration 관련 오류 다수 발생에 따라 주석 처리
		}
		config.save();
	}

	public abstract class Setting<T> implements Configurable<T> {

		private final String key, path;
		private final T defaultValue;
		private final String[] comments;

		public Setting(final Class<? extends AbstractGame> gameClass, final String key, final T defaultValue, final String... comments) {
			final GameManifest manifest = checkNotNull(gameClass).getAnnotation(GameManifest.class);
			if (manifest != null) {
				this.path = manifest.name() + "." + checkNotNull(key);
			} else {
				throw new IllegalArgumentException(gameClass.getName() + " 클래스에 GameManifest 어노테이션이 존재하지 않습니다.");
			}

			this.key = key;
			this.defaultValue = checkNotNull(defaultValue);
			this.comments = comments;
			registerSetting(manifest.name(), this);
		}

		@Override
		public String getKey() {
			return key;
		}

		@Override
		public String getPath() {
			return path;
		}

		@Override
		public T getDefaultValue() {
			return defaultValue;
		}

		@Override
		public String[] getComments() {
			return comments;
		}

		@Override
		public abstract boolean condition(T value);

		@Override
		@SuppressWarnings("unchecked")
		public T getValue() {
			if (isError()) {
				logger.error("콘피그가 불러와지는 도중 오류가 발생했습니다.");
				throw new IllegalStateException("콘피그가 불러와지는 도중 오류가 발생했습니다.");
			}
			if (lastModified != configFile.lastModified()) {
				try {
					_update();
				} catch (IOException | InvalidConfigurationException e) {
					logger.error("콘피그를 다시 불러오는 도중 오류가 발생하였습니다.");
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

		@Override
		public boolean setValue(T value) {
			if (condition(value)) {
				cache.put(this, new Cache(true, value));
				return true;
			}
			return false;
		}

		@Override
		public void reset() {
			setValue(defaultValue);
		}

		@Override
		public String toString() {
			return String.valueOf(getValue());
		}

	}

}
