package daybreak.abilitywar.utils.base.minecraft;

import daybreak.abilitywar.AbilityWar;
import daybreak.abilitywar.utils.base.minecraft.version.ServerVersion;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.FallingBlock;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.material.MaterialData;
import org.bukkit.util.Vector;

/**
 * FallingBlock을 더욱 편하게 사용하기 위해 만든 유틸입니다.
 *
 * @author Daybreak 새벽
 */
public class FallingBlocks {

	@SuppressWarnings("deprecation")
	public static FallingBlock spawnFallingBlock(Location location, Material type, byte data, boolean glowing, Vector velocity, Behavior behavior) {
		final FallingBlock fallingBlock;
		if (ServerVersion.getVersion() >= 13) {
			fallingBlock = location.getWorld().spawnFallingBlock(location, type.createBlockData());
		} else {
			fallingBlock = location.getWorld().spawnFallingBlock(location, new MaterialData(type, data));
		}
		if (behavior != null) {
			Bukkit.getPluginManager().registerEvents(new Listener() {
				@EventHandler
				public void onEntityChangeBlock(EntityChangeBlockEvent event) {
					if (event.getEntity().equals(fallingBlock)) {
						if (!behavior.onEntityChangeBlock(fallingBlock)) {
							event.setCancelled(true);
							event.getEntity().remove();
						}
						HandlerList.unregisterAll(this);
					}
				}
			}, AbilityWar.getPlugin());
		}

		fallingBlock.setGlowing(glowing);
		fallingBlock.setInvulnerable(true);
		fallingBlock.setDropItem(false);
		if (velocity != null) {
			fallingBlock.setVelocity(velocity);
		}

		return fallingBlock;
	}

	public static FallingBlock spawnFallingBlock(Location location, Material type, byte data, boolean glowing, Behavior behavior) {
		return spawnFallingBlock(location, type, data, glowing, null, behavior);
	}

	public static FallingBlock spawnFallingBlock(Location location, Material type, byte data, boolean glowing, Vector velocity) {
		return spawnFallingBlock(location, type, data, glowing, velocity, null);
	}

	public static FallingBlock spawnFallingBlock(Location location, Material type, byte data, boolean glowing) {
		return spawnFallingBlock(location, type, data, glowing, null, null);
	}

	public static FallingBlock spawnFallingBlock(Location location, Material type, boolean glowing, Vector velocity, Behavior behavior) {
		return spawnFallingBlock(location, type, (byte) 0, glowing, velocity, behavior);
	}

	public static FallingBlock spawnFallingBlock(Location location, Material type, boolean glowing, Behavior behavior) {
		return spawnFallingBlock(location, type, (byte) 0, glowing, null, behavior);
	}

	public static FallingBlock spawnFallingBlock(Location location, Material type, boolean glowing, Vector velocity) {
		return spawnFallingBlock(location, type, (byte) 0, glowing, velocity, null);
	}

	public static FallingBlock spawnFallingBlock(Location location, Material type, boolean glowing) {
		return spawnFallingBlock(location, type, (byte) 0, glowing, null, null);
	}

	@FunctionalInterface
	public interface Behavior {

		Behavior TRUE = new Behavior() {
			@Override
			public boolean onEntityChangeBlock(FallingBlock fallingBlock) {
				return true;
			}
		};
		Behavior FALSE = new Behavior() {
			@Override
			public boolean onEntityChangeBlock(FallingBlock fallingBlock) {
				return false;
			}
		};

		boolean onEntityChangeBlock(FallingBlock fallingBlock);

	}

}
