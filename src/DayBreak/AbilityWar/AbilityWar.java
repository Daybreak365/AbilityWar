package DayBreak.AbilityWar;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.plugin.java.JavaPlugin;

import DayBreak.AbilityWar.Ability.AbilityList;
import DayBreak.AbilityWar.Config.AbilitySettings;
import DayBreak.AbilityWar.Config.AbilityWarSettings;
import DayBreak.AbilityWar.Development.Addon.AddonLoader;
import DayBreak.AbilityWar.Game.MainCommand;
import DayBreak.AbilityWar.Game.Script.Script;
import DayBreak.AbilityWar.Game.Script.Script.RequiredData;
import DayBreak.AbilityWar.Game.Script.Types.ChangeAbilityScript;
import DayBreak.AbilityWar.Game.Script.Types.ChangeAbilityScript.ChangeTarget;
import DayBreak.AbilityWar.Game.Script.Types.LocationNoticeScript;
import DayBreak.AbilityWar.Game.Script.Types.TeleportScript;
import DayBreak.AbilityWar.Utils.Messager;
import DayBreak.AbilityWar.Utils.AutoUpdate.AutoUpdate;
import DayBreak.AbilityWar.Utils.AutoUpdate.AutoUpdate.Branch;
import DayBreak.AbilityWar.Utils.VersionCompat.ServerVersion;

/**
 * Ability War 능력자 전쟁 플러그인
 * @author DayBreak 새벽
 */
public class AbilityWar extends JavaPlugin {
	
	private static AbilityWar Plugin;
	
	public static AbilityWar getPlugin() {
		return AbilityWar.Plugin;
	}
	
	private final AutoUpdate au = new AutoUpdate("DayBreak365", "AbilityWar", this, Branch.Master);

	@Override
	public void onEnable() {
		AbilityWar.Plugin = this;
		
		ServerVersion.VersionCompat(this);
		
		if(au.Check()) {
			Messager.sendMessage("Server Version: " + Bukkit.getServer().getBukkitVersion());
			
			Load();
			
			Messager.sendMessage("플러그인이 활성화되었습니다.");
		}
	}
	
	private void Load() {
		Bukkit.getPluginCommand("AbilityWar").setExecutor(new MainCommand());

		Script.registerScript(TeleportScript.class, new RequiredData<Location>("텔레포트 위치", Location.class));
		Script.registerScript(ChangeAbilityScript.class, new RequiredData<ChangeTarget>("능력 변경 대상", ChangeTarget.class));
		Script.registerScript(LocationNoticeScript.class);

		AbilityList.nameValues();

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
		
	}
	
	@Override
	public void onDisable() {
		AbilityWarSettings.Refresh();
		AbilitySettings.Refresh();
		AddonLoader.onDisable();
		
		Messager.sendMessage("플러그인이 비활성화되었습니다.");
	}
	
}
