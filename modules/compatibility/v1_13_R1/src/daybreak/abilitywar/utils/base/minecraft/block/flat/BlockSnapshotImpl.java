package daybreak.abilitywar.utils.base.minecraft.block.flat;

import daybreak.abilitywar.utils.base.minecraft.block.IBlockSnapshot;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;

public class BlockSnapshotImpl implements IBlockSnapshot {

	private final Block block;
	private final BlockData data;

	public BlockSnapshotImpl(Block block) {
		this.block = block;
		this.data = block.getBlockData();
	}

	@Override
	public void apply() {
		block.setBlockData(data);
	}

}
