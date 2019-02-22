package Marlang.AbilityWar.Addon;

import org.bukkit.plugin.Plugin;

import Marlang.AbilityWar.AbilityWar;
import Marlang.AbilityWar.Addon.AddonLoader.DescriptionFile;

abstract public class Addon {
	
	private DescriptionFile description;
	
	@SuppressWarnings("unused")
	private void setDescription(DescriptionFile description) {
		this.description = description;
	}
	
	abstract public void onEnable();
	
	abstract public void onDisable();
	
	protected Plugin getPlugin() {
		return AbilityWar.getPlugin();
	}
	
	public DescriptionFile getDescription() {
		return description;
	}
	
}
