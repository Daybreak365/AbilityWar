package daybreak.abilitywar.ability.event;

import daybreak.abilitywar.ability.AbilityBase;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class AbilityCooldownResetEvent extends AbilityEvent {

	private static final HandlerList handlers = new HandlerList();

	public static HandlerList getHandlerList() {
		return handlers;
	}

	@Override
	public @NotNull HandlerList getHandlers() {
		return handlers;
	}

	public AbilityCooldownResetEvent(AbilityBase ability) {
		super(ability);
	}
}
