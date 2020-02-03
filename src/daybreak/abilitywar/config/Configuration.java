package daybreak.abilitywar.config;

import daybreak.abilitywar.config.enums.ConfigNodes;
import daybreak.abilitywar.config.enums.OnDeath;
import daybreak.abilitywar.game.games.mode.AbstractGame;
import daybreak.abilitywar.game.games.standard.DefaultGame;
import daybreak.abilitywar.utils.ReflectionUtil.ClassUtil;
import daybreak.abilitywar.utils.base.io.FileUtil;
import org.bukkit.Location;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Configuration {

	private static final Logger logger = Logger.getLogger(Configuration.class.getName());
	private static File file = null;
	private static long lastModified;
	private static CommentedConfiguration config = null;

	public static boolean isLoaded() {
		return file != null && config != null;
	}

	public static void load() throws IOException, InvalidConfigurationException {
		if (!isLoaded()) {
			file = FileUtil.newFile("Config.yml");
			lastModified = file.lastModified();
			config = new CommentedConfiguration(file);
			update();
		}
	}

	public static void update() throws IOException, InvalidConfigurationException {
		config.load();

		for (Entry<ConfigNodes, Cache> entry : cache.entrySet()) {
			Cache cache = entry.getValue();
			if (cache.isModifiedValue()) {
				config.set(entry.getKey().getPath(), cache.getValue());
			}
		}

		cache.clear();
		for (ConfigNodes node : ConfigNodes.values()) {
			Object value = config.get(node.getPath());
			config.addComment(node.getPath(), node.getComments());
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

	private static final EnumMap<ConfigNodes, Cache> cache = new EnumMap<>(ConfigNodes.class);

	@SuppressWarnings("unchecked") // private only method
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

	@SuppressWarnings("unchecked") // private only method
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

		public static String getString(ConfigNodes node) {
			return get(node, String.class);
		}
		public static int getInt(ConfigNodes node) {
			return get(node, Integer.class);
		}
		public static boolean getBoolean(ConfigNodes node) {
			return get(node, Boolean.class);
		}
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
			return getBoolean(ConfigNodes.GAME_NO_HUNGER);
		}
		public static int getStartLevel() {
			return getInt(ConfigNodes.GAME_START_LEVEL);
		}
		public static boolean getInventoryClear() {
			return getBoolean(ConfigNodes.GAME_INVENTORY_CLEAR);
		}

		public static List<ItemStack> getDefaultKit() {
			return getItemStackList(ConfigNodes.GAME_KIT);
		}
		public static boolean getDrawAbility() {
			return getBoolean(ConfigNodes.GAME_DRAW_ABILITY);
		}
		public static boolean getInfiniteDurability() {
			return getBoolean(ConfigNodes.GAME_INFINITE_DURABILITY);
		}
		public static boolean getFirewall() {
			return getBoolean(ConfigNodes.GAME_FIREWALL);
		}
		public static boolean getClearWeather() {
			return getBoolean(ConfigNodes.GAME_CLEAR_WEATHER);
		}
		public static Location getSpawnLocation() {
			return getLocation(ConfigNodes.GAME_SPAWN_LOCATION);
		}
		public static boolean getSpawnEnable() {
			return getBoolean(ConfigNodes.GAME_SPAWN_ENABLE);
		}
		public static boolean getVisualEffect() {
			return getBoolean(ConfigNodes.GAME_VISUAL_EFFECT);
		}

		public static boolean isBlackListed(String abilityName) {
			return getBlackList().contains(abilityName);
		}

		public static List<String> getBlackList() {
			return getStringList(ConfigNodes.GAME_BLACKLIST);
		}

		public static void addBlackListAll(Collection<String> abilityNames) {
			List<String> list = getStringList(ConfigNodes.GAME_BLACKLIST);
			if (!list.containsAll(abilityNames)) {
				list.addAll(abilityNames);
				modifyProperty(ConfigNodes.GAME_BLACKLIST, list);
			}
		}

		public static void addBlackList(String abilityName) {
			List<String> list = getStringList(ConfigNodes.GAME_BLACKLIST);
			if (!list.contains(abilityName)) {
				list.add(abilityName);
				modifyProperty(ConfigNodes.GAME_BLACKLIST, list);
			}
		}

		public static void removeBlackListAll(Collection<String> abilityNames) {
			List<String> list = getStringList(ConfigNodes.GAME_BLACKLIST);
			if (list.removeAll(abilityNames)) {
				modifyProperty(ConfigNodes.GAME_BLACKLIST, list);
			}
		}

		public static void removeBlackList(String abilityName) {
			List<String> list = getStringList(ConfigNodes.GAME_BLACKLIST);
			if (list.contains(abilityName)) {
				list.remove(abilityName);
				modifyProperty(ConfigNodes.GAME_BLACKLIST, list);
			}
		}

		public static class InvincibilitySettings {
			public static boolean isEnabled() {
				return getBoolean(ConfigNodes.GAME_INVINCIBILITY_ENABLE);
			}

			public static int getDuration() {
				return getInt(ConfigNodes.GAME_INVINCIBILITY_DURATION);
			}

			public static boolean isBossbarEnabled() {
				return getBoolean(ConfigNodes.GAME_INVINCIBILITY_BOSSBAR_ENABLE);
			}

			public static String getBossbarMessage() {
				return getString(ConfigNodes.GAME_INVINCIBILITY_BOSSBAR_MESSAGE);
			}

			public static String getBossbarInfiniteMessage() {
				return getString(ConfigNodes.GAME_INVINCIBILITY_BOSSBAR_INFINITE_MESSAGE);
			}
		}

		public static class DeathSettings {

			public static OnDeath getOperation() {
				return OnDeath.getIfPresent(getString(ConfigNodes.GAME_DEATH_OPERATION));
			}

			public static void nextOperation() {
				modifyProperty(ConfigNodes.GAME_DEATH_OPERATION, getOperation().Next().name());
			}

			public static boolean getAbilityReveal() {
				return getBoolean(ConfigNodes.GAME_DEATH_ABILITY_REVEAL);
			}

			public static boolean getAbilityRemoval() {
				return getBoolean(ConfigNodes.GAME_DEATH_ABILITY_REMOVAL);
			}

		}

		public static class ChangeAbilityWarSettings {

			public static int getPeriod() {
				return getInt(ConfigNodes.ABILITY_CHANGE_GAME_PERIOD);
			}
			public static int getLife() {
				return getInt(ConfigNodes.ABILITY_CHANGE_GAME_LIFE);
			}
			public static boolean getEliminate() {
				return getBoolean(ConfigNodes.ABILITY_CHANGE_GAME_ELIMINATE);
			}

		}

		public static class SummerVacationSettings {

			public static int getMaxKill() {
				return getInt(ConfigNodes.SUMMER_VACATION_KILL);
			}

		}

		@SuppressWarnings("unchecked")
		public static Class<? extends AbstractGame> getGameMode() {
			try {
				Class<?> clazz = ClassUtil.forName(getString(ConfigNodes.GAME_MODE));
				if (AbstractGame.class.isAssignableFrom(clazz)) {
					return (Class<? extends AbstractGame>) clazz;
				}
			} catch (ClassNotFoundException e) {
			}

			modifyProperty(ConfigNodes.GAME_MODE, DefaultGame.class.getName());
			return DefaultGame.class;
		}

		public static boolean isWRECKEnabled() {
			return getBoolean(ConfigNodes.GAME_WRECK);
		}

	}

}
