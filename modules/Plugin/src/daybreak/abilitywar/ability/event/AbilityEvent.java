package daybreak.abilitywar.ability.event;

import daybreak.abilitywar.ability.AbilityBase;
import daybreak.abilitywar.game.event.participant.ParticipantEvent;

public abstract class AbilityEvent extends ParticipantEvent {

	private final AbilityBase ability;

	protected AbilityEvent(AbilityBase ability) {
		super(ability.getParticipant());
		this.ability = ability;
	}

	public AbilityBase getAbility() {
		return ability;
	}

}
