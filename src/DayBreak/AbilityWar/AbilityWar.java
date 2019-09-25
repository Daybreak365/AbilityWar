package DayBreak.AbilityWar;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.plugin.java.JavaPlugin;

import DayBreak.AbilityWar.Addon.AddonLoader;
import DayBreak.AbilityWar.Config.AbilitySettings;
import DayBreak.AbilityWar.Config.AbilityWarSettings;
import DayBreak.AbilityWar.Game.MainCommand;
import DayBreak.AbilityWar.Game.Manager.AbilityList;
import DayBreak.AbilityWar.Game.Script.Script;
import DayBreak.AbilityWar.Game.Script.Script.RequiredData;
import DayBreak.AbilityWar.Game.Script.Types.ChangeAbilityScript;
import DayBreak.AbilityWar.Game.Script.Types.ChangeAbilityScript.ChangeTarget;
import DayBreak.AbilityWar.Game.Script.Types.LocationNoticeScript;
import DayBreak.AbilityWar.Game.Script.Types.TeleportScript;
import DayBreak.AbilityWar.Utils.Messager;
import DayBreak.AbilityWar.Utils.AutoUpdate.AutoUpdate;
import DayBreak.AbilityWar.Utils.AutoUpdate.AutoUpdate.Branch;
import DayBreak.AbilityWar.Utils.Thread.AbilityWarThread;
import DayBreak.AbilityWar.Utils.VersionCompat.ServerVersion;

/**
 * Ability War 능력자 전쟁 플러그인
 * @author DayBreak 새벽
 */
public class AbilityWar extends JavaPlugin {
	
	private static final Messager messager = new Messager();
	private static AbilityWar Plugin;
	
	public static AbilityWar getPlugin() {
		return AbilityWar.Plugin;
	}
	
	private final AutoUpdate au = new AutoUpdate("DayBreak365", "AbilityWar", this, Branch.Master);

	public AutoUpdate getAutoUpdate() {
		return au;
	}

	public AbilityWar() {
		AbilityWar.Plugin = this;
	}
	
	@Override
	public void onEnable() {
		ServerVersion.VersionCompat(this);

		au.Check();
		
		messager.sendConsoleMessage("Server Version: " + Bukkit.getServer().getBukkitVersion());

		
		Bukkit.getPluginCommand("AbilityWar").setExecutor(new MainCommand());

		Script.registerScript(TeleportScript.class, new RequiredData<Location>("텔레포트 위치", Location.class));
		Script.registerScript(ChangeAbilityScript.class, new RequiredData<ChangeTarget>("능력 변경 대상", ChangeTarget.class));
		Script.registerScript(LocationNoticeScript.class);
		
		AbilityList.nameValues();

		AddonLoader.loadAddons();
		AddonLoader.enableAll();
		
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
		
		
		messager.sendConsoleMessage("플러그인이 활성화되었습니다.");
	}
	
	@Override
	public void onDisable() {
		AbilityWarThread.StopGame();
		AbilityWarSettings.Refresh();
		AbilitySettings.Refresh();
		AddonLoader.disableAll();
		
		messager.sendConsoleMessage("플러그인이 비활성화되었습니다.");
	}
	
}
