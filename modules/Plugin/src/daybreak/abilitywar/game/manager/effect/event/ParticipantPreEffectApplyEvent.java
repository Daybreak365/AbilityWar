package daybreak.abilitywar.game.manager.effect.event;

import daybreak.abilitywar.game.AbstractGame;
import daybreak.abilitywar.game.event.participant.ParticipantEvent;
import daybreak.abilitywar.game.manager.effect.registry.EffectRegistry;
import daybreak.abilitywar.game.manager.effect.registry.EffectRegistry.EffectRegistration;
import daybreak.abilitywar.utils.base.concurrent.TimeUnit;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class ParticipantPreEffectApplyEvent extends ParticipantEvent implements Cancellable {

	private static final HandlerList handlers = new HandlerList();

	private final EffectRegistry.EffectRegistration<?> effectType;
	private boolean cancelled = false;
	private int duration;

	public ParticipantPreEffectApplyEvent(@NotNull AbstractGame.Participant who, final @NotNull EffectRegistry.EffectRegistration<?> effectType, final @NotNull TimeUnit timeUnit, final int duration) {
		super(who);
		this.effectType = effectType;
		this.duration = timeUnit.toTicks(duration);
	}

	public static HandlerList getHandlerList() {
		return handlers;
	}

	@Override
	public @NotNull HandlerList getHandlers() {
		return handlers;
	}

	@NotNull
	public EffectRegistration<?> getEffectType() {
		return effectType;
	}

	@Override
	public boolean isCancelled() {
		return cancelled;
	}

	@Override
	public void setCancelled(boolean cancelled) {
		this.cancelled = cancelled;
	}

	public int getDuration() {
		return duration;
	}

	public void setDuration(final @NotNull TimeUnit timeUnit, int duration) {
		this.duration = timeUnit.toTicks(duration);
	}

}
