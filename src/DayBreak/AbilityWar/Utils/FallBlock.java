package DayBreak.AbilityWar.Utils;

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

import DayBreak.AbilityWar.AbilityWar;
import DayBreak.AbilityWar.Utils.VersionCompat.ServerVersion;

/**
 * FallingBlock을 더욱 편하게 사용하기 위해 만든 유틸입니다.
 * @author DayBreak 새벽
 */
public abstract class FallBlock implements Listener {

	private Object Data = null;
	private Location location;
	private World world;
	private Vector vector = new Vector(0, 0, 0);

	/**
	 * Fallblock의 기본 생성자입니다.
	 * @param Data		생성할 FallingBlock의 종류
	 * @param location	생성할 위치
	 */
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

	/**
	 * Fallblock의 기본 생성자입니다.
	 * @param Data		생성할 FallingBlock의 종류
	 * @param location	생성할 위치
	 * @param vector	생성할 때 적용할 벡터
	 */
	public FallBlock(Material Data, Location location, Vector vector) {
		if(ServerVersion.getVersion() >= 13) {
			try {
				Method method = Material.class.getDeclaredMethod("createBlockData");
				this.Data = method.invoke(Data);
			} catch(Exception ex) {}
		} else {
			this.Data = new MaterialData(Data);
		}
		this.location = location;
		this.world = location.getWorld();
		this.vector = vector;
	}
	
	/**
	 * FallinBlock을 스폰합니다.
	 * @param setBlock 	FallingBlock이 땅에 떨어졌을 때 블록 설치으로 설치될지 여부
	 * @return 			스폰한 FallingBlock
	 * 					FallingBlock를 스폰하지 못했을 경우 null 반환
	 */
	@SuppressWarnings("deprecation")
	public FallingBlock Spawn(boolean setBlock) {
		if(ServerVersion.getVersion() >= 13) {
			try {
				Class<?> blockDataClass = Class.forName("org.bukkit.block.data.BlockData");
				Method spawnFallingBlock = World.class.getDeclaredMethod("spawnFallingBlock", Location.class, blockDataClass);
				fb = (FallingBlock) spawnFallingBlock.invoke(world, location, blockDataClass.cast(Data));
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
	
	/**
	 * FallingBlock 스폰한 엔티티가 땅에 떨어졌을 때 호출됩니다.
	 * @param block	FallingBlock 엔티티
	 */
	abstract public void onChangeBlock(FallingBlock block);
	
	/**
	 * FallingBlock이 땅에 떨어졌을 때 블록 설치 캔슬 및 onChangeBlock() 호출을 위해 사용됩니다.
	 */
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
