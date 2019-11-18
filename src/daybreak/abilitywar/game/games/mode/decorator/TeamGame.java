package daybreak.abilitywar.game.games.mode.decorator;

import daybreak.abilitywar.game.games.mode.AbstractGame;
import java.util.Collection;

public interface TeamGame {

    boolean hasTeam(AbstractGame.Participant participant);
    Team getTeam(AbstractGame.Participant participant);
    void setTeam(AbstractGame.Participant participant, Team team);

    boolean teamExists(String name);
    Team getTeam(String name);
    Collection<Team> getTeams();
    Collection<AbstractGame.Participant> getParticipants(Team team);
    Team newTeam(String name, String displayName) throws IllegalStateException;
    void removeTeam(Team team);


    class Team {

        private final String name;
        private final String displayName;

        public Team(String name, String displayName) {
            this.name = name;
            this.displayName = displayName;
        }

        public String getName() {
            return name;
        }

        public String getDisplayName() {
            return displayName;
        }

        @Override
        public boolean equals(Object object) {
            if (object instanceof Team) {
                return ((Team) object).name.equals(name);
            }
            return false;
        }

        @Override
        public String toString() {
            return displayName;
        }

    }

}
