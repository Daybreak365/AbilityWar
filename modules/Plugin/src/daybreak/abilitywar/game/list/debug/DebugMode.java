package daybreak.abilitywar.game.list.debug;

import daybreak.abilitywar.AbilityWar;
import daybreak.abilitywar.game.AbstractGame;
import daybreak.abilitywar.game.Game;
import daybreak.abilitywar.game.GameManifest;
import daybreak.abilitywar.game.manager.object.DefaultKitHandler;
import daybreak.abilitywar.utils.base.minecraft.PlayerCollector;
import org.bukkit.Bukkit;
import org.bukkit.event.HandlerList;

@GameManifest(name = "Debug Mode", description = {})
public class DebugMode extends Game implements DefaultKitHandler, AbstractGame.Observer {

	public DebugMode() {
		super(PlayerCollector.EVERY_PLAYER_EXCLUDING_SPECTATORS());
		attachObserver(this);
		Bukkit.getPluginManager().registerEvents(this, AbilityWar.getPlugin());
	}

	@Override
	protected void progressGame(int seconds) {
		if (seconds == 1) {
			setRestricted(false);
			startGame();
		}
	}

	@Override
	public void update(GameUpdate update) {
		if (update == GameUpdate.END) {
			HandlerList.unregisterAll(this);
		}
	}

}
