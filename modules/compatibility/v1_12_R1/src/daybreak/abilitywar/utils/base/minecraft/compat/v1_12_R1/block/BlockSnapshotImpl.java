package daybreak.abilitywar.utils.base.minecraft.compat.v1_12_R1.block;

import daybreak.abilitywar.utils.base.minecraft.compat.block.BlockSnapshot;
import org.bukkit.Material;
import org.bukkit.block.Block;

public class BlockSnapshotImpl implements BlockSnapshot {

	private final Block block;
	private final Material type;
	private final byte data;

	BlockSnapshotImpl(Block block) {
		this.block = block;
		this.type = block.getType();
		this.data = block.getData();
	}

	@Override
	public void apply() {
		block.setType(type);
		block.setData(data);
	}

}
