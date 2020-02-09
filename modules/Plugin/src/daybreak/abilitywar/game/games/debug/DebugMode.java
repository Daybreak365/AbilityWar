package daybreak.abilitywar.game.games.debug;

import daybreak.abilitywar.game.games.mode.GameManifest;
import daybreak.abilitywar.game.games.standard.Game;
import daybreak.abilitywar.game.manager.object.DefaultKitHandler;
import daybreak.abilitywar.utils.base.minecraft.PlayerCollector;

@GameManifest(Name = "Debug Mode", Description = {})
public class DebugMode extends Game implements DefaultKitHandler {

	public DebugMode() {
		super(PlayerCollector.EVERY_PLAYER_EXCLUDING_SPECTATORS());
	}

	@Override
	protected void progressGame(int i) {
		if (i == 1) {
			setRestricted(false);
			startGame();
		}
	}

}
