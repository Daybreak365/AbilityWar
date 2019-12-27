package daybreak.abilitywar.game.events.participant;

import daybreak.abilitywar.ability.AbilityBase;
import daybreak.abilitywar.game.games.mode.AbstractGame;
import org.bukkit.event.HandlerList;

public class ParticipantAbilitySetEvent extends ParticipantEvent {

	private static final HandlerList handlers = new HandlerList();

	@Override
	public HandlerList getHandlers() {
		return handlers;
	}

	public static HandlerList getHandlerList() {
		return handlers;
	}

	private final AbilityBase oldAbility, newAbility;

	public ParticipantAbilitySetEvent(AbstractGame.Participant participant, AbilityBase oldAbility, AbilityBase newAbility) {
		super(participant);
		this.oldAbility = oldAbility;
		this.newAbility = newAbility;
	}

	public AbilityBase getOldAbility() {
		return oldAbility;
	}

	public AbilityBase getNewAbility() {
		return newAbility;
	}

}
