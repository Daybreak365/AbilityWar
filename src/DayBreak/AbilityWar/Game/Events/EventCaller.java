package DayBreak.AbilityWar.Game.Events;

import org.bukkit.Bukkit;
import org.bukkit.entity.Damageable;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventException;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.plugin.EventExecutor;

import DayBreak.AbilityWar.Utils.VersionCompat.ServerVersion;

public class EventCaller implements EventExecutor {

	@Override
	public void execute(Listener listener, Event event) throws EventException {
		if (event instanceof EntityDamageEvent) {
			EntityDamageEvent e = (EntityDamageEvent) event;

			if (e.getEntity() instanceof Player) {
				Player p = (Player) e.getEntity();
				
				double health;
				
				if(ServerVersion.getVersion() >= 8) {
					health = p.getHealth();
				} else {
					health = ((Damageable) p).getHealth();
				}
				
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
