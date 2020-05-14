package daybreak.abilitywar.ability.event;

import daybreak.abilitywar.ability.AbilityBase;
import org.bukkit.event.HandlerList;

public class PreAbilityRestrictionEvent extends AbilityEvent {

	private static final HandlerList handlers = new HandlerList();
	private boolean restricted;

	public PreAbilityRestrictionEvent(AbilityBase ability, boolean restricted) {
		super(ability);
		this.restricted = restricted;
	}

	public static HandlerList getHandlerList() {
		return handlers;
	}

	@Override
	public HandlerList getHandlers() {
		return handlers;
	}

	public boolean isRestricted() {
		return restricted;
	}

	public void setRestricted(boolean restricted) {
		this.restricted = restricted;
	}

}
