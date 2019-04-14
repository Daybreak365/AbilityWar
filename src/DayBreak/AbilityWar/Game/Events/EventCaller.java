package DayBreak.AbilityWar.Game.Events;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventException;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.plugin.EventExecutor;

public class EventCaller implements EventExecutor {

	@Override
	public void execute(Listener listener, Event event) throws EventException {
		if (event instanceof EntityDamageEvent) {
			EntityDamageEvent e = (EntityDamageEvent) event;

			if (e.getEntity() instanceof Player) {
				Player p = (Player) e.getEntity();

				double health = p.getHealth();
				if ((health - e.getFinalDamage()) <= 0) {
					PlayerPreDeathEvent preDeathEvent = new PlayerPreDeathEvent(p);
					Bukkit.getPluginManager().callEvent(preDeathEvent);
					if (preDeathEvent.isCancelled()) {
						e.setDamage(0);
					}
				}
			}
		}
	}

}
