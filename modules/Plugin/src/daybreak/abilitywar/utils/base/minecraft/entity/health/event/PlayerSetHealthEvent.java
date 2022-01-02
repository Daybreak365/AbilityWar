package daybreak.abilitywar.utils.base.minecraft.entity.health.event;

import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;
import org.jetbrains.annotations.NotNull;

public class PlayerSetHealthEvent extends PlayerEvent implements Cancellable {

	private static final HandlerList handlers = new HandlerList();

	public static HandlerList getHandlerList() {
		return handlers;
	}

	@Override
	public @NotNull HandlerList getHandlers() {
		return handlers;
	}

	private double health;

	public PlayerSetHealthEvent(@NotNull final Player who, final double health) {
		super(who);
		this.health = Math.max(Math.min(health, player.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue()), 0.0);
	}

	public void setHealth(double health) {
		this.health = Math.max(Math.min(health, player.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue()), 0.0);
	}

	public double getHealth() {
		return health;
	}

	private boolean cancelled = false;

	@Override
	public boolean isCancelled() {
		return cancelled;
	}

	@Override
	public void setCancelled(boolean cancelled) {
		this.cancelled = cancelled;
	}
}
