package daybreak.abilitywar.utils.base.minecraft.nms.v1_20_R1;

import daybreak.abilitywar.utils.base.minecraft.nms.IWorldBorder;
import net.minecraft.world.level.border.WorldBorder;
import org.bukkit.World;
import org.bukkit.craftbukkit.v1_20_R1.CraftWorld;

/** Keep the plugin interface's method names separate from remapped server methods. */
public final class WorldBorderImpl implements IWorldBorder {
    final WorldBorder handle = new WorldBorder();
    WorldBorderImpl(World world) { setWorld(world); }
    @Override public void setWorld(World world) { handle.world = ((CraftWorld) world).getHandle(); }
    @Override public void setCenter(double x, double z) { handle.setCenter(x, z); }
    @Override public void setSize(double size) { handle.setSize(size); }
    @Override public void setWarningTime(int time) { handle.setWarningTime(time); }
    @Override public void setDamageAmount(double amount) { handle.setDamagePerBlock(amount); }
    @Override public void setDamageBuffer(double buffer) { handle.setDamageSafeZone(buffer); }
    @Override public void setWarningDistance(int distance) { handle.setWarningBlocks(distance); }
}
