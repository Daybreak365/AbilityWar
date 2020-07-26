package daybreak.abilitywar.game.manager.object;

import daybreak.abilitywar.AbilityWar;
import daybreak.abilitywar.game.AbstractGame.GameUpdate;
import daybreak.abilitywar.game.AbstractGame.Observer;
import daybreak.abilitywar.game.Game;
import java.util.LinkedList;
import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

public class ScoreboardManager implements Listener, Observer {

	private final Scoreboard scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();
	private final List<Team> teams = new LinkedList<>();

	public ScoreboardManager(Game game) {
		Bukkit.getPluginManager().registerEvents(this, AbilityWar.getPlugin());
		game.attachObserver(this);
	}

	public Scoreboard getScoreboard() {
		return scoreboard;
	}

	public Team registerNewTeam(String name) {
		Team team = scoreboard.registerNewTeam(name);
		teams.add(team);
		return team;
	}

	public void unregisterTeam(Team team) {
		teams.remove(team);
		team.unregister();
	}

	@Override
	public void update(GameUpdate update) {
		if (update == GameUpdate.START) {
			for (Player player : Bukkit.getOnlinePlayers()) {
				player.setScoreboard(scoreboard);
			}
		} else if (update == GameUpdate.END) {
			HandlerList.unregisterAll(this);
			for (Player player : Bukkit.getOnlinePlayers()) {
				player.setScoreboard(Bukkit.getScoreboardManager().getMainScoreboard());
			}
			for (Team team : teams) {
				team.unregister();
			}
		}
	}

	@EventHandler
	private void onJoin(PlayerJoinEvent e) {
		e.getPlayer().setScoreboard(scoreboard);
	}

	@EventHandler
	private void onQuit(PlayerQuitEvent e) {
		final Player player = e.getPlayer();
		try {
			if (player.isValid() && player.isOnline()) {
				player.setScoreboard(Bukkit.getScoreboardManager().getMainScoreboard());
			}
		} catch (IllegalStateException ignored) {
		}
	}

	public interface Handler {
		ScoreboardManager getScoreboardManager();
	}

}
