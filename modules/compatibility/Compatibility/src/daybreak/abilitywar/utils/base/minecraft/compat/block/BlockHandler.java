package daybreak.abilitywar.utils.base.minecraft.compat.block;

import daybreak.abilitywar.utils.base.minecraft.version.ServerVersion;
import daybreak.abilitywar.utils.base.minecraft.version.UnsupportedVersionException;
import org.bukkit.block.Block;

public class BlockHandler {

	private static final SnapshotSupplier snapshotSupplier = SnapshotSupplier.newInstance(ServerVersion.getVersion());

	public static BlockSnapshot createSnapshot(Block block) {
		return snapshotSupplier.createSnapshot(block);
	}

	private interface SnapshotSupplier {

		BlockSnapshot createSnapshot(Block block);

		static SnapshotSupplier newInstance(ServerVersion.Version version) {
			switch (version) {
				case v1_9_R1:
					return new SnapshotSupplier() {
						@Override
						public BlockSnapshot createSnapshot(Block block) {
							return new daybreak.abilitywar.utils.base.minecraft.compat.v1_9_R1.block.BlockSnapshotImpl(block);
						}
					};
				case v1_9_R2:
					return new SnapshotSupplier() {
						@Override
						public BlockSnapshot createSnapshot(Block block) {
							return new daybreak.abilitywar.utils.base.minecraft.compat.v1_9_R2.block.BlockSnapshotImpl(block);
						}
					};
				case v1_10_R1:
					return new SnapshotSupplier() {
						@Override
						public BlockSnapshot createSnapshot(Block block) {
							return new daybreak.abilitywar.utils.base.minecraft.compat.v1_10_R1.block.BlockSnapshotImpl(block);
						}
					};
				case v1_11_R1:
					return new SnapshotSupplier() {
						@Override
						public BlockSnapshot createSnapshot(Block block) {
							return new daybreak.abilitywar.utils.base.minecraft.compat.v1_11_R1.block.BlockSnapshotImpl(block);
						}
					};
				case v1_12_R1:
					return new SnapshotSupplier() {
						@Override
						public BlockSnapshot createSnapshot(Block block) {
							return new daybreak.abilitywar.utils.base.minecraft.compat.v1_12_R1.block.BlockSnapshotImpl(block);
						}
					};
				case v1_13_R1:
					return new SnapshotSupplier() {
						@Override
						public BlockSnapshot createSnapshot(Block block) {
							return new daybreak.abilitywar.utils.base.minecraft.compat.v1_13_R1.block.BlockSnapshotImpl(block);
						}
					};
				case v1_13_R2:
					return new SnapshotSupplier() {
						@Override
						public BlockSnapshot createSnapshot(Block block) {
							return new daybreak.abilitywar.utils.base.minecraft.compat.v1_13_R2.block.BlockSnapshotImpl(block);
						}
					};
				case v1_14_R1:
					return new SnapshotSupplier() {
						@Override
						public BlockSnapshot createSnapshot(Block block) {
							return new daybreak.abilitywar.utils.base.minecraft.compat.v1_14_R1.block.BlockSnapshotImpl(block);
						}
					};
				case v1_15_R1:
					return new SnapshotSupplier() {
						@Override
						public BlockSnapshot createSnapshot(Block block) {
							return new daybreak.abilitywar.utils.base.minecraft.compat.v1_15_R1.block.BlockSnapshotImpl(block);
						}
					};
				case v1_16_R1:
					return new SnapshotSupplier() {
						@Override
						public BlockSnapshot createSnapshot(Block block) {
							return new daybreak.abilitywar.utils.base.minecraft.compat.v1_16_R1.block.BlockSnapshotImpl(block);
						}
					};
				default:
					throw new UnsupportedVersionException(version);
			}
		}

	}

}
