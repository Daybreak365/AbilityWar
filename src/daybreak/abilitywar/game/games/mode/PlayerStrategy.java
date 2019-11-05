package daybreak.abilitywar.game.games.mode;

import daybreak.abilitywar.game.manager.SpectatorManager;
import java.util.ArrayList;
import java.util.Collection;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public interface PlayerStrategy {

	PlayerStrategy EVERY_PLAYER_EXCLUDING_SPECTATORS = new PlayerStrategy() {
		@Override
		public Collection<Player> getPlayers() {
			ArrayList<Player> players = new ArrayList<>();
			for(Player player : Bukkit.getOnlinePlayers()) {
				if(!SpectatorManager.isSpectator(player.getName())) {
					players.add(player);
				}
			}
			return players;
		}
	};

	Collection<Player> getPlayers();

}
