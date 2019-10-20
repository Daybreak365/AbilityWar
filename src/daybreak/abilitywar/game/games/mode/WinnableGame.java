package daybreak.abilitywar.game.games.mode;

import daybreak.abilitywar.game.games.defaultgame.Game;
import daybreak.abilitywar.utils.thread.AbilityWarThread;

public abstract class WinnableGame extends Game {

	public WinnableGame(PlayerStrategy strategy) {
		super(strategy);
	}

	public void Victory(Participant... participants) {
		onVictory(participants);

		AbilityWarThread.StopGame();
	}

	abstract protected void onVictory(Participant... participants);

}
