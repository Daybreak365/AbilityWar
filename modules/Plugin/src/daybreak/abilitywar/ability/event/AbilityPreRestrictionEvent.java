package daybreak.abilitywar.ability.event;

import daybreak.abilitywar.ability.AbilityBase;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class AbilityPreRestrictionEvent extends AbilityEvent {

	private static final HandlerList handlers = new HandlerList();

	public static HandlerList getHandlerList() {
		return handlers;
	}

	@Override
	public @NotNull HandlerList getHandlers() {
		return handlers;
	}

	private boolean newState;

	public AbilityPreRestrictionEvent(AbilityBase ability, final boolean newState) {
		super(ability);
		this.newState = newState;
	}

	public boolean getNewState() {
		return newState;
	}

	public void setNewState(boolean newState) {
		this.newState = newState;
	}

}
