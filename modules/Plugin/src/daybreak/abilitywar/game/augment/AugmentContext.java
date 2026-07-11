package daybreak.abilitywar.game.augment;

import daybreak.abilitywar.game.AbstractGame;
import daybreak.abilitywar.game.AbstractGame.Participant;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

public interface AugmentContext {

	@NotNull
	AbstractGame getGame();

	@NotNull
	Collection<Augment> getAugments(@NotNull Participant participant);

	boolean hasAugment(@NotNull Participant participant, @NotNull Augment augment);

	void addAugment(@NotNull Participant participant, @NotNull Augment augment);

	default void addMaxHealth(@NotNull Participant participant, double amount) {
		final AttributeInstance attribute = participant.getPlayer().getAttribute(Attribute.GENERIC_MAX_HEALTH);
		if (attribute != null) {
			attribute.setBaseValue(Math.max(1, attribute.getBaseValue() + amount));
			participant.getPlayer().setHealth(Math.min(attribute.getValue(), participant.getPlayer().getHealth() + amount));
		}
	}
}
