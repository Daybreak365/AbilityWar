package daybreak.abilitywar.game.manager.object;

import daybreak.abilitywar.config.Configuration.Settings;
import daybreak.abilitywar.config.enums.CooldownDecrease;
import daybreak.abilitywar.game.AbstractGame;

@Deprecated
public class Wreck {

	public interface Handler {
		Wreck getWreck();
	}

	public static boolean isEnabled(AbstractGame game) {
		return game instanceof Wreck.Handler && ((Wreck.Handler) game).getWreck().isEnabled();
	}

	public static double calculateDecreasedAmount(final int maxDecrease) {
		if (maxDecrease < 0 || maxDecrease > 100)
			throw new IllegalArgumentException("maxDecrease must be between 0 ~ 100: (value = " + maxDecrease + ")");
		final CooldownDecrease cooldownDecrease = Settings.getCooldownDecrease();
		if (cooldownDecrease == CooldownDecrease._100) return 0.0;
		return Math.max(100 - maxDecrease, 100 - cooldownDecrease.getPercentage()) / 100.0;
	}

	private final boolean enabled;

	public Wreck(boolean enabled) {
		this.enabled = enabled;
	}

	public Wreck() {
		this(Settings.isWreckEnabled());
	}

	public boolean isEnabled() {
		return enabled;
	}

}
