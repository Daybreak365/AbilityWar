package DayBreak.AbilityWar.Game.Events;

import static DayBreak.AbilityWar.Utils.Validate.notNull;

import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;

import DayBreak.AbilityWar.Game.Games.Mode.AbstractGame.Participant;

public class ParticipantDeathEvent extends PlayerEvent {

	public ParticipantDeathEvent(Participant p) {
		super(notNull(p).getPlayer());
		this.participant = p;
	}

	private final Participant participant;

	public Participant getParticipant() {
		return participant;
	}

	private static final HandlerList handlers = new HandlerList();

	@Override
	public HandlerList getHandlers() {
		return handlers;
	}

	public static HandlerList getHandlerList() {
		return handlers;
	}

}
