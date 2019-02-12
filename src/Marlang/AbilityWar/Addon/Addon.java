package Marlang.AbilityWar.Addon;

import org.bukkit.plugin.Plugin;

import Marlang.AbilityWar.AbilityWar;

abstract public class Addon {
	
	abstract public void onEnable();
	
	abstract public void onDisable();
	
	protected Plugin getPlugin() {
		return AbilityWar.getPlugin();
	}
	
}
