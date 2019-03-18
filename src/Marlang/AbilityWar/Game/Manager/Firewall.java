package Marlang.AbilityWar.Game.Manager;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventException;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerLoginEvent.Result;
import org.bukkit.plugin.EventExecutor;

import Marlang.AbilityWar.AbilityWar;
import Marlang.AbilityWar.Config.AbilityWarSettings;
import Marlang.AbilityWar.Game.Games.AbstractGame;
import Marlang.AbilityWar.Game.Games.AbstractGame.Participant;
import Marlang.AbilityWar.Game.Games.Game;

/**
 * 방화벽
 */
public class Firewall implements EventExecutor {
	
	private AbstractGame game;
	
	public Firewall(AbstractGame game) {
		this.game = game;
		Bukkit.getPluginManager().registerEvent(PlayerLoginEvent.class, game, EventPriority.HIGHEST, this, AbilityWar.getPlugin());
	}
	
	@Override
	public void execute(Listener listener, Event event) throws EventException {
		if(event instanceof PlayerLoginEvent) {
			PlayerLoginEvent e = (PlayerLoginEvent) event;

			boolean canLogin = false;
			
			Player p = e.getPlayer();
			
			if(AbilityWarSettings.getFirewall()) {
				
				if(p.isOp()) {
					canLogin = true;
				}
				
				for(Participant participant : game.getParticipants()) {
					if(participant.getPlayer().getName().equals(p.getName())) {
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
							+ System.lineSeparator()
							+ ChatColor.translateAlternateColorCodes('&', "&f게임 진행중이므로 접속할 수 없습니다."));
				}
			}
			
			if(AbilityWarSettings.getEliminate()) {
				if(game.getDeathManager().isEliminated(p) && !p.isOp()) {
					e.disallow(Result.KICK_OTHER,
							ChatColor.translateAlternateColorCodes('&', "&2《&aAbilityWar&2》")
							+ System.lineSeparator()
							+ ChatColor.translateAlternateColorCodes('&', "&f탈락하셨습니다."));
				}
			}
		}
	}
	
}
