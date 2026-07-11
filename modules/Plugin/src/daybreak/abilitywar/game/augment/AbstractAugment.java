package daybreak.abilitywar.game.augment;

import daybreak.abilitywar.game.AbstractGame.Participant;
import org.jetbrains.annotations.NotNull;

public abstract class AbstractAugment implements Augment {

	private final String key;
	private final String displayName;
	private final AugmentRarity rarity;
	private final String description;

	protected AbstractAugment(@NotNull String key, @NotNull String displayName, @NotNull AugmentRarity rarity, @NotNull String description) {
		this.key = key;
		this.displayName = displayName;
		this.rarity = rarity;
		this.description = description;
	}

	@Override
	public final String getKey() {
		return key;
	}

	@Override
	public final String getDisplayName() {
		return displayName;
	}

	@Override
	public final AugmentRarity getRarity() {
		return rarity;
	}

	@Override
	public final String getDescription() {
		return description;
	}

	@Override
	public boolean isAvailable(@NotNull AugmentContext context, @NotNull Participant participant) {
		return !context.hasAugment(participant, this);
	}
}
