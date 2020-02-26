package daybreak.abilitywar.utils.base.minecraft.compat.v1_15_R1.block;

import com.google.common.collect.ImmutableSet;
import daybreak.abilitywar.utils.base.minecraft.compat.block.BlockSnapshot;
import daybreak.abilitywar.utils.base.minecraft.compat.block.Blocks;
import org.bukkit.Material;
import org.bukkit.block.Block;

import java.util.Set;

public class BlocksImpl implements Blocks {

	@Override
	public BlockSnapshot createSnapshot(Block block) {
		return new BlockSnapshotImpl(block);
	}

	private final Set<Material> indestructible = ImmutableSet.of(Material.BARRIER, Material.BEDROCK, Material.COMMAND_BLOCK, Material.CHAIN_COMMAND_BLOCK, Material.REPEATING_COMMAND_BLOCK, Material.END_PORTAL_FRAME, Material.STRUCTURE_BLOCK);

	@Override
	public boolean isIndestructible(Material type) {
		return indestructible.contains(type);
	}

}
