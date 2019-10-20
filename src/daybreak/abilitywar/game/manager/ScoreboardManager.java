package daybreak.abilitywar.game.manager;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventException;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.EventExecutor;
import org.bukkit.scoreboard.Scoreboard;

import daybreak.abilitywar.AbilityWar;
import daybreak.abilitywar.game.games.defaultgame.Game;

public class ScoreboardManager implements EventExecutor {

	private final Scoreboard scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();
	
	public Scoreboard getScoreboard() {
		return scoreboard;
	}
	
	private final List<Player> viewers = new ArrayList<>();
	
	public void Initialize() {
		for(Player p : Bukkit.getOnlinePlayers()) {
			if(!viewers.contains(p)) {
				p.setScoreboard(scoreboard);
				viewers.add(p);
			}
		}
	}
	
	public void Clear() {
		for(Player p : viewers) {
			p.setScoreboard(Bukkit.getScoreboardManager().getMainScoreboard());
		}
	}
	
	public ScoreboardManager(Game game) {
		Bukkit.getPluginManager().registerEvent(PlayerJoinEvent.class, game, EventPriority.HIGH, this, AbilityWar.getPlugin());
	}

	@Override
	public void execute(Listener listener, Event event) throws EventException {
		if(event instanceof PlayerJoinEvent) {
			PlayerJoinEvent e = (PlayerJoinEvent) event;
			Player p = e.getPlayer();
			if(!viewers.contains(p)) {
				p.setScoreboard(scoreboard);
				viewers.add(p);
			}
		}
	}

}
