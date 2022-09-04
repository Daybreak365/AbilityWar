package daybreak.abilitywar.game.manager.effect.event;

import daybreak.abilitywar.game.AbstractGame;
import daybreak.abilitywar.game.AbstractGame.Effect;
import daybreak.abilitywar.game.event.participant.ParticipantEvent;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class ParticipantNewEffectApplyEvent extends ParticipantEvent {

    private static final HandlerList handlers = new HandlerList();

    @NotNull
    private final AbstractGame.Effect effect;

    public ParticipantNewEffectApplyEvent(@NotNull AbstractGame.Participant who, final @NotNull AbstractGame.Effect effect) {
        super(who);
        this.effect = effect;
    }

    @NotNull
    public Effect getEffect() {
        return effect;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return handlers;
    }

}
