package daybreak.abilitywar.ability.event;

import daybreak.abilitywar.ability.AbilityBase;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.EquipmentSlot;
import org.jetbrains.annotations.NotNull;

class AbilityPreTargetEvent extends AbilityEvent implements Cancellable {

	private static final HandlerList handlers = new HandlerList();
	private final @NotNull EquipmentSlot hand;
	private boolean cancelled = false;

	public AbilityPreTargetEvent(@NotNull AbilityBase ability, @NotNull EquipmentSlot hand) {
		super(ability);
		this.hand = hand;
	}

	public static HandlerList getHandlerList() {
		return handlers;
	}

	@Override
	public @NotNull HandlerList getHandlers() {
		return handlers;
	}

	@Override
	public boolean isCancelled() {
		return cancelled;
	}

	@Override
	public void setCancelled(boolean cancelled) {
		this.cancelled = cancelled;
	}

	public @NotNull EquipmentSlot getHand() {
		return hand;
	}
}
