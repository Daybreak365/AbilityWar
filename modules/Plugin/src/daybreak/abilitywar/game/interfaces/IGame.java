package daybreak.abilitywar.game.interfaces;

import daybreak.abilitywar.game.AbstractGame.Participant;
import java.util.Collection;
import java.util.UUID;
import org.bukkit.entity.Player;

public interface IGame {

	boolean isRunning();
	boolean start();
	boolean stop();

	boolean isGameStarted();

	boolean isRestricted();
	void setRestricted(boolean restricted);

	Collection<? extends Participant> getParticipants();
	Participant getParticipant(Player player);
	Participant getParticipant(UUID uniqueId);
	boolean isParticipating(Player player);
	boolean isParticipating(UUID uniqueId);
	void addParticipant(Player player) throws UnsupportedOperationException;
	void removeParticipant(UUID uuid) throws UnsupportedOperationException;

}
