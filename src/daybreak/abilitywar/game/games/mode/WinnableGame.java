package daybreak.abilitywar.game.games.mode;

import daybreak.abilitywar.utils.thread.AbilityWarThread;

public abstract class WinnableGame extends AbstractGame {

	public void Victory(Participant... participants) {
		onVictory(participants);
		
		AbilityWarThread.StopGame();
	}
	
	abstract protected void onVictory(Participant... participants);
	
}
