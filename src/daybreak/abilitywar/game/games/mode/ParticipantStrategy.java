package daybreak.abilitywar.game.games.mode;

import java.util.Collection;
import java.util.HashMap;
import java.util.UUID;
import org.bukkit.entity.Player;

public abstract class ParticipantStrategy {

    protected final AbstractGame.Participant buildParticipant(Player player) {
        UUID uuid = player.getUniqueId();
        if (isParticipating(uuid)) {
            return getParticipant(uuid);
        }
        return game.new Participant(player);
    }

    private final AbstractGame game;

    public ParticipantStrategy(AbstractGame game) {
        this.game = game;
    }

    public abstract Collection<AbstractGame.Participant> getParticipants();

    public abstract boolean isParticipating(UUID uuid);
    public abstract AbstractGame.Participant getParticipant(UUID uuid);

    public abstract void addParticipant(Player player) throws UnsupportedOperationException;
    public abstract void removeParticipant(UUID uuid) throws UnsupportedOperationException;

    public static class DEFAULT_MANAGEMENT extends ParticipantStrategy {

        private final HashMap<String, AbstractGame.Participant> participants = new HashMap<>();

        public DEFAULT_MANAGEMENT(AbstractGame game, Collection<Player> players) {
            super(game);
            for (Player player : players) {
                participants.put(player.getUniqueId().toString(), buildParticipant(player));
            }
        }

        @Override
        public Collection<AbstractGame.Participant> getParticipants() {
            return participants.values();
        }

        @Override
        public boolean isParticipating(UUID uuid) {
            return participants.containsKey(uuid.toString());
        }

        @Override
        public AbstractGame.Participant getParticipant(UUID uuid) {
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
