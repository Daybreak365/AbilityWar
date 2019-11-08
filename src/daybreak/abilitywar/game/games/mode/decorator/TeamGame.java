package daybreak.abilitywar.game.games.mode.decorator;

import daybreak.abilitywar.game.games.mode.AbstractGame;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

public interface TeamGame {

    boolean hasTeam(AbstractGame.Participant participant);
    Team getTeam(AbstractGame.Participant participant);
    void setTeam(AbstractGame.Participant participant, Team team);

    boolean teamExists(String name);
    Team getTeam(String name);
    Collection<Team> getTeams();
    Team newTeam(String name, String displayName) throws IllegalStateException;


    class Team {

        private final String name;
        private final String displayName;
        private final ArrayList<AbstractGame.Participant> members = new ArrayList<>();

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

        public Collection<AbstractGame.Participant> getMembers() {
            return Collections.unmodifiableCollection(members);
        }

        public void addMember(AbstractGame.Participant participant) {
            if (!isMember(participant)) {
                members.add(participant);
            }
        }

        public void removeMember(AbstractGame.Participant participant) {
            if (isMember(participant)) {
                members.remove(participant);
            }
        }

        public boolean isMember(AbstractGame.Participant participant) {
            return members.contains(participant);
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
