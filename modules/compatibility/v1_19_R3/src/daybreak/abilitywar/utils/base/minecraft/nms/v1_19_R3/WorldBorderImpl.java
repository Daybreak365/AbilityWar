package daybreak.abilitywar.utils.base.minecraft.nms.v1_19_R3;

import daybreak.abilitywar.utils.base.minecraft.nms.IWorldBorder;
import net.minecraft.world.level.border.WorldBorder;
import org.bukkit.World;
import org.bukkit.craftbukkit.v1_19_R2.CraftWorld;
import org.jetbrains.annotations.NotNull;

public class WorldBorderImpl extends WorldBorder implements IWorldBorder {

	WorldBorderImpl(World world) {
		this.world = ((CraftWorld) world).getHandle();
	}

	@Override
	public void setWorld(@NotNull World world) {
		this.world = ((CraftWorld) world).getHandle();
	}

	@Override
	public void setDamageAmount(double amount) {
		super.setDamagePerBlock(amount);
	}

	@Override
	public void setDamageBuffer(double buffer) {
		super.setDamageSafeZone(buffer);
	}

	@Override
	public void setWarningDistance(int distance) {
		super.setWarningBlocks(distance);
	}

}
