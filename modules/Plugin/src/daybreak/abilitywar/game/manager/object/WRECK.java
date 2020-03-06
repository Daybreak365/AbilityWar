package daybreak.abilitywar.game.manager.object;

import daybreak.abilitywar.config.Configuration.Settings;
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

}
