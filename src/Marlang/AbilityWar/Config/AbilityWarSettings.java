package Marlang.AbilityWar.Config;

import java.util.ArrayList;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.inventory.ItemStack;

import Marlang.AbilityWar.Config.Nodes.ConfigNodes;
import Marlang.AbilityWar.Utils.FileManager;
import Marlang.AbilityWar.Utils.Messager;

public class AbilityWarSettings {
	
	private static CommentedConfiguration Config = new CommentedConfiguration(FileManager.getFile("Config.yml"));
	
	public static void Setup() {
		CommentedConfiguration newConfig = new CommentedConfiguration(FileManager.getFile("Config.yml"));
		Config.load();
		
		for(ConfigNodes n : ConfigNodes.values()) {
			if(getConfig(n) != null) {
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
	
	public static boolean getEliminate() {
		return getBoolean(ConfigNodes.Game_Deaeth_Eliminate);
	}
	
	public static boolean getAbilityReveal() {
		return getBoolean(ConfigNodes.Game_Deaeth_AbilityReveal);
	}
	
	public static boolean getClearWeather() {
		return getBoolean(ConfigNodes.Game_ClearWeather);
	}
	
	public static ArrayList<ItemStack> getDefaultKit() {
		return getItemStackList(ConfigNodes.Game_Kit);
	}

	public static Location getSpawnLocation() {
		return getLocation(ConfigNodes.Game_Spawn_Location);
	}

	public static boolean getSpawnEnable() {
		return getBoolean(ConfigNodes.Game_Spawn_Enable);
	}

	public static boolean getOldEnchant() {
		return getBoolean(ConfigNodes.Game_OldMechanics_Enchant);
	}
	
	public static int getInt(ConfigNodes node) {
		try {
			return Config.getInt(node.getPath());
		} catch (Exception e) {
			Messager.sendErrorMessage("Config.yml, " + node.getPath() + "에서 오류가 발생하였습니다.");
			setNewProperty(node, node.getDefault());
			Config.save();
			return Config.getInt(node.getPath());
		}
	}
	
	public static boolean getBoolean(ConfigNodes node) {
		try {
			return Config.getBoolean(node.getPath());
		} catch (Exception e) {
			Messager.sendErrorMessage("Config.yml, " + node.getPath() + "에서 오류가 발생하였습니다.");
			Config.set(node.getPath(), node.getDefault());
			Config.save();
			return Config.getBoolean(node.getPath());
		}
	}
	
	public static ArrayList<String> getStringList(ConfigNodes node) {
		try {
			ArrayList<String> List = new ArrayList<String>();
			
			for(Object o : Config.getList(node.getPath())) {
				List.add(o.toString());
			}
			
			return List;
		} catch (Exception e) {
			Messager.sendErrorMessage("Config.yml, " + node.getPath() + "에서 오류가 발생하였습니다.");
			setNewProperty(node, node.getDefault());
			Config.save();
			
			ArrayList<String> List = new ArrayList<String>();
			
			for(Object o : Config.getList(node.getPath())) {
				List.add(o.toString());
			}
			
			return List;
		}
	}
	
	public static ArrayList<ItemStack> getItemStackList(ConfigNodes node) {
		try {
			ArrayList<ItemStack> List = new ArrayList<ItemStack>();
			
			for(Object o : Config.getList(node.getPath())) {
				if(o instanceof ItemStack) {
					List.add((ItemStack) o);
				} else {
					throw new Exception();
				}
			}
			
			return List;
		} catch (Exception e) {
			Messager.sendErrorMessage("Config.yml, " + node.getPath() + "에서 오류가 발생하였습니다.");
			setNewProperty(node, node.getDefault());
			Config.save();

			ArrayList<ItemStack> List = new ArrayList<ItemStack>();
			
			for(Object o : Config.getList(node.getPath())) {
				if(o instanceof ItemStack) {
					List.add((ItemStack) o);
				}
			}
			
			return List;
		}
	}
	
	public static Location getLocation(ConfigNodes node) {
		try {
			return (Location) Config.get(node.getPath());
		} catch (Exception e) {
			Messager.sendErrorMessage("Config.yml, " + node.getPath() + "에서 오류가 발생하였습니다.");
			setNewProperty(node, Bukkit.getWorlds().get(0).getSpawnLocation());
			Config.save();
			return (Location) Config.get(node.getPath());
		}
	}

	public static void setNewProperty(ConfigNodes node, Object value) {
		Config.set(node.getPath(), value);
		Config.save();
		Setup();
		Config.load();
	}
	
	public static void Reload() {
		Config.load();
	}
	
}
