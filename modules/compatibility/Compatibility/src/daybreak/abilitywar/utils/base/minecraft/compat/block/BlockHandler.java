package daybreak.abilitywar.utils.base.minecraft.compat.block;

import org.bukkit.block.Block;

public class BlockHandler {

	private static final SnapshotSupplier snapshotSupplier = new SnapshotSupplier() {
		@Override
		public BlockSnapshot createSnapshot(Block block) {
			return new BlockSnapshotImpl(block);
		}
	};

	public static BlockSnapshot createSnapshot(Block block) {
		return snapshotSupplier.createSnapshot(block);
	}

	private interface SnapshotSupplier {

		BlockSnapshot createSnapshot(Block block);

	}

}
