package Marlang.AbilityWar.Config;

import java.util.ArrayList;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.inventory.ItemStack;

import Marlang.AbilityWar.Config.Nodes.ConfigNodes;
import Marlang.AbilityWar.Utils.FileManager;
import Marlang.AbilityWar.Utils.Messager;

public class AbilityWarSettings {
	
	public AbilityWarSettings() {
		Config = new CommentedConfiguration(FileManager.getFile("Config.yml"));
	}
	
	CommentedConfiguration Config, newConfig;
	
	public void Setup() {
		newConfig = new CommentedConfiguration(FileManager.getFile("Config.yml"));
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
	
	public Object getConfig(ConfigNodes node) {
		return Config.get(node.getPath());
	}
	
	public boolean getNoHunger() {
		return getBoolean(ConfigNodes.Game_NoHunger);
	}
	
	public int getStartLevel() {
		return getInt(ConfigNodes.Game_StartLevel);
	}
	
	public boolean getInvincibilityEnable() {
		return getBoolean(ConfigNodes.Game_Invincibility_Enable);
	}
	
	public int getInvincibilityDuration() {
		return getInt(ConfigNodes.Game_Invincibility_Duration);
	}

	public ArrayList<ItemStack> getDefaultKit() {
		return getItemStackList(ConfigNodes.Game_Kit);
	}

	public Location getSpawnLocation() {
		return getLocation(ConfigNodes.Game_Spawn_Location);
	}

	public boolean getSpawnEnable() {
		return getBoolean(ConfigNodes.Game_Spawn_Enable);
	}

	public boolean getOldEnchant() {
		return getBoolean(ConfigNodes.Game_OldMechanics_Enchant);
	}
	
	public int getInt(ConfigNodes node) {
		Reload();
		try {
			return Config.getInt(node.getPath());
		} catch (Exception e) {
			Messager.sendErrorMessage("Config.yml, " + node.getPath() + "에서 오류가 발생하였습니다.");
			setNewProperty(node, node.getDefault());
			Config.save();
			return Config.getInt(node.getPath());
		}
	}
	
	public boolean getBoolean(ConfigNodes node) {
		Reload();
		try {
			return Config.getBoolean(node.getPath());
		} catch (Exception e) {
			Messager.sendErrorMessage("Config.yml, " + node.getPath() + "에서 오류가 발생하였습니다.");
			Config.set(node.getPath(), node.getDefault());
			Config.save();
			return Config.getBoolean(node.getPath());
		}
	}
	
	public ArrayList<String> getStringList(ConfigNodes node) {
		Reload();
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
	
	public ArrayList<ItemStack> getItemStackList(ConfigNodes node) {
		Reload();
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
	
	public Location getLocation(ConfigNodes node) {
		Reload();
		try {
			return (Location) Config.get(node.getPath());
		} catch (Exception e) {
			Messager.sendErrorMessage("Config.yml, " + node.getPath() + "에서 오류가 발생하였습니다.");
			setNewProperty(node, Bukkit.getWorlds().get(0).getSpawnLocation());
			Config.save();
			return (Location) Config.get(node.getPath());
		}
	}

	public void setNewProperty(ConfigNodes node, Object value) {
		Config.set(node.getPath(), value);
		Config.save();
		Setup();
		Config.load();
	}
	
	public void Reload() {
		Config.load();
	}
	
}
