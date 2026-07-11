package daybreak.abilitywar.game.augment;

import daybreak.abilitywar.game.AbstractGame;
import daybreak.abilitywar.game.AbstractGame.Participant;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

public class ParticipantAugments implements AugmentContext {

	private final AbstractGame game;
	private final Map<Participant, Set<Augment>> augments = new LinkedHashMap<>();

	public ParticipantAugments(@NotNull AbstractGame game) {
		this.game = game;
	}

	@Override
	public AbstractGame getGame() {
		return game;
	}

	@Override
	public Collection<Augment> getAugments(@NotNull Participant participant) {
		final Set<Augment> participantAugments = augments.get(participant);
		return participantAugments != null ? Collections.unmodifiableSet(participantAugments) : Collections.<Augment>emptySet();
	}

	@Override
	public boolean hasAugment(@NotNull Participant participant, @NotNull Augment augment) {
		final Set<Augment> participantAugments = augments.get(participant);
		return participantAugments != null && participantAugments.contains(augment);
	}

	@Override
	public void addAugment(@NotNull Participant participant, @NotNull Augment augment) {
		Set<Augment> participantAugments = augments.get(participant);
		if (participantAugments == null) {
			participantAugments = new LinkedHashSet<>();
			augments.put(participant, participantAugments);
		}
		participantAugments.add(augment);
	}
}
