package daybreak.abilitywar.game.augment;

import daybreak.abilitywar.game.AbstractGame.Participant;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class AbstractAugment implements Augment, Listener {

	private final String key;
	private final String displayName;
	private final AugmentRarity rarity;
	private final String description;
	private AugmentContext context;

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

	@Override
	public final void apply(@NotNull AugmentContext context, @NotNull Participant participant) {
		this.context = context;
		context.addAugment(participant, this);
		onApply(context, participant);
	}

	protected void onApply(@NotNull AugmentContext context, @NotNull Participant participant) {}

	protected final boolean hasAugment(@Nullable Participant participant) {
		return context != null && participant != null && context.hasAugment(participant, this);
	}

	@Nullable
	protected final Participant getParticipant(@NotNull Player player) {
		return context != null ? context.getGame().getParticipant(player) : null;
	}

	protected final void heal(@NotNull Player player, double amount) {
		final AttributeInstance maxHealth = player.getAttribute(Attribute.GENERIC_MAX_HEALTH);
		if (maxHealth != null) player.setHealth(Math.min(maxHealth.getValue(), player.getHealth() + amount));
	}
}
