package daybreak.abilitywar.game;

import daybreak.abilitywar.game.AbstractGame.Participant;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.bukkit.entity.Player;

public interface ParticipantStrategy {

	Collection<? extends Participant> getParticipants();

	boolean isParticipating(UUID uuid);

	AbstractGame.Participant getParticipant(UUID uuid);

	void addParticipant(Player player) throws UnsupportedOperationException;

	void removeParticipant(UUID uuid) throws UnsupportedOperationException;

	class DefaultManagement implements ParticipantStrategy {

		private final Map<String, Participant> participants = new HashMap<>();

		public DefaultManagement(AbstractGame game, Collection<Player> players) {
			for (Player player : players) {
				participants.put(player.getUniqueId().toString(), game.new Participant(player));
			}
		}

		@Override
		public Collection<? extends Participant> getParticipants() {
			return participants.values();
		}

		@Override
		public boolean isParticipating(UUID uuid) {
			return participants.containsKey(uuid.toString());
		}

		@Override
		public Participant getParticipant(UUID uuid) {
			return participants.get(uuid.toString());
		}

		@Override
		public void addParticipant(Player player) throws UnsupportedOperationException {
			throw new UnsupportedOperationException("참가자를 추가할 수 없습니다.");
		}

		@Override
		public void removeParticipant(UUID uuid) throws UnsupportedOperationException {
			throw new UnsupportedOperationException("참가자를 제거할 수 없습니다.");
		}

	}

}
