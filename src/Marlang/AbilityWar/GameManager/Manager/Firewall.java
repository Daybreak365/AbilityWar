package Marlang.AbilityWar.GameManager.Manager;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerLoginEvent.Result;

import Marlang.AbilityWar.Config.AbilityWarSettings;
import Marlang.AbilityWar.GameManager.Game;
import Marlang.AbilityWar.GameManager.Module.Module;
import Marlang.AbilityWar.Utils.AbilityWarThread;

/**
 * 방화벽
 */
public class Firewall extends Module implements Listener {
	
	public Firewall() {
		RegisterListener(this);
	}
	
	@EventHandler
	public void onLogin(PlayerLoginEvent e) {
		if(AbilityWarThread.isGameTaskRunning()) {
			
			boolean canLogin = false;
			
			if(AbilityWarSettings.getFirewall()) {
				Player p = e.getPlayer();
				
				if(p.isOp()) {
					canLogin = true;
				}
				
				for(Player player : AbilityWarThread.getGame().getPlayers()) {
					if(player.getName().equals(p.getName())) {
						canLogin = true;
					}
				}
				
				for(String playerName : Game.getSpectators()) {
					if(p.getName().equals(playerName)) {
						canLogin = true;
					}
				}
				
				if(!canLogin) {
					e.disallow(Result.KICK_OTHER,
							ChatColor.translateAlternateColorCodes('&', "&2《&aAbilityWar&2》")
							+ "\n"
							+ ChatColor.translateAlternateColorCodes('&', "&f게임 진행중이므로 접속할 수 없습니다."));
				}
			}
			
			if(AbilityWarSettings.getEliminate()) {
				Player p = e.getPlayer();
				
				if(AbilityWarThread.getGame().getDeathManager().isEliminated(p) && !p.isOp()) {
					e.disallow(Result.KICK_OTHER,
							ChatColor.translateAlternateColorCodes('&', "&2《&aAbilityWar&2》")
							+ "\n"
							+ ChatColor.translateAlternateColorCodes('&', "&f탈락하셨습니다."));
				}
			}
		}
	}
	
}
