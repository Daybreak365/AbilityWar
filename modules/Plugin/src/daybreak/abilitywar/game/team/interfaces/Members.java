package daybreak.abilitywar.game.team.interfaces;

import daybreak.abilitywar.config.serializable.SpawnLocation;
import daybreak.abilitywar.game.AbstractGame.Participant;

import java.util.Set;

public interface Members {
	String getName();
	String getDisplayName();
	boolean addMember(Participant participant);
	boolean removeMember(Participant participant);
	boolean isMember(Participant participant);
	Set<Participant> getMembers();
	boolean isExcluded();
	SpawnLocation getSpawn();
	void setSpawn(SpawnLocation spawn);
	void unregister();
}
