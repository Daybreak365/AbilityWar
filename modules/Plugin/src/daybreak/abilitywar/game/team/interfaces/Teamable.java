package daybreak.abilitywar.game.team.interfaces;

import daybreak.abilitywar.game.AbstractGame.Participant;
import daybreak.abilitywar.game.interfaces.IGame;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;

public interface Teamable extends IGame {

	boolean hasTeam(Participant participant);
	Members getTeam(Participant participant);
	void setTeam(Participant participant, @Nullable Members nullableTeam);
	boolean teamExists(String name);
	Members getTeam(String name);
	Collection<Members> getTeams();
	Members newTeam(String name, String displayName) throws IllegalStateException, IllegalArgumentException;
	void removeTeam(Members team);

}
