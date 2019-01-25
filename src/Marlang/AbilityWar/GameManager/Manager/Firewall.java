package Marlang.AbilityWar.GameManager.Manager;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerLoginEvent.Result;

import Marlang.AbilityWar.AbilityWar;
import Marlang.AbilityWar.API.Events.AbilityWarProgressEvent;
import Marlang.AbilityWar.API.Events.AbilityWarProgressEvent.Progress;
import Marlang.AbilityWar.Config.AbilityWarSettings;
import Marlang.AbilityWar.GameManager.Game;

/**
 * 방화벽
 */
public class Firewall implements Listener {
	
	Game game;
	
	public Firewall(Game game) {
		this.game = game;
		Bukkit.getPluginManager().registerEvents(this, AbilityWar.getPlugin());
	}
	
	/**
	 * 게임 종료시 Listener Unregister
	 */
	@EventHandler
	public void onGameProcess(AbilityWarProgressEvent e) {
		if(e.getProgress().equals(Progress.Game_ENDED)) {
			HandlerList.unregisterAll(this);
		}
	}
	
	@EventHandler
	public void onLogin(PlayerLoginEvent e) {
		boolean canLogin = false;
		
		if(AbilityWarSettings.getFirewall()) {
			Player p = e.getPlayer();
			
			if(p.isOp()) {
				canLogin = true;
			}
			
			for(Player player : game.getPlayers()) {
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
			
			if(game.getDeathManager().isEliminated(p) && !p.isOp()) {
				e.disallow(Result.KICK_OTHER,
						ChatColor.translateAlternateColorCodes('&', "&2《&aAbilityWar&2》")
						+ "\n"
						+ ChatColor.translateAlternateColorCodes('&', "&f탈락하셨습니다."));
			}
		}
	}
	
}
