package daybreak.abilitywar.game.manager.object;

import daybreak.abilitywar.AbilityWar;
import daybreak.abilitywar.game.AbstractGame.GameUpdate;
import daybreak.abilitywar.game.AbstractGame.Observer;
import daybreak.abilitywar.game.Game;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
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
	private final Set<UUID> viewers = new HashSet<>();

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

	@Override
	public void update(GameUpdate update) {
		if (update == GameUpdate.START) {
			for (Player player : Bukkit.getOnlinePlayers()) {
				if (viewers.add(player.getUniqueId())) {
					player.setScoreboard(scoreboard);
				}
			}
		} else if (update == GameUpdate.END) {
			HandlerList.unregisterAll(this);
			for (UUID uuid : viewers) {
				Player viewer = Bukkit.getPlayer(uuid);
				if (viewer != null) {
					viewer.setScoreboard(Bukkit.getScoreboardManager().getMainScoreboard());
				}
			}
			for (Team team : teams) {
				team.unregister();
			}
		}
	}

	@EventHandler
	private void onJoin(PlayerJoinEvent e) {
		Player player = e.getPlayer();
		if (viewers.add(player.getUniqueId())) {
			player.setScoreboard(scoreboard);
		}
	}

	@EventHandler
	private void onQuit(PlayerQuitEvent e) {
		Player player = e.getPlayer();
		viewers.remove(player.getUniqueId());
		if (player.isValid()) {
			player.setScoreboard(Bukkit.getScoreboardManager().getMainScoreboard());
		}
	}

	public interface Handler {
		ScoreboardManager getScoreboardManager();
	}

}
