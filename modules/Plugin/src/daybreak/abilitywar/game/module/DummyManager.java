package daybreak.abilitywar.game.module;


import daybreak.abilitywar.AbilityWar;
import daybreak.abilitywar.game.AbstractGame;
import daybreak.abilitywar.utils.base.minecraft.nms.IDummy;
import daybreak.abilitywar.utils.base.minecraft.nms.NMS;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByBlockEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerJoinEvent;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

@ModuleBase(DummyManager.class)
public class DummyManager implements Module, Listener {

	private final AbstractGame game;

	public DummyManager(final AbstractGame game) {
		this.game = game;
	}

	@Override
	public void register() {
		Bukkit.getPluginManager().registerEvents(this, AbilityWar.getPlugin());
	}

	private final Map<UUID, IDummy> dummies = new HashMap<>();

	public void createDummy(final Location location) {
		final IDummy dummy = NMS.createDummy(location);
		dummies.put(dummy.getUniqueID(), dummy);
		game.addParticipant(dummy.getBukkitEntity());
		for (Player player : Bukkit.getOnlinePlayers()) {
			dummy.display(player);
		}
	}

	public int clearDummies() {
		final int clear = dummies.size();
		for (final Iterator<IDummy> iterator = dummies.values().iterator(); iterator.hasNext();) {
			iterator.next().remove();
			iterator.remove();
		}
		return clear;
	}

	@EventHandler
	private void onPlayerJoin(final PlayerJoinEvent e) {
		final Player player = e.getPlayer();
		for (final Iterator<IDummy> iterator = dummies.values().iterator(); iterator.hasNext();) {
			final IDummy dummy = iterator.next();
			if (!dummy.isAlive()) {
				iterator.remove();
				game.removeParticipant(dummy.getUniqueID());
				return;
			}
			dummy.display(player);
		}
	}

	@EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
	private void onEntityDamage(final EntityDamageEvent e) {
		final UUID uuid = e.getEntity().getUniqueId();
		final IDummy dummy = dummies.get(uuid);
		if (dummy != null) {
			if (!dummy.isAlive()) {
				dummies.remove(uuid);
				game.removeParticipant(dummy.getUniqueID());
				return;
			}
			dummy.addDamage(e.getFinalDamage());
			e.setDamage(0);
		}
	}

	@EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
	private void onEntityDamageByEntity(final EntityDamageByEntityEvent e) {
		this.onEntityDamage(e);
	}

	@EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
	private void onEntityDamageByBlock(final EntityDamageByBlockEvent e) {
		this.onEntityDamage(e);
	}

	@Override
	public void unregister() {
		HandlerList.unregisterAll(this);
		for (final Iterator<IDummy> iterator = dummies.values().iterator(); iterator.hasNext();) {
			iterator.next().remove();
			iterator.remove();
		}
	}

}
