package DayBreak.AbilityWar.Development.PlaceholderAPI;

import org.bukkit.entity.Player;

import DayBreak.AbilityWar.Game.Games.AbstractGame.Participant;
import DayBreak.AbilityWar.Utils.Thread.AbilityWarThread;

public enum Placeholders {

	/**
	 * 게임 진행 여부
	 */
	GameCondition("gamecondition", false) {
		@Override
		public String onRequest(Player p) {
			return AbilityWarThread.isGameTaskRunning() ? "true" : "false";
		}
	},
	AbilityName("abilityname", true) {
		@Override
		protected String onRequest(Player p) {
			if(AbilityWarThread.isGameTaskRunning()) {
				if(AbilityWarThread.getGame().isParticipating(p)) {
					Participant participant = AbilityWarThread.getGame().getParticipant(p);
					if(participant.hasAbility()) {
						return participant.getAbility().getName();
					}
				}
			}
			
			return "능력 없음";
		}
	};
	
	private final String identifier;
	private final boolean needPlayer;
	
	private Placeholders(String identifier, boolean needPlayer) {
		this.identifier = identifier;
		this.needPlayer = needPlayer;
	}

	public String getIdentifier() {
		return identifier;
	}
	
	public String Request(Player p) {
		if(!needPlayer || (needPlayer && p != null)) {
			return onRequest(p);
		} else {
			return null;
		}
	}
	
	abstract protected String onRequest(Player p);
	
}
