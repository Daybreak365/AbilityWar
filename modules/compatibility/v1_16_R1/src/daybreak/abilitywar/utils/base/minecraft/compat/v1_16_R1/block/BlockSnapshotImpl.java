package daybreak.abilitywar.utils.base.minecraft.compat.v1_16_R1.block;

import daybreak.abilitywar.utils.base.minecraft.compat.block.BlockSnapshot;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;

public class BlockSnapshotImpl implements BlockSnapshot {

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
