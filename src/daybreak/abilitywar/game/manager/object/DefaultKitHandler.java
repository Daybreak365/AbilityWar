package daybreak.abilitywar.game.manager.object;

import daybreak.abilitywar.game.games.mode.AbstractGame.Participant;
import java.util.Collection;
import org.bukkit.entity.Player;

public interface DefaultKitHandler {

	void giveDefaultKit(Player p);

	default void giveDefaultKit(Collection<Participant> participants) {
		for(Participant p : participants) {
			giveDefaultKit(p.getPlayer());
		}
	}
	
}
