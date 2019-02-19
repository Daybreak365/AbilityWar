package Marlang.AbilityWar.Utils;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.FallingBlock;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.material.MaterialData;
import org.bukkit.util.Vector;

import Marlang.AbilityWar.AbilityWar;
import Marlang.AbilityWar.Utils.VersionCompat.ServerVersion;

/**
 * Falling Block
 * @author _Marlang ¸»¶û
 */
public class FallBlock implements Listener {

	private final MaterialData Data;
	private final Location location;
	private final World world;
	private Vector vector = new Vector(0, 0, 0);

	public FallBlock(MaterialData Data, Location location) {
		this.Data = Data;
		this.location = location;
		this.world = location.getWorld();
	}

	public FallBlock(MaterialData Data, Location location, Vector vector) {
		this.Data = Data;
		this.location = location;
		this.world = location.getWorld();
		this.vector = vector;
	}
	
	@SuppressWarnings("deprecation")
	public FallingBlock Spawn(boolean SetBlock) {
		if(ServerVersion.getVersion() >= 11) {
			fb = world.spawnFallingBlock(location, Data);
		} else {
			fb = world.spawnFallingBlock(location, Data.getItemType(), Data.getData());
		}
		
		if(!SetBlock) {
			Bukkit.getPluginManager().registerEvents(this, AbilityWar.getPlugin());
		}
		
		if(ServerVersion.getVersion() >= 9) {
			fb.setInvulnerable(true);
		}
		
		fb.setDropItem(false);
		
		fb.setVelocity(vector);
		
		return fb;
	}
	
	FallingBlock fb;
	
	@EventHandler
	public void onEntityChangeBlock(EntityChangeBlockEvent e) {
		if(fb.equals(e.getEntity())) {
			e.setCancelled(true);
			e.getEntity().remove();
			
			HandlerList.unregisterAll(this);
		}
	}
	
}
