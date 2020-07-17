package daybreak.abilitywar.utils.base.minecraft.block.preflat;

import daybreak.abilitywar.utils.base.minecraft.block.IBlockSnapshot;
import daybreak.abilitywar.utils.base.minecraft.block.IBlocks;
import org.bukkit.block.Block;

public class BlocksImpl implements IBlocks {
	@Override
	public IBlockSnapshot createSnapshot(Block block) {
		return new BlockSnapshotImpl(block);
	}
}
