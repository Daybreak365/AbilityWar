package daybreak.abilitywar.ability.event;

import com.google.common.base.Preconditions;
import daybreak.abilitywar.ability.AbilityBase;
import org.bukkit.Material;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class AbilityPreTargetEvent extends AbilityEvent implements Cancellable {

	private static final HandlerList handlers = new HandlerList();
	private boolean cancelled = false;
	private @NotNull Material material;
	private @NotNull LivingEntity target;

	public AbilityPreTargetEvent(@NotNull AbilityBase ability, @NotNull Material material, @NotNull LivingEntity target) {
		super(ability);
		this.material = material;
		this.target = target;
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

	public @NotNull Material getMaterial() {
		return material;
	}

	public void setMaterial(@NotNull Material material) {
		this.material = Preconditions.checkNotNull(material);
	}

	public @NotNull LivingEntity getTarget() {
		return target;
	}

	public void setTarget(@NotNull LivingEntity target) {
		this.target = Preconditions.checkNotNull(target);
	}
}
