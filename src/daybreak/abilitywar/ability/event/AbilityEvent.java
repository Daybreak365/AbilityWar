package daybreak.abilitywar.ability.event;

import daybreak.abilitywar.ability.AbilityBase;
import daybreak.abilitywar.game.events.participant.ParticipantEvent;
import org.bukkit.event.HandlerList;

public class AbilityEvent extends ParticipantEvent {

	private final AbilityBase ability;

	protected AbilityEvent(AbilityBase ability) {
		super(ability.getParticipant());
		this.ability = ability;
	}

	public AbilityBase getAbility() {
		return ability;
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
