package DayBreak.AbilityWar.Game.Games.Mode;

import DayBreak.AbilityWar.Utils.Thread.AbilityWarThread;

public abstract class WinnableGame extends AbstractGame {

	public void Victory(Participant... participants) {
		onVictory(participants);
		
		AbilityWarThread.StopGame();
	}
	
	abstract protected void onVictory(Participant... participants);
	
}
