package daybreak.abilitywar.ability.event;

import daybreak.abilitywar.ability.AbilityBase;
import daybreak.abilitywar.ability.AbilityBase.ClickType;
import org.bukkit.Material;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class AbilityPreActiveSkillEvent extends AbilityEvent implements Cancellable {

	private static final HandlerList handlers = new HandlerList();

	public static HandlerList getHandlerList() {
		return handlers;
	}

	@Override
	public @NotNull HandlerList getHandlers() {
		return handlers;
	}

	private boolean cancelled = false;
	private final Material material;
	private final ClickType clickType;

	public AbilityPreActiveSkillEvent(AbilityBase ability, Material material, ClickType clickType) {
		super(ability);
		this.material = material;
		this.clickType = clickType;
	}

	@Override
	public boolean isCancelled() {
		return cancelled;
	}

	@Override
	public void setCancelled(boolean cancelled) {
		this.cancelled = cancelled;
	}

	public Material getMaterial() {
		return material;
	}

	public ClickType getClickType() {
		return clickType;
	}
}
