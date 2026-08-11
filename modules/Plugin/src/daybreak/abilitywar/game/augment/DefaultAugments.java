package daybreak.abilitywar.game.augment;

import org.bukkit.ChatColor;
import org.jetbrains.annotations.NotNull;

public final class DefaultAugments {

	private DefaultAugments() {}

	public static AugmentRegistry createRegistry() {
		final AugmentRegistry registry = new AugmentRegistry();
		for (AugmentFactory.AugmentRegistration registration : AugmentFactory.getRegistrations()) {
			registry.register(registration.newInstance());
		}
		return registry;
	}

	public static String format(@NotNull Augment augment) {
		return augment.getRarity().format("[" + augment.getRarity().getDisplayName() + "] ") + ChatColor.WHITE + augment.getDisplayName() + ChatColor.GRAY + " - " + augment.getDescription();
	}
}
