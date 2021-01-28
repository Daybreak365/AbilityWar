package daybreak.abilitywar.game.event.customentity;

import daybreak.abilitywar.game.AbstractGame.CustomEntity;
import org.bukkit.event.Event;

public abstract class CustomEntityEvent extends Event {

	public CustomEntityEvent(CustomEntity customEntity) {
		this.customEntity = customEntity;
	}

	private final CustomEntity customEntity;

	public CustomEntity getCustomEntity() {
		return customEntity;
	}

}
