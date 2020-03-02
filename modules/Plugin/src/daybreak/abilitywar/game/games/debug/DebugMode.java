package daybreak.abilitywar.game.games.debug;

import daybreak.abilitywar.AbilityWar;
import daybreak.abilitywar.game.games.mode.AbstractGame;
import daybreak.abilitywar.game.games.mode.GameManifest;
import daybreak.abilitywar.game.games.standard.Game;
import daybreak.abilitywar.game.manager.object.DefaultKitHandler;
import daybreak.abilitywar.utils.base.minecraft.PlayerCollector;
import org.bukkit.Bukkit;
import org.bukkit.event.HandlerList;

@GameManifest(Name = "Debug Mode", Description = {})
public class DebugMode extends Game implements DefaultKitHandler, AbstractGame.Observer {

	public DebugMode() {
		super(PlayerCollector.EVERY_PLAYER_EXCLUDING_SPECTATORS());
		attachObserver(this);
		Bukkit.getPluginManager().registerEvents(this, AbilityWar.getPlugin());
	}

	@Override
	protected void progressGame(int i) {
		if (i == 1) {
			setRestricted(false);
			startGame();
		}
	}

	@Override
	public void update(GAME_UPDATE update) {
		if (update == GAME_UPDATE.END) {
			HandlerList.unregisterAll(this);
		}
	}

}
