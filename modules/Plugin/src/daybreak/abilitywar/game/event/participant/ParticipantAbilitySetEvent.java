package daybreak.abilitywar.game.event.participant;

import daybreak.abilitywar.ability.AbilityBase;
import daybreak.abilitywar.game.AbstractGame.Participant;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class ParticipantAbilitySetEvent extends ParticipantEvent {

	private static final HandlerList handlers = new HandlerList();

	@Override
	public @NotNull HandlerList getHandlers() {
		return handlers;
	}

	public static HandlerList getHandlerList() {
		return handlers;
	}

	private final AbilityBase oldAbility, newAbility;

	public ParticipantAbilitySetEvent(Participant participant, AbilityBase oldAbility, AbilityBase newAbility) {
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
