package daybreak.abilitywar.game.manager.object;

import daybreak.abilitywar.AbilityWar;
import daybreak.abilitywar.game.AbstractGame.GameUpdate;
import daybreak.abilitywar.game.AbstractGame.Observer;
import daybreak.abilitywar.game.Game;
import daybreak.abilitywar.utils.base.minecraft.version.NMSVersion;
import daybreak.abilitywar.utils.base.minecraft.version.ServerVersion;
import java.util.HashSet;
import java.util.Set;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

public class ScoreboardManager implements Listener, Observer {

	private static final DisplaySlot[] SLOTS_TO_COPY = new DisplaySlot[]{DisplaySlot.BELOW_NAME, DisplaySlot.PLAYER_LIST};
	private final Scoreboard scoreboard;
	private final Set<Team> teams = new HashSet<>();

	@SuppressWarnings("deprecation")
	public ScoreboardManager(Game game) {
		this.scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();
		Bukkit.getPluginManager().registerEvents(this, AbilityWar.getPlugin());
		game.attachObserver(this);
		final Scoreboard mainScoreboard = Bukkit.getScoreboardManager().getMainScoreboard();
		for (final DisplaySlot displaySlot : SLOTS_TO_COPY) {
			final Objective objective = mainScoreboard.getObjective(displaySlot);
			if (objective != null) {
				final Objective register = registerNewObjective(objective.getName(), objective.getCriteria(), objective.getDisplayName());
				if (ServerVersion.isAboveOrEqual(NMSVersion.v1_13_R2)) {
					register.setRenderType(objective.getRenderType());
				}
				register.setDisplaySlot(displaySlot);
			}
		}
	}

	public Scoreboard getScoreboard() {
		return scoreboard;
	}

	@SuppressWarnings("deprecation")
	public Objective registerNewObjective(final String name, final String criteria, final String displayName) {
		if (ServerVersion.isAboveOrEqual(NMSVersion.v1_13_R1)) {
			return scoreboard.registerNewObjective(name, criteria, displayName);
		} else {
			final Objective register = scoreboard.registerNewObjective(name, criteria);
			register.setDisplayName(displayName);
			return register;
		}
	}

	public Team registerNewTeam(String name) {
		final Team team = scoreboard.registerNewTeam(name);
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
