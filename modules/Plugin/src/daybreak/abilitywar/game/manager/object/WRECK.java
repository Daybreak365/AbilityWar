package daybreak.abilitywar.game.manager.object;

import daybreak.abilitywar.config.Configuration.Settings;
import daybreak.abilitywar.config.enums.CooldownDecrease;
import daybreak.abilitywar.game.AbstractGame;

public class WRECK {

	private final boolean enabled;

	public WRECK(boolean enabled) {
		this.enabled = enabled;
	}

	public WRECK() {
		this(Settings.isWRECKEnabled());
	}

	public boolean isEnabled() {
		return enabled;
	}

	public interface Handler {
		default WRECK newWRECK() {
			return new WRECK();
		}

		WRECK getWRECK();

		boolean isWRECKEnabled();
	}

	public static boolean isEnabled(AbstractGame game) {
		return game instanceof WRECK.Handler && ((WRECK.Handler) game).isWRECKEnabled();
	}

	public static double calculateDecreasedAmount(final int maxDecrease) {
		if (maxDecrease < 0 || maxDecrease > 100)
			throw new IllegalArgumentException("Illegal argument: " + maxDecrease);
		final CooldownDecrease cooldownDecrease = Settings.getCooldownDecrease();
		if (cooldownDecrease == CooldownDecrease._100) return 0.0;
		return Math.max(100 - maxDecrease, 100 - cooldownDecrease.getPercentage()) / 100.0;
	}

}
