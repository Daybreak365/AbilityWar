package daybreak.abilitywar.utils.base.minecraft;

import daybreak.abilitywar.game.manager.SpectatorManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class PlayerCollector {

	private PlayerCollector() {
	}

	public static Collection<Player> EVERY_PLAYER_EXCLUDING_SPECTATORS() {
		final List<Player> players = new ArrayList<>(Math.max(1, Bukkit.getOnlinePlayers().size() - SpectatorManager.getSpectatorCount()));
		for (final Player player : Bukkit.getOnlinePlayers()) {
			if (SpectatorManager.isSpectator(player.getName())) continue;
			players.add(player);
		}
		return players;
	}

}
