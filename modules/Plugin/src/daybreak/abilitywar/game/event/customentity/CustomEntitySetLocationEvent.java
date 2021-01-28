package daybreak.abilitywar.game.event.customentity;

import daybreak.abilitywar.game.AbstractGame.CustomEntity;
import org.bukkit.Location;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class CustomEntitySetLocationEvent extends CustomEntityEvent {

	private static final HandlerList handlers = new HandlerList();

	public static HandlerList getHandlerList() {
		return handlers;
	}

	@Override
	public @NotNull HandlerList getHandlers() {
		return handlers;
	}

	private final Location from, to;

	public CustomEntitySetLocationEvent(CustomEntity customEntity, final Location to) {
		super(customEntity);
		this.from = customEntity.getLocation();
		this.to = to.clone();
	}

	public Location getFrom() {
		return from;
	}

	public Location getTo() {
		return to;
	}
}
