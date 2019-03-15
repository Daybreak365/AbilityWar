package Marlang.AbilityWar;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.plugin.java.JavaPlugin;

import Marlang.AbilityWar.Ability.AbilityBase;
import Marlang.AbilityWar.Ability.AbilityList;
import Marlang.AbilityWar.Addon.AddonLoader;
import Marlang.AbilityWar.Config.AbilitySettings;
import Marlang.AbilityWar.Config.AbilityWarSettings;
import Marlang.AbilityWar.Game.MainCommand;
import Marlang.AbilityWar.Game.Script.Script;
import Marlang.AbilityWar.Game.Script.Script.RequiredData;
import Marlang.AbilityWar.Game.Script.Types.TeleportScript;
import Marlang.AbilityWar.Utils.Messager;
import Marlang.AbilityWar.Utils.AutoUpdate.AutoUpdate;
import Marlang.AbilityWar.Utils.AutoUpdate.AutoUpdate.Branch;
import Marlang.AbilityWar.Utils.VersionCompat.ServerVersion;

/**
 * Ability War 능력자 전쟁 플러그인
 * @author _Marlang 말랑
 */
public class AbilityWar extends JavaPlugin {
	
	private static AbilityWar Plugin;
	
	public static AbilityWar getPlugin() {
		return AbilityWar.Plugin;
	}
	
	private final AutoUpdate au = new AutoUpdate("Marlang365", "AbilityWar", this, Branch.Master);
	
	@Override
	public void onEnable() {
		AbilityWar.Plugin = this;
		
		ServerVersion.VersionCompat(this);
		
		if(au.Check()) {
			Messager.sendMessage("Server Version: " + Bukkit.getServer().getBukkitVersion());

			Script.registerScript(TeleportScript.class, new RequiredData<Location>("텔레포트 위치", Location.class));
			
			Load();
			
			AddonLoader.loadAddons();
			AddonLoader.onEnable();
			
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
			for(String name : AbilityList.nameValues()) {
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
		AddonLoader.onDisable();
		
		Messager.sendMessage("플러그인이 비활성화되었습니다.");
	}
	
}
