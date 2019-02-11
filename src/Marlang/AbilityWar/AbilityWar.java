package Marlang.AbilityWar;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import Marlang.AbilityWar.Ability.AbilityBase;
import Marlang.AbilityWar.Ability.AbilityList;
import Marlang.AbilityWar.Config.AbilitySettings;
import Marlang.AbilityWar.Config.AbilityWarSettings;
import Marlang.AbilityWar.GameManager.MainCommand;
import Marlang.AbilityWar.GameManager.Manager.GUI.SpecialThanksGUI;
import Marlang.AbilityWar.GameManager.Module.Module;
import Marlang.AbilityWar.GameManager.Script.Script;
import Marlang.AbilityWar.GameManager.Script.Script.RequiredData;
import Marlang.AbilityWar.GameManager.Script.Types.TeleportScript;
import Marlang.AbilityWar.Utils.Messager;
import Marlang.AbilityWar.Utils.AutoUpdate.AutoUpdate;
import Marlang.AbilityWar.Utils.AutoUpdate.AutoUpdate.Branch;

/**
 * Ability War 능력자 전쟁 플러그인
 * @author _Marlang 말랑
 */
public class AbilityWar extends JavaPlugin {
	
	private static Plugin Plugin;
	
	public static Plugin getPlugin() {
		return AbilityWar.Plugin;
	}
	
	private AutoUpdate au = new AutoUpdate("Marlang365", "test", this, Branch.Master);
	
	@Override
	public void onEnable() {
		AbilityWar.Plugin = this;
		
		if(!Script.isRegistered(TeleportScript.class)) {
			Script.registerScript(TeleportScript.class, new RequiredData("텔레포트 위치", Location.class, null));
		}
		
		if(au.Check()) {
			Messager.sendMessage("Server Version: " + Bukkit.getServer().getBukkitVersion());
			
			Module.Setup();
			
			Load();
			
			/*
			 * 서버 부팅이 끝나면 실행
			 */
			Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(this, new Runnable() {
				@Override
				public void run() {
					AbilityWarSettings.Setup();
					AbilitySettings.Setup();
					Script.LoadAll();
				}
			});
			
			Messager.sendMessage("플러그인이 활성화되었습니다.");
		}
	}
	
	private void Load() {
		Bukkit.getPluginCommand("AbilityWar").setExecutor(new MainCommand());
		

		try {
			//OfflinePlayer Pre-Load
			Class.forName(SpecialThanksGUI.class.getName());
			
			for(String name : AbilityList.values()) {
				Class<? extends AbilityBase> Ability = AbilityList.getByString(name);
				Class.forName(Ability.getName());
			}
		} catch(ClassNotFoundException e) {
			Messager.sendErrorMessage(ChatColor.translateAlternateColorCodes('&', "&f능력을 불러오던 도중 오류가 발생하였습니다."));
		}
	}
	
	@Override
	public void onDisable() {
		AbilityWarSettings.Refresh();
		AbilitySettings.Refresh();
		
		Messager.sendMessage("플러그인이 비활성화되었습니다.");
	}
	
}
