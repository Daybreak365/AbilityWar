package daybreak.abilitywar.game.augment;

import com.google.common.base.Preconditions;
import daybreak.abilitywar.game.AbstractGame.Participant;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class AbstractAugment implements Augment, Listener {

	private final AugmentManifest manifest;
	private AugmentContext context;

	protected AbstractAugment() {
		if (!getClass().isAnnotationPresent(AugmentManifest.class)) {
			throw new IllegalArgumentException("AugmentManifest가 없는 증강입니다.");
		}
		this.manifest = getClass().getAnnotation(AugmentManifest.class);
		Preconditions.checkNotNull(manifest.key());
		Preconditions.checkNotNull(manifest.name());
		Preconditions.checkNotNull(manifest.rarity());
		Preconditions.checkNotNull(manifest.description());
	}

	@Override
	public final String getKey() {
		return manifest.key();
	}

	@Override
	public final String getDisplayName() {
		return manifest.name();
	}

	@Override
	public final AugmentRarity getRarity() {
		return manifest.rarity();
	}

	@Override
	public final String getDescription() {
		return manifest.description();
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
