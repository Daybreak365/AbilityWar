package DayBreak.AbilityWar.Utils;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.data.BlockData;
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
@SuppressWarnings("deprecation")
public abstract class FallBlock implements Listener {

	private Class<?> mdClass = null;
	private Object Data = null;
	private Byte byteData = null;
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
			this.Data = Data.createBlockData();
		} else {
			try {
				this.mdClass = Class.forName("org.bukkit.material.MaterialData");
				this.Data = mdClass.getConstructor(Material.class).newInstance(Data);
			} catch(Exception ex) {}
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
		this(Data, location);
		this.vector = vector;
	}

	/**
	 * FallinBlock을 스폰합니다.
	 * @return 			스폰한 FallingBlock
	 * 					FallingBlock를 스폰하지 못했을 경우 null 반환
	 */
	public FallingBlock Spawn() {
		final FallingBlock fb;
		if(ServerVersion.getVersion() >= 13) {
			BlockData bd = (BlockData) Data;
			fb = world.spawnFallingBlock(location, bd);
		} else {
			if(byteData != null) {
				fb = world.spawnFallingBlock(location, ((MaterialData) Data).getItemType(), byteData);
			} else {
				fb = world.spawnFallingBlock(location, (MaterialData) Data);
			}
		}
		
		Bukkit.getPluginManager().registerEvents(this, AbilityWar.getPlugin());
		
		fb.setGlowing(glowing);
		fb.setInvulnerable(true);
		fb.setDropItem(false);
		fb.setVelocity(vector);
		
		fbList.add(fb);
		
		return fb;
	}
	
	public FallBlock toggleSetBlock(boolean bool) {
		this.setBlock = bool;
		return this;
	}

	public FallBlock toggleGlowing(boolean bool) {
		this.glowing = bool;
		return this;
	}
 	
	private boolean glowing = false;
	private boolean setBlock = false;
	private final List<FallingBlock> fbList = new ArrayList<>();
	
	/**
	 * 스폰한 FallingBlock 엔티티가 땅에 떨어져 블록으로 변환되었을 때 호출됩니다.
	 */
	public abstract void onChangeBlock(FallingBlock block);
	
	/**
	 * FallingBlock이 땅에 떨어졌을 때 블록 설치 캔슬 및 onChangeBlock() 호출을 위해 사용됩니다.
	 */
	@EventHandler
	public void onEntityChangeBlock(EntityChangeBlockEvent e) {
		if(fbList.contains(e.getEntity())) {
			onChangeBlock((FallingBlock) e.getEntity());
			if(!setBlock) {
				e.setCancelled(true);
				e.getEntity().remove();
			}
			
			HandlerList.unregisterAll(this);
		}
	}
	
}
