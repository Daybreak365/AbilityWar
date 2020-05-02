package daybreak.abilitywar.game.manager;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class SpectatorManager {

	private SpectatorManager() {
	}

	private static final Set<String> spectators = new HashSet<>();

	public static boolean isSpectator(String name) {
		return spectators.contains(name);
	}

	public static void addSpectator(String name) {
		spectators.add(name);
	}

	public static void removeSpectator(String name) {
		spectators.remove(name);
	}

	public static Set<String> getSpectators() {
		return Collections.unmodifiableSet(spectators);
	}

}
