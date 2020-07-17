package daybreak.abilitywar.utils.base.minecraft;

import daybreak.abilitywar.game.manager.SpectatorManager;
import java.util.ArrayList;
import java.util.Collection;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class PlayerCollector {

	private PlayerCollector() {
	}

	public static Collection<Player> EVERY_PLAYER_EXCLUDING_SPECTATORS() {
		final Collection<Player> players = new ArrayList<>();
		for (Player player : Bukkit.getOnlinePlayers()) {
			if (SpectatorManager.isSpectator(player.getName())) continue;
			players.add(player);
		}
		return players;
	}

}
