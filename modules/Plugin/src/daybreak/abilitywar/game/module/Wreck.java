package daybreak.abilitywar.game.module;

import daybreak.abilitywar.config.Configuration.Settings;
import daybreak.abilitywar.config.enums.CooldownDecrease;
import daybreak.abilitywar.game.AbstractGame;

@ModuleBase(Wreck.class)
public class Wreck implements Module {

	public interface Handler {
		Wreck getWreck();
	}

	public static boolean isEnabled(AbstractGame game) {
		return game instanceof Handler && ((Handler) game).getWreck().isEnabled();
	}

	public static double calculateDecreasedAmount(final int maxDecrease) {
		if (maxDecrease < 0 || maxDecrease > 100)
			throw new IllegalArgumentException("maxDecrease must be between 0 ~ 100: (value = " + maxDecrease + ")");
		final CooldownDecrease cooldownDecrease = Settings.getCooldownDecrease();
		if (cooldownDecrease == CooldownDecrease._100) return 0.0;
		return Math.max(100 - maxDecrease, 100 - cooldownDecrease.getPercentage()) / 100.0;
	}

	@Override
	public void register() {}

	@Override
	public void unregister() {}

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
