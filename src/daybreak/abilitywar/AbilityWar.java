package daybreak.abilitywar;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.plugin.java.JavaPlugin;

import daybreak.abilitywar.addon.AddonLoader;
import daybreak.abilitywar.config.AbilitySettings;
import daybreak.abilitywar.config.AbilityWarSettings;
import daybreak.abilitywar.game.MainCommand;
import daybreak.abilitywar.game.manager.AbilityList;
import daybreak.abilitywar.game.script.Script;
import daybreak.abilitywar.game.script.Script.RequiredData;
import daybreak.abilitywar.game.script.types.ChangeAbilityScript;
import daybreak.abilitywar.game.script.types.LocationNoticeScript;
import daybreak.abilitywar.game.script.types.TeleportScript;
import daybreak.abilitywar.game.script.types.ChangeAbilityScript.ChangeTarget;
import daybreak.abilitywar.utils.Messager;
import daybreak.abilitywar.utils.autoupdate.AutoUpdate;
import daybreak.abilitywar.utils.autoupdate.AutoUpdate.Branch;
import daybreak.abilitywar.utils.thread.AbilityWarThread;
import daybreak.abilitywar.utils.versioncompat.ServerVersion;

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
