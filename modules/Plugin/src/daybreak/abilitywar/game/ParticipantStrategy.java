package daybreak.abilitywar.game;

import daybreak.abilitywar.game.AbstractGame.Participant;
import org.bukkit.entity.Player;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public interface ParticipantStrategy {

	Collection<? extends Participant> getParticipants();

	boolean isParticipating(UUID uuid);

	AbstractGame.Participant getParticipant(UUID uuid);

	void addParticipant(Player player) throws UnsupportedOperationException;

	void removeParticipant(UUID uuid) throws UnsupportedOperationException;

	class DefaultManagement implements ParticipantStrategy {

		private final Game game;
		private final Map<UUID, Participant> participants = new HashMap<>();

		public DefaultManagement(Game game, Collection<Player> players) {
			this.game = game;
			for (Player player : players) {
				participants.put(player.getUniqueId(), game.new ParticipantImpl(player));
			}
		}

		@Override
		public Collection<? extends Participant> getParticipants() {
			return Collections.unmodifiableCollection(participants.values());
		}

		@Override
		public boolean isParticipating(UUID uuid) {
			return participants.containsKey(uuid);
		}

		@Override
		public Participant getParticipant(UUID uuid) {
			return participants.get(uuid);
		}

		@Override
		public void addParticipant(Player player) throws UnsupportedOperationException {
			participants.putIfAbsent(player.getUniqueId(), game.new ParticipantImpl(player));
		}

		@Override
		public void removeParticipant(UUID uuid) throws UnsupportedOperationException {
			participants.remove(uuid);
		}

	}

}
