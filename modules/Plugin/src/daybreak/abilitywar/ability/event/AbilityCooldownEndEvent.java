package daybreak.abilitywar.ability.event;

import daybreak.abilitywar.ability.AbilityBase;
import daybreak.abilitywar.ability.AbilityBase.Cooldown;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class AbilityCooldownEndEvent extends AbilityEvent {

	private static final HandlerList handlers = new HandlerList();

	public static HandlerList getHandlerList() {
		return handlers;
	}

	@Override
	public @NotNull HandlerList getHandlers() {
		return handlers;
	}

	private final Cooldown cooldown;

	public AbilityCooldownEndEvent(AbilityBase abilityBase, Cooldown cooldown) {
		super(abilityBase);
		this.cooldown = cooldown;
	}

	public Cooldown getCooldown() {
		return cooldown;
	}
}
