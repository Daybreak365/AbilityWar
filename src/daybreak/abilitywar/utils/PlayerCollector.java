package daybreak.abilitywar.utils;

import daybreak.abilitywar.game.manager.SpectatorManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collection;

public class PlayerCollector {

	private PlayerCollector() {
	}

	public static Collection<Player> EVERY_PLAYER_EXCLUDING_SPECTATORS() {
		ArrayList<Player> players = new ArrayList<>();
		for (Player player : Bukkit.getOnlinePlayers()) {
			if (!SpectatorManager.isSpectator(player.getName())) {
				players.add(player);
			}
		}
		return players;
	}

}
