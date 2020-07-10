package daybreak.abilitywar.utils.base.minecraft.compat.block;

import daybreak.abilitywar.utils.base.minecraft.version.ServerVersion;
import daybreak.abilitywar.utils.base.minecraft.version.ServerVersion.Version;
import daybreak.abilitywar.utils.base.minecraft.version.UnsupportedVersionException;
import org.bukkit.block.Block;

public class BlockHandler {

	private static final SnapshotSupplier snapshotSupplier = SnapshotSupplier.newInstance(ServerVersion.getVersion());

	public static BlockSnapshot createSnapshot(Block block) {
		return snapshotSupplier.createSnapshot(block);
	}

	private interface SnapshotSupplier {

		BlockSnapshot createSnapshot(Block block);

		static SnapshotSupplier newInstance(Version version) {
			return new SnapshotSupplier() {
				@Override
				public BlockSnapshot createSnapshot(Block block) {
					try {
						return (BlockSnapshot) Class.forName("daybreak.abilitywar.utils.base.minecraft.compat." + version.name() + ".block.BlockSnapshotImpl").getConstructor(Block.class).newInstance(block);
					} catch (Exception ex) {
						throw new UnsupportedVersionException(version);
					}
				}
			};
		}

	}

}
