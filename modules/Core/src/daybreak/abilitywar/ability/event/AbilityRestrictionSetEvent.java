package daybreak.abilitywar.ability.event;

import daybreak.abilitywar.ability.AbilityBase;
import org.bukkit.event.HandlerList;

public class AbilityRestrictionSetEvent extends AbilityEvent {

	private static final HandlerList handlers = new HandlerList();

	@Override
	public HandlerList getHandlers() {
		return handlers;
	}

	public static HandlerList getHandlerList() {
		return handlers;
	}

	public AbilityRestrictionSetEvent(AbilityBase ability) {
		super(ability);
	}

}
