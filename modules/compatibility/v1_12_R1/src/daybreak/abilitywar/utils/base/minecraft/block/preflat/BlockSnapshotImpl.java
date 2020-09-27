package daybreak.abilitywar.utils.base.minecraft.block.preflat;

import daybreak.abilitywar.utils.base.minecraft.block.IBlockSnapshot;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;

public class BlockSnapshotImpl implements IBlockSnapshot {

	private final Block block;
	private final Material type;
	private final byte data;
	private final BlockState state;

	public BlockSnapshotImpl(Block block) {
		this.block = block;
		this.type = block.getType();
		this.data = block.getData();
		this.state = block.getState();
	}

	@Override
	public void apply() {
		block.setType(type);
		block.setData(data);
		state.update(true, false);
	}

}
