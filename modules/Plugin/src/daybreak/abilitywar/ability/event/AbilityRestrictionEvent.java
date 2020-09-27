package daybreak.abilitywar.ability.event;

import daybreak.abilitywar.ability.AbilityBase;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class AbilityRestrictionEvent extends AbilityEvent {

	private static final HandlerList handlers = new HandlerList();

	public static HandlerList getHandlerList() {
		return handlers;
	}

	@Override
	public @NotNull HandlerList getHandlers() {
		return handlers;
	}

	private final boolean newState;

	public AbilityRestrictionEvent(AbilityBase ability, final boolean newState) {
		super(ability);
		this.newState = newState;
	}

	public boolean getNewState() {
		return newState;
	}

}
