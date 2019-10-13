package daybreak.abilitywar.config;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.Location;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.inventory.ItemStack;

import daybreak.abilitywar.config.enums.ConfigNodes;
import daybreak.abilitywar.config.enums.OnDeath;
import daybreak.abilitywar.game.games.defaultgame.DefaultGame;
import daybreak.abilitywar.game.games.mode.AbstractGame;
import daybreak.abilitywar.utils.ReflectionUtil.ClassUtil;
import daybreak.abilitywar.utils.database.FileManager;

public class AbilityWarSettings {

	private static final Logger logger = Logger.getLogger(AbilityWarSettings.class.getName());
	private static File file = null;
	private static long lastModified;
	private static CommentedConfiguration config = null;

	public static boolean isLoaded() {
		return file != null && config != null;
	}

	public static void load() throws IOException, InvalidConfigurationException {
		if (!isLoaded()) {
			file = FileManager.getFile("Config.yml");
			lastModified = file.lastModified();
			config = new CommentedConfiguration(file);
			update();
		}
	}

	public static void update() throws FileNotFoundException, IOException, InvalidConfigurationException {
		config.load();

		for (Entry<ConfigNodes, Cache> entry : cache.entrySet()) {
			Cache cache = entry.getValue();
			if (cache.isModifiedValue()) {
				if (entry.getKey().equals(ConfigNodes.Game_Death_AbilityReveal)) {
					logger.log(Level.SEVERE, "");
				}
				config.set(entry.getKey().getPath(), cache.getValue());
			}
		}

		cache.clear();
		for (ConfigNodes node : ConfigNodes.values()) {
			Object value = config.get(node.getPath());
			if (value != null) {
				cache.put(node, new Cache(false, value));
			} else {
				config.set(node.getPath(), node.getDefault());
				cache.put(node, new Cache(false, node.getDefault()));
			}
		}
		config.save();
		lastModified = file.lastModified();
	}

	private static EnumMap<ConfigNodes, Cache> cache = new EnumMap<>(ConfigNodes.class);

	@SuppressWarnings("unchecked") // Private only method
	private static <T> T get(ConfigNodes configNode, Class<T> clazz) throws IllegalStateException {
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
		return (T) cache.get(configNode).getValue();
	}

	@SuppressWarnings("unchecked") // Private only method
	private static <T> List<T> getList(ConfigNodes configNode, Class<T> clazz) throws IllegalStateException {
		List<?> list = (List<?>) get(configNode, List.class);
		List<T> newList = new ArrayList<>();
		for (Object object : list) {
			if (object != null && clazz.isAssignableFrom(object.getClass())) {
				newList.add((T) object);
			}
		}
		return newList;
	}

	public static void modifyProperty(ConfigNodes node, Object value) {
		cache.put(node, new Cache(true, value));
	}

	public static class Settings {

		/**
		 * String Config
		 */
		public static String getString(ConfigNodes node) {
			return get(node, String.class);
		}

		/**
		 * Integer Config
		 */
		public static int getInt(ConfigNodes node) {
			return get(node, Integer.class);
		}

		/**
		 * Boolean Config
		 */
		public static boolean getBoolean(ConfigNodes node) {
			return get(node, Boolean.class);
		}

		/**
		 * Location Config
		 */
		public static Location getLocation(ConfigNodes node) {
			return get(node, Location.class);
		}

		public static List<String> getStringList(ConfigNodes node) {
			return getList(node, String.class);
		}

		public static List<ItemStack> getItemStackList(ConfigNodes node) {
			return getList(node, ItemStack.class);
		}

		public static boolean getNoHunger() {
			return getBoolean(ConfigNodes.Game_NoHunger);
		}

		public static int getStartLevel() {
			return getInt(ConfigNodes.Game_StartLevel);
		}

		public static boolean getInvincibilityEnable() {
			return getBoolean(ConfigNodes.Game_Invincibility_Enable);
		}

		public static int getInvincibilityDuration() {
			return getInt(ConfigNodes.Game_Invincibility_Duration);
		}

		public static boolean getInventoryClear() {
			return getBoolean(ConfigNodes.Game_InventoryClear);
		}

		public static boolean getDrawAbility() {
			return getBoolean(ConfigNodes.Game_DrawAbility);
		}

		public static boolean getInfiniteDurability() {
			return getBoolean(ConfigNodes.Game_InfiniteDurability);
		}

		public static boolean getFirewall() {
			return getBoolean(ConfigNodes.Game_Firewall);
		}

		public static class DeathSettings {

			public static OnDeath getOperation() {
				return OnDeath.getIfPresent(getString(ConfigNodes.Game_Death_Operation));
			}

			public static void nextOperation() {
				modifyProperty(ConfigNodes.Game_Death_Operation, getOperation().Next().name());
			}

			public static boolean getItemDrop() {
				return getBoolean(ConfigNodes.Game_Death_ItemDrop);
			}

			public static boolean getAbilityReveal() {
				return getBoolean(ConfigNodes.Game_Death_AbilityReveal);
			}

			public static boolean getAbilityRemoval() {
				return getBoolean(ConfigNodes.Game_Death_AbilityRemoval);
			}

		}

		public static boolean getClearWeather() {
			return getBoolean(ConfigNodes.Game_ClearWeather);
		}

		public static List<ItemStack> getDefaultKit() {
			return getItemStackList(ConfigNodes.Game_Kit);
		}

		public static Location getSpawnLocation() {
			return getLocation(ConfigNodes.Game_Spawn_Location);
		}

		public static boolean getSpawnEnable() {
			return getBoolean(ConfigNodes.Game_Spawn_Enable);
		}

		public static boolean getVisualEffect() {
			return getBoolean(ConfigNodes.Game_VisualEffect);
		}

		public static boolean isBlackListed(String abilityName) {
			return getBlackList().contains(abilityName);
		}

		public static List<String> getBlackList() {
			return getStringList(ConfigNodes.Game_BlackList);
		}

		public static void addBlackList(String abilityName) {
			List<String> list = getStringList(ConfigNodes.Game_BlackList);
			if (!list.contains(abilityName)) {
				list.add(abilityName);
				modifyProperty(ConfigNodes.Game_BlackList, list);
			}
		}

		public static void removeBlackList(String abilityName) {
			List<String> list = getStringList(ConfigNodes.Game_BlackList);
			if (list.contains(abilityName)) {
				list.remove(abilityName);
				modifyProperty(ConfigNodes.Game_BlackList, list);
			}
		}

		public static class ChangeAbilityWarSettings {

			public static int getPeriod() {
				return getInt(ConfigNodes.AbilityChangeGame_Period);
			}

			public static int getLife() {
				return getInt(ConfigNodes.AbilityChangeGame_Life);
			}

			public static boolean getEliminate() {
				return getBoolean(ConfigNodes.AbilityChangeGame_Eliminate);
			}

		}

		public static class SummerVacationSettings {

			public static int getMaxKill() {
				return getInt(ConfigNodes.SummerVacation_Kill);
			}

		}

		@SuppressWarnings("unchecked")
		public static Class<? extends AbstractGame> getGameMode() {
			try {
				Class<?> clazz = ClassUtil.forName(getString(ConfigNodes.GameMode));
				if (AbstractGame.class.isAssignableFrom(clazz)) {
					return (Class<? extends AbstractGame>) clazz;
				}
			} catch (ClassNotFoundException e) {
			}

			modifyProperty(ConfigNodes.GameMode, DefaultGame.class.getName());
			return DefaultGame.class;
		}

		public static boolean isWRECKEnabled() {
			return getBoolean(ConfigNodes.Game_WRECK);
		}

	}

}

class Cache {

	private final boolean isModifiedValue;
	private final Object value;

	Cache(boolean isModifiedValue, Object value) {
		this.isModifiedValue = isModifiedValue;
		this.value = value;
	}

	boolean isModifiedValue() {
		return isModifiedValue;
	}

	Object getValue() {
		return value;
	}

}
