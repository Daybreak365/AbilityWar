package daybreak.abilitywar.config;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.inventory.ItemStack;

import daybreak.abilitywar.config.enums.ConfigNodes;
import daybreak.abilitywar.config.enums.OnDeath;
import daybreak.abilitywar.game.games.defaultgame.DefaultGame;
import daybreak.abilitywar.game.games.mode.AbstractGame;
import daybreak.abilitywar.utils.Messager;
import daybreak.abilitywar.utils.ReflectionUtil.ClassUtil;
import daybreak.abilitywar.utils.database.FileManager;

public class AbilityWarSettings {

	private static CommentedConfiguration Config = new CommentedConfiguration(FileManager.getFile("Config.yml"));

	public static void Setup() {
		CommentedConfiguration newConfig = new CommentedConfiguration(FileManager.getFile("Config.yml"));
		Config.load();

		for (ConfigNodes n : ConfigNodes.values()) {
			if (getConfig(n) != null) {
				newConfig.set(n.getPath(), getConfig(n));
			} else {
				newConfig.set(n.getPath(), n.getDefault());
			}
			newConfig.addComment(n.getPath(), n.getComments());
		}

		Config = newConfig;

		Config.save();
		newConfig = null;
	}

	private static HashMap<ConfigNodes, Object> Cache = new HashMap<ConfigNodes, Object>();

	public static Object getConfig(ConfigNodes node) {
		return Config.get(node.getPath());
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
			setNewProperty(ConfigNodes.Game_Death_Operation, getOperation().Next().name());
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
		if(!list.contains(abilityName)) {
			list.add(abilityName);
			setNewProperty(ConfigNodes.Game_BlackList, list);
		}
	}

	public static void removeBlackList(String abilityName) {
		List<String> list = getStringList(ConfigNodes.Game_BlackList);
		if(list.contains(abilityName)) {
			list.remove(abilityName);
			setNewProperty(ConfigNodes.Game_BlackList, list);
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
			if(AbstractGame.class.isAssignableFrom(clazz)) {
				return (Class<? extends AbstractGame>) clazz;
			}
		} catch (ClassNotFoundException e) {}
		
		setNewProperty(ConfigNodes.GameMode, DefaultGame.class.getName());
		return DefaultGame.class;
	}

	public static boolean getWRECKEnable() {
		return getBoolean(ConfigNodes.Game_WRECK);
	}
	
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
	
	@SuppressWarnings("unchecked")
	private static <T> T get(ConfigNodes node, Class<T> clazz) {
		if (Cache.containsKey(node)) {
			Object o = Cache.get(node);
			if (o != null && clazz.isAssignableFrom(o.getClass())) {
				return (T) o;
			}
		}

		try {
			T o = (T) Config.get(node.getPath());
			Cache.put(node, o);
			return o;
		} catch (Exception e) {
			Messager.sendConsoleErrorMessage("Config.yml, " + node.getPath() + "에서 오류가 발생하였습니다.");
			Config.set(node.getPath(), node.getDefault());
			Config.save();

			T o = (T) Config.get(node.getPath());
			Cache.put(node, o);
			return o;
		}
	}

	@SuppressWarnings("unchecked")
	private static <T> List<T> getList(ConfigNodes node, Class<T> clazz) {
		try {
			if (Cache.containsKey(node)) {
				Object o = Cache.get(node);
				if (o != null && o instanceof List) {
					List<?> objects = (List<?>) o;
					List<T> list = new ArrayList<T>();

					for (Object obj : objects) {
						if (clazz.isAssignableFrom(obj.getClass())) {
							list.add((T) obj);
						} else {
							throw new Exception();
						}
					}
					
					return list;
				}
			}

			List<?> objects = Config.getList(node.getPath());
			List<T> list = new ArrayList<T>();

			for (Object obj : objects) {
				if (clazz.isAssignableFrom(obj.getClass())) {
					list.add((T) obj);
				} else {
					throw new Exception();
				}
			}

			return list;
		} catch (Exception e) {
			Messager.sendConsoleErrorMessage("Config.yml, " + node.getPath() + "에서 오류가 발생하였습니다.");
			Config.set(node.getPath(), node.getDefault());
			Config.save();

			List<?> objects = Config.getList(node.getPath());
			List<T> list = new ArrayList<T>();

			for (Object obj : objects) {
				if (clazz.isAssignableFrom(obj.getClass())) {
					list.add((T) obj);
				}
			}

			return list;
		}
	}
	
	public static void setNewProperty(ConfigNodes node, Object value) {
		Cache.put(node, value);
	}
	
	public static void Save() {
		for(ConfigNodes node : Cache.keySet()) {
			if(node != null) {
				Config.set(node.getPath(), Cache.get(node));
			}
		}
		
		Config.save();
	}
	
	public static void Refresh() {
		Config.load();
		Setup();
	}
	
}
