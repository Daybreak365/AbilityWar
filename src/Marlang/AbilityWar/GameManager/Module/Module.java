package Marlang.AbilityWar.GameManager.Module;

import org.bukkit.Bukkit;
import org.bukkit.event.Listener;

import Marlang.AbilityWar.AbilityWar;
import Marlang.AbilityWar.GameManager.GameListener;
import Marlang.AbilityWar.GameManager.Module.OldMechanics.EnchantWithoutLapis;

abstract public class Module {
	
	public static void Setup() {
		ModuleList.values();
	}
	
	public void RegisterListener(Listener listener) {
		Bukkit.getPluginManager().registerEvents(listener, AbilityWar.getPlugin());
	}
	
	public static enum ModuleList {
		
		GameListener(new GameListener()),
		EnchantWithoutLapis(new EnchantWithoutLapis());
		
		Module module;
		
		private ModuleList(Module module) {
			this.module = module;
		}
		
		public Module getModule() {
			return module;
		}
		
	}
	
}
