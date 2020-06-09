package daybreak.abilitywar.game.interfaces;

import daybreak.abilitywar.game.AbstractGame.Participant;
import daybreak.abilitywar.game.manager.object.ScoreboardManager;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import org.bukkit.ChatColor;

public interface TeamGame {

	boolean hasTeam(Participant participant);

	Team getTeam(Participant participant);

	void setTeam(Participant participant, Team team);

	boolean teamExists(String name);

	Team getTeam(String name);

	Collection<Team> getTeams();

	Team newTeam(String name, String displayName) throws IllegalStateException, IllegalArgumentException;

	void removeTeam(Team team);

	class Team {

		private final Set<Participant> participants = new HashSet<>();
		private final String name;
		private final String displayName;
		private final org.bukkit.scoreboard.Team scoreboardTeam;

		public Team(ScoreboardManager scoreboardManager, String name, String displayName) {
			this.name = name;
			this.displayName = displayName;
			this.scoreboardTeam = scoreboardManager.registerNewTeam(name);
			scoreboardTeam.setCanSeeFriendlyInvisibles(true);
			scoreboardTeam.setDisplayName(displayName);
			scoreboardTeam.setAllowFriendlyFire(false);
			scoreboardTeam.setPrefix(displayName + ChatColor.WHITE + " ");
		}

		public boolean add(Participant participant) {
			if (participants.add(participant)) {
				scoreboardTeam.addEntry(participant.getPlayer().getName());
				return true;
			}
			return false;
		}

		public boolean remove(Participant participant) {
			if (participants.remove(participant)) {
				scoreboardTeam.removeEntry(participant.getPlayer().getName());
				return true;
			}
			return false;
		}

		public boolean isTeam(Participant participant) {
			return participants.contains(participant);
		}

		public String getName() {
			return name;
		}

		public String getDisplayName() {
			return displayName;
		}

		public Set<Participant> getParticipants() {
			return Collections.unmodifiableSet(participants);
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

		public void unregister() {
			scoreboardTeam.unregister();
		}

	}

}
