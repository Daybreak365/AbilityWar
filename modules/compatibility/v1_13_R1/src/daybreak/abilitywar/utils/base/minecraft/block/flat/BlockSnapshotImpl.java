package daybreak.abilitywar.utils.base.minecraft.block.flat;

import daybreak.abilitywar.utils.base.minecraft.block.IBlockSnapshot;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.data.BlockData;

public class BlockSnapshotImpl implements IBlockSnapshot {

	private final Block block;
	private final BlockData data;
	private final BlockState state;

	public BlockSnapshotImpl(Block block) {
		this.block = block;
		this.data = block.getBlockData();
		this.state = block.getState();
	}

	@Override
	public void apply() {
		block.setBlockData(data);
		state.update(true, false);
	}

}
