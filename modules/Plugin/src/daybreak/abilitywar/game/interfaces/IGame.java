package daybreak.abilitywar.game.interfaces;

import java.util.Collection;
import java.util.UUID;
import org.bukkit.entity.Player;

public interface IGame {

	boolean start();
	boolean stop();

	boolean isGameStarted();

	boolean isRestricted();
	void setRestricted(boolean restricted);

	Collection<? extends Participable> getParticipants();
	Participable getParticipant(Player player);
	Participable getParticipant(UUID uniqueId);
	boolean isParticipating(Player player);
	boolean isParticipating(UUID uniqueId);
	void addParticipant(Player player) throws UnsupportedOperationException;
	void removeParticipant(UUID uuid) throws UnsupportedOperationException;

}
