package daybreak.abilitywar.game.manager.gui;

import org.bukkit.event.Listener;

public interface PagedGUI extends Listener {

	void openGUI(int page);
	int getCurrentPage();

}
