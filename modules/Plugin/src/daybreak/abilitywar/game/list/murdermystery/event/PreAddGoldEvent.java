package daybreak.abilitywar.game.list.murdermystery.event;

import com.google.common.base.Preconditions;
import daybreak.abilitywar.game.AbstractGame;
import daybreak.abilitywar.game.event.participant.ParticipantEvent;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class PreAddGoldEvent extends ParticipantEvent implements Cancellable {

    private static final HandlerList handlers = new HandlerList();

    public static HandlerList getHandlerList() {
        return handlers;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return handlers;
    }

    private boolean cancelled = false;

    private int amount;

    public PreAddGoldEvent(AbstractGame.Participant participant, int amount) {
        super(participant);
        this.amount = amount;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }

    public int getAmount() {
        return amount;
    }

    public void setAmount(int amount) {
        Preconditions.checkArgument(amount > 0 && amount <= 64, "amount must be a number between 1 and 64");
        this.amount = amount;
    }
}
