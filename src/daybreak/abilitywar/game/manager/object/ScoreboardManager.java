package daybreak.abilitywar.game.manager.object;

import daybreak.abilitywar.AbilityWar;
import daybreak.abilitywar.game.games.mode.AbstractGame;
import daybreak.abilitywar.game.games.standard.Game;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.EventExecutor;
import org.bukkit.scoreboard.Scoreboard;

import java.util.HashSet;
import java.util.Set;

public class ScoreboardManager implements EventExecutor, AbstractGame.Observer {

	public ScoreboardManager(Game game) {
		Bukkit.getPluginManager().registerEvent(PlayerJoinEvent.class, game, EventPriority.HIGH, this, AbilityWar.getPlugin());
		game.attachObserver(this);
	}

	private final Scoreboard scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();

	public Scoreboard getScoreboard() {
		return scoreboard;
	}

	private final Set<Player> viewers = new HashSet<>();

	@Override
	public void update(AbstractGame.GAME_UPDATE update) {
		if (update == AbstractGame.GAME_UPDATE.START) {
			for (Player player : Bukkit.getOnlinePlayers()) {
				if (viewers.add(player)) {
					player.setScoreboard(scoreboard);
				}
			}
		} else if (update == AbstractGame.GAME_UPDATE.END) {
			for (Player viewer : viewers) {
				viewer.setScoreboard(Bukkit.getScoreboardManager().getMainScoreboard());
			}
		}
	}

	@Override
	public void execute(Listener listener, Event event) {
		if (event instanceof PlayerJoinEvent) {
			PlayerJoinEvent e = (PlayerJoinEvent) event;
			Player player = e.getPlayer();
			if (viewers.add(player)) {
				player.setScoreboard(scoreboard);
			}
		}
	}

	public interface Handler {
		ScoreboardManager getScoreboardManager();
	}

}
