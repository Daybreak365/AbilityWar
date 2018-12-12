package Marlang.AbilityWar;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import Marlang.AbilityWar.GameManager.Game;
import Marlang.AbilityWar.GameManager.MainCommand;
import Marlang.AbilityWar.Utils.AbilityWarThread;
import Marlang.AbilityWar.Utils.Messager;

/**
 * Ability War 능력자 전쟁 플러그인
 * @author _Marlang 말랑
 */
public class AbilityWar extends JavaPlugin {
	
	@Override
	public void onEnable() {
		AbilityWarThread.Initialize(this);
		Messager.Initialize(this);
		Game.Initialize(this);
		
		Load();
		
		Messager.sendMessage("플러그인이 활성화되었습니다.");
	}
	
	public void Load() {
		Bukkit.getPluginCommand("AbilityWar").setExecutor(new MainCommand());
	}
	
	@Override
	public void onDisable() {
		Messager.sendMessage("플러그인이 비활성화되었습니다.");
	}
	
}
