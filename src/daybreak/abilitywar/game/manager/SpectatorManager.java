package daybreak.abilitywar.game.manager;

import org.bukkit.event.Listener;

import java.util.ArrayList;
import java.util.List;

public class SpectatorManager implements Listener {

	private SpectatorManager() {
	}

	private static List<String> Spectators = new ArrayList<String>();

	public static boolean isSpectator(String name) {
		return Spectators.contains(name);
	}

	public static void addSpectator(String name) {
		if (!Spectators.contains(name)) {
			Spectators.add(name);
		}
	}

	public static void removeSpectator(String name) {
		Spectators.remove(name);
	}

	public static List<String> getSpectators() {
		return new ArrayList<>(Spectators);
	}

}
