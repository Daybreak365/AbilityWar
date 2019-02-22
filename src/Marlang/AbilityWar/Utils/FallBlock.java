package Marlang.AbilityWar.Utils;

import java.lang.reflect.Method;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
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
 * @author _Marlang 말랑
 */
public abstract class FallBlock implements Listener {

	private Object Data = null;
	private Location location;
	private World world;
	private Vector vector = new Vector(0, 0, 0);

	public FallBlock(Material Data, Location location) {
		if(ServerVersion.getVersion() >= 13) {
			try {
				Method method = Material.class.getDeclaredMethod("createBlockData");
				this.Data = method.invoke(Data);
			} catch(Exception ex) {ex.printStackTrace();}
		} else {
			this.Data = new MaterialData(Data);
		}
		this.location = location;
		this.world = location.getWorld();
	}

	public FallBlock(Material Data, Location location, Vector vector) {
		if(ServerVersion.getVersion() >= 13) {
			try {
				Method method = Material.class.getDeclaredMethod("createBlockData");
				this.Data = method.invoke(Data);
			} catch(Exception ex) {ex.printStackTrace();}
		} else {
			this.Data = new MaterialData(Data);
		}
		this.location = location;
		this.world = location.getWorld();
		this.vector = vector;
	}
	
	/**
	 * FallingBlock를 스폰하지 못했을 경우 null 반환
	 */
	@SuppressWarnings("deprecation")
	public FallingBlock Spawn(boolean setBlock) {
		if(ServerVersion.getVersion() >= 13) {
			try {
				Method spawnFallingBlock = World.class.getDeclaredMethod("spawnFallingBlock", Location.class, Class.forName("org.bukkit.block.data.BlockData"));
				fb = (FallingBlock) spawnFallingBlock.invoke(world, location, Class.forName("org.bukkit.block.data.BlockData").cast(Data));
			} catch(Exception ex) {}
		} else if(ServerVersion.getVersion() >= 11) {
			fb = world.spawnFallingBlock(location, (MaterialData) Data);
		} else {
			fb = world.spawnFallingBlock(location, ((MaterialData) Data).getItemType(), ((MaterialData) Data).getData());
		}
		
		this.setBlock = setBlock;
		Bukkit.getPluginManager().registerEvents(this, AbilityWar.getPlugin());
		
		if(ServerVersion.getVersion() >= 9) {
			fb.setInvulnerable(true);
		}
		
		fb.setDropItem(false);
		
		fb.setVelocity(vector);
		
		return fb;
	}
	
	private boolean setBlock;
	private FallingBlock fb;
	
	abstract public void onChangeBlock(FallingBlock block);
	
	@EventHandler
	public void onEntityChangeBlock(EntityChangeBlockEvent e) {
		if(fb.equals(e.getEntity())) {
			onChangeBlock(fb);
			if(!setBlock) {
				e.setCancelled(true);
				e.getEntity().remove();
			}
			
			HandlerList.unregisterAll(this);
		}
	}
	
}
