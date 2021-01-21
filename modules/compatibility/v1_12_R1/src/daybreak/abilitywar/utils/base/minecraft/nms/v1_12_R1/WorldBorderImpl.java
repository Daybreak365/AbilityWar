package daybreak.abilitywar.utils.base.minecraft.nms.v1_12_R1;

import daybreak.abilitywar.utils.base.minecraft.nms.IWorldBorder;
import net.minecraft.server.v1_12_R1.WorldBorder;
import org.bukkit.World;
import org.bukkit.craftbukkit.v1_12_R1.CraftWorld;
import org.jetbrains.annotations.NotNull;

public class WorldBorderImpl extends WorldBorder implements IWorldBorder {

	WorldBorderImpl(World world) {
		this.world = ((CraftWorld) world).getHandle();
	}

	@Override
	public void setWorld(@NotNull World world) {
		this.world = ((CraftWorld) world).getHandle();
	}

}
