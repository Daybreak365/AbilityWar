package daybreak.abilitywar.ability.event;

import daybreak.abilitywar.ability.AbilityBase;
import daybreak.abilitywar.game.AbstractGame.Participant;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;

public abstract class AbilityEvent extends Event {

	private final AbilityBase ability;

	protected AbilityEvent(final AbilityBase ability) {
		this.ability = ability;
	}

	public AbilityBase getAbility() {
		return ability;
	}

	public Participant getParticipant() {
		return ability.getParticipant();
	}

	public Player getPlayer() {
		return ability.getPlayer();
	}

}
