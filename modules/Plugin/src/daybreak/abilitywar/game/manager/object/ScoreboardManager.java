package daybreak.abilitywar.game.manager.object;

import daybreak.abilitywar.AbilityWar;
import daybreak.abilitywar.game.games.mode.AbstractGame;
import daybreak.abilitywar.game.games.mode.AbstractGame.GAME_UPDATE;
import daybreak.abilitywar.game.games.mode.AbstractGame.Observer;
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
import java.util.UUID;

public class ScoreboardManager implements EventExecutor, Observer {

	private final Scoreboard scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();
	private final Set<UUID> viewers = new HashSet<>();

	public ScoreboardManager(Game game) {
		Bukkit.getPluginManager().registerEvent(PlayerJoinEvent.class, game, EventPriority.HIGH, this, AbilityWar.getPlugin());
		game.attachObserver(this);
	}

	public Scoreboard getScoreboard() {
		return scoreboard;
	}

	@Override
	public void update(GAME_UPDATE update) {
		if (update == AbstractGame.GAME_UPDATE.START) {
			for (Player player : Bukkit.getOnlinePlayers()) {
				if (viewers.add(player.getUniqueId())) {
					player.setScoreboard(scoreboard);
				}
			}
		} else if (update == AbstractGame.GAME_UPDATE.END) {
			for (UUID uuid : viewers) {
				Player viewer = Bukkit.getPlayer(uuid);
				if (viewer != null) {
					viewer.setScoreboard(Bukkit.getScoreboardManager().getMainScoreboard());
				}
			}
		}
	}

	@Override
	public void execute(Listener listener, Event event) {
		if (event instanceof PlayerJoinEvent) {
			Player player = ((PlayerJoinEvent) event).getPlayer();
			if (viewers.add(player.getUniqueId())) {
				player.setScoreboard(scoreboard);
			}
		}
	}

	public interface Handler {
		ScoreboardManager getScoreboardManager();
	}

}
