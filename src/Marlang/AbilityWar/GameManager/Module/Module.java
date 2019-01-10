package Marlang.AbilityWar.GameManager.Module;

import org.bukkit.Bukkit;
import org.bukkit.event.Listener;

import Marlang.AbilityWar.AbilityWar;
import Marlang.AbilityWar.Config.SettingWizard;
import Marlang.AbilityWar.GameManager.AbilityGUI;
import Marlang.AbilityWar.GameManager.DeathManager;
import Marlang.AbilityWar.GameManager.GameListener;
import Marlang.AbilityWar.GameManager.Module.OldMechanics.EnchantWithoutLapis;

abstract public class Module {
	
	static AbilityWar Plugin;
	
	public static void Initialize(AbilityWar Plugin) {
		Module.Plugin = Plugin;
		ModuleList.values();
	}
	
	public void RegisterListener(Listener listener) {
		Bukkit.getPluginManager().registerEvents(listener, Plugin);
	}
	
	public static enum ModuleList {
		
		GameListener(new GameListener()),
		DeathManager(new DeathManager()),
		SettingWizard(new SettingWizard()),
		AbilityGUI(new AbilityGUI()),
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
