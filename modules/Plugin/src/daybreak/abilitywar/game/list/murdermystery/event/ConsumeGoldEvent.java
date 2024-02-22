package daybreak.abilitywar.game.list.murdermystery.event;

import daybreak.abilitywar.game.AbstractGame;
import daybreak.abilitywar.game.event.participant.ParticipantEvent;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class ConsumeGoldEvent extends ParticipantEvent {

    private static final HandlerList handlers = new HandlerList();

    public static HandlerList getHandlerList() {
        return handlers;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return handlers;
    }

    private final int amount;

    public ConsumeGoldEvent(AbstractGame.Participant participant, int amount) {
        super(participant);
        this.amount = amount;
    }

    public int getAmount() {
        return amount;
    }

}
