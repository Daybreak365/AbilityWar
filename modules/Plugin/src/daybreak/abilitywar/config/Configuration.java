package daybreak.abilitywar.config;

import daybreak.abilitywar.config.enums.ConfigNodes;
import daybreak.abilitywar.config.enums.CooldownDecrease;
import daybreak.abilitywar.config.enums.OnDeath;
import daybreak.abilitywar.config.interfaces.Cacher;
import daybreak.abilitywar.config.serializable.SpawnLocation;
import daybreak.abilitywar.config.serializable.team.PresetContainer;
import daybreak.abilitywar.game.AbstractGame;
import daybreak.abilitywar.game.list.standard.StandardGame;
import daybreak.abilitywar.utils.base.io.FileUtil;
import daybreak.abilitywar.utils.base.logging.Logger;
import daybreak.abilitywar.utils.base.reflect.ReflectionUtil.ClassUtil;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.logging.Level;

public class Configuration {

	private Configuration() {
	}

	private static final Logger logger = Logger.getLogger(Configuration.class.getName());
	private static File file = null;
	private static long lastModified;
	private static CommentedConfiguration config = null;
	private static boolean error = false;

	public static boolean isError() {
		return error;
	}

	public static boolean isLoaded() {
		return file != null && config != null;
	}

	public static void update() throws IOException, InvalidConfigurationException {
		if (!isLoaded()) {
			try {
				file = FileUtil.newFile("Config.yml");
				lastModified = file.lastModified();
				config = new CommentedConfiguration(file);
			} catch (IOException | InvalidConfigurationException e) {
				error = true;
			}
		}
		config.load();

		for (Entry<ConfigNodes, Cache> entry : cache.entrySet()) {
			ConfigNodes node = entry.getKey();
			Cache cache = entry.getValue();
			if (cache.isModifiedValue()) {
				if (node.hasCacher()) {
					config.set(node.getPath(), node.getCacher().revertCache(cache.getValue()));
				} else {
					config.set(node.getPath(), cache.getValue());
				}
			}
		}

		cache.clear();
		for (ConfigNodes node : ConfigNodes.values()) {
			if (node.hasCacher()) {
				final Cacher handler = node.getCacher();
				if (config.isSet(node.getPath())) {
					cache.put(node, new Cache(false, handler.toCache(config.get(node.getPath()))));
				} else {
					config.set(node.getPath(), node.getDefault());
					cache.put(node, new Cache(false, handler.toCache(node.getDefault())));
				}
			} else {
				Object value = config.get(node.getPath());
				if (value != null) {
					cache.put(node, new Cache(false, value));
				} else {
					config.set(node.getPath(), node.getDefault());
					cache.put(node, new Cache(false, node.getDefault()));
				}
			}
			config.addComment(node.getPath(), node.getComments());
		}
		config.save();
		lastModified = file.lastModified();
	}

	public static CommentedConfiguration getConfig() {
		return config;
	}

	private static final EnumMap<ConfigNodes, Cache> cache = new EnumMap<>(ConfigNodes.class);

	@SuppressWarnings("unchecked") // private only method
	private static <T> T get(ConfigNodes configNode) throws IllegalStateException {
		if (!error) {
			if (!isLoaded()) {
				try {
					file = FileUtil.newFile("Config.yml");
					lastModified = file.lastModified();
					config = new CommentedConfiguration(file);
					update();
				} catch (IOException | InvalidConfigurationException e) {
					error = true;
				}
				return get(configNode);
			}
			if (lastModified != file.lastModified()) {
				try {
					update();
				} catch (IOException | InvalidConfigurationException e) {
					logger.log(Level.SEVERE, "콘피그를 다시 불러오는 도중 오류가 발생하였습니다.");
				}
			}
			return (T) cache.get(configNode).getValue();
		} else {
			throw new IllegalStateException("콘피그를 불러오는 도중 오류가 발생하였습니다.");
		}
	}

	@SuppressWarnings("unchecked") // private only method
	private static <T> List<T> getList(ConfigNodes configNode, Class<T> clazz) throws IllegalStateException {
		List<?> list = get(configNode);
		List<T> newList = new ArrayList<>();
		for (Object object : list) {
			if (object != null && clazz.isAssignableFrom(object.getClass())) {
				newList.add((T) object);
			}
		}
		return newList;
	}

	private static <T> Set<T> getSet(ConfigNodes configNode) throws IllegalStateException {
		return get(configNode);
	}

	public static void modifyProperty(ConfigNodes node, Object value) {
		cache.put(node, new Cache(true, value));
	}

	public static void updateProperty(ConfigNodes node) {
		cache.put(node, new Cache(true, cache.get(node).getValue()));
	}

	public static class Settings {

		private Settings() {
		}

		public static List<ItemStack> getItemStackList(ConfigNodes node) {
			return Configuration.getList(node, ItemStack.class);
		}

		public static boolean getNoHunger() {
			return get(ConfigNodes.GAME_NO_HUNGER);
		}

		public static int getStartLevel() {
			return get(ConfigNodes.GAME_START_LEVEL);
		}

		public static boolean getInventoryClear() {
			return get(ConfigNodes.GAME_INVENTORY_CLEAR);
		}

		public static boolean getDrawAbility() {
			return get(ConfigNodes.GAME_DRAW_ABILITY);
		}

		public static boolean isAutoSkipEnabled() {
			return get(ConfigNodes.GAME_DRAW_AUTOSKIP_ENABLED);
		}

		public static int getAutoSkipTime() {
			return get(ConfigNodes.GAME_DRAW_AUTOSKIP_TIME);
		}

		public static boolean getInfiniteDurability() {
			return get(ConfigNodes.GAME_INFINITE_DURABILITY);
		}

		public static boolean getFirewall() {
			return get(ConfigNodes.GAME_FIREWALL);
		}

		public static boolean getClearWeather() {
			return get(ConfigNodes.GAME_CLEAR_WEATHER);
		}

		public static boolean isArrowDamageDistanceProportional() {
			return get(ConfigNodes.GAME_ARROW_DISTANCE_PROPORTIONAL_DAMAGE);
		}

		public static boolean isShieldCooldownEnabled() {
			return get(ConfigNodes.GAME_SHIELD_COOLDOWN);
		}

		public static boolean isBowCooldownEnabled() {
			return get(ConfigNodes.GAME_BOW_COOLDOWN);
		}

		public static SpawnLocation getSpawnLocation() {
			return get(ConfigNodes.GAME_SPAWN_LOCATION);
		}

		public static boolean getSpawnEnable() {
			return get(ConfigNodes.GAME_SPAWN_ENABLE);
		}

		public static boolean getVisualEffect() {
			return get(ConfigNodes.GAME_VISUAL_EFFECT);
		}

		public static int getAbilityChangeCount() {
			return get(ConfigNodes.GAME_ABILITY_CHANGE_COUNT);
		}

		public static Set<String> getBlackList() {
			return getSet(ConfigNodes.GAME_BLACKLIST);
		}

		public static boolean isZeroTickEnabled() {
			return get(ConfigNodes.GAME_ZEROTICK);
		}

		public static boolean isTeamGameEnabled() {
			return get(ConfigNodes.GAME_TEAMGAME);
		}

		public static PresetContainer getPresetContainer() {
			return get(ConfigNodes.GAME_TEAM_PRESETS);
		}

		public static boolean isDefaultMaxHealthEnabled() {
			return get(ConfigNodes.GAME_DEFAULT_MAX_HEALTH_ENABLE);
		}

		public static int getDefaultMaxHealth() {
			return get(ConfigNodes.GAME_DEFAULT_MAX_HEALTH_VALUE);
		}

		public static boolean getDurationTimerBehavior() {
			return get(ConfigNodes.GAME_DURATION_TIMER_BEHAVIOR);
		}

		public static boolean isAutoWorldResetEnabled() {
			return get(ConfigNodes.WORLD_RESET_ON_GAME_END);
		}

		public static boolean isBlacklisted(String abilityName) {
			return getBlackList().contains(abilityName);
		}

		public static boolean isUsingBetaAbility() {
			return get(ConfigNodes.GAME_USE_BETA_ABILITY);
		}

		public static void addBlacklist(Collection<String> abilityNames) {
			Set<String> set = getBlackList();
			if (set.addAll(abilityNames)) {
				modifyProperty(ConfigNodes.GAME_BLACKLIST, set);
			}
		}

		public static void addBlacklist(String abilityName) {
			Set<String> set = getBlackList();
			if (set.add(abilityName)) {
				modifyProperty(ConfigNodes.GAME_BLACKLIST, set);
			}
		}

		public static void removeBlacklist(Collection<String> abilityNames) {
			Set<String> set = getBlackList();
			if (set.removeAll(abilityNames)) {
				modifyProperty(ConfigNodes.GAME_BLACKLIST, set);
			}
		}

		public static void removeBlacklist(String abilityName) {
			Set<String> set = getBlackList();
			if (set.remove(abilityName)) {
				modifyProperty(ConfigNodes.GAME_BLACKLIST, set);
			}
		}

		/**
		 * @return blacklist에서 삭제된 경우 false, blacklist에 추가된 경우 true
		 */
		public static boolean toggleBlacklist(String abilityName) {
			if (isBlacklisted(abilityName)) {
				removeBlacklist(abilityName);
				return false;
			} else {
				addBlacklist(abilityName);
				return true;
			}
		}

		public static class InvincibilitySettings {

			private InvincibilitySettings() {
			}

			public static boolean isEnabled() {
				return get(ConfigNodes.GAME_INVINCIBILITY_ENABLE);
			}

			public static int getDuration() {
				return get(ConfigNodes.GAME_INVINCIBILITY_DURATION);
			}

			public static boolean isBossbarEnabled() {
				return get(ConfigNodes.GAME_INVINCIBILITY_BOSSBAR_ENABLE);
			}

			public static String getBossbarMessage() {
				return get(ConfigNodes.GAME_INVINCIBILITY_BOSSBAR_MESSAGE);
			}

			public static String getBossbarInfiniteMessage() {
				return get(ConfigNodes.GAME_INVINCIBILITY_BOSSBAR_INFINITE_MESSAGE);
			}
		}

		public static class DeathSettings {

			private DeathSettings() {
			}

			public static OnDeath getOperation() {
				return OnDeath.getIfPresent(get(ConfigNodes.GAME_DEATH_OPERATION));
			}

			public static void nextOperation() {
				modifyProperty(ConfigNodes.GAME_DEATH_OPERATION, getOperation().next().name());
			}

			public static boolean getAbilityReveal() {
				return get(ConfigNodes.GAME_DEATH_ABILITY_REVEAL);
			}

			public static boolean getAutoRespawn() {
				return get(ConfigNodes.GAME_DEATH_AUTO_RESPAWN);
			}

		}

		public static class DeveloperSettings {

			private DeveloperSettings() {
			}

			public static boolean isEnabled() {
				return get(ConfigNodes.DEVELOPER);
			}

		}

		public static class AprilSettings {

			private AprilSettings() {
			}

			public static boolean isEnabled() {
				return get(ConfigNodes.DEVELOPER);
			}

		}

		public static Class<? extends AbstractGame> getGameMode() {
			try {
				Class<?> clazz = ClassUtil.forName(get(ConfigNodes.GAME_MODE));
				if (AbstractGame.class.isAssignableFrom(clazz)) {
					return clazz.asSubclass(AbstractGame.class);
				}
			} catch (ClassNotFoundException ignored) {
			}

			modifyProperty(ConfigNodes.GAME_MODE, StandardGame.class.getName());
			return StandardGame.class;
		}

		public static boolean isWreckEnabled() {
			return get(ConfigNodes.GAME_WRECK_ENABLE);
		}

		public static CooldownDecrease getCooldownDecrease() {
			return CooldownDecrease.getIfPresent(get(ConfigNodes.GAME_WRECK_DECREASE));
		}

	}

}
