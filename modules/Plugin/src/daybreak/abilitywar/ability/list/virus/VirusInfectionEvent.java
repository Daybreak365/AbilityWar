package daybreak.abilitywar.ability.list.virus;

import daybreak.abilitywar.ability.event.AbilityEvent;
import daybreak.abilitywar.game.AbstractGame.Participant;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class VirusInfectionEvent extends AbilityEvent implements Cancellable {

    private static final HandlerList handlers = new HandlerList();
    private boolean cancelled = false;

    public static HandlerList getHandlerList() {
        return handlers;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return handlers;
    }

    private final Virus virus;
    private final Participant target;

    VirusInfectionEvent(Virus virus, Participant target) {
        super(virus);
        this.virus = virus;
        this.target = target;
    }

    @Override
    public Virus getAbility() {
        return virus;
    }

    public Participant getTarget() {
        return target;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }

}
