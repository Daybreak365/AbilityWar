package daybreak.abilitywar.game.module;

import daybreak.abilitywar.AbilityWar;
import org.bukkit.Bukkit;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;

public interface ListenerModule extends Module, Listener {

	@Override
	default void register() {
		Bukkit.getPluginManager().registerEvents(this, AbilityWar.getPlugin());
	}

	@Override
	default void unregister() {
		HandlerList.unregisterAll(this);
	}

}
