package daybreak.abilitywar.game.manager.object;

import daybreak.abilitywar.AbilityWar;
import daybreak.abilitywar.game.AbstractGame;
import daybreak.abilitywar.game.AbstractGame.GameUpdate;
import org.bukkit.Bukkit;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByBlockEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.scheduler.BukkitRunnable;

public class ZeroTick implements Listener, AbstractGame.Observer {

	public ZeroTick(AbstractGame abstractGame) {
		Bukkit.getPluginManager().registerEvents(this, AbilityWar.getPlugin());
		abstractGame.attachObserver(this);
	}

	@EventHandler
	private void onEntityDamage(EntityDamageEvent e) {
		if (e.getEntity() instanceof LivingEntity) {
			new BukkitRunnable() {
				@Override
				public void run() {
					((LivingEntity) e.getEntity()).setNoDamageTicks(0);
				}
			}.runTaskLater(AbilityWar.getPlugin(), 1);
		}
	}

	@EventHandler
	private void onEntityDamageByEntity(EntityDamageByBlockEvent e) {
		this.onEntityDamage(e);
	}

	@EventHandler
	private void onEntityDamageByEntity(EntityDamageByEntityEvent e) {
		this.onEntityDamage(e);
	}

	@Override
	public void update(GameUpdate update) {
		if (update == GameUpdate.END) {
			HandlerList.unregisterAll(this);
		}
	}

}
