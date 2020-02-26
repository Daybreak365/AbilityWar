package daybreak.abilitywar.utils.base.minecraft.compat.block;

import daybreak.abilitywar.utils.base.minecraft.version.ServerVersion;
import daybreak.abilitywar.utils.base.minecraft.version.UnsupportedVersionException;

public class BlocksHandler {

	private static final Blocks blocks = newInstance(ServerVersion.getVersion());

	private static Blocks newInstance(ServerVersion.Version version) {
		switch (version) {
			case v1_12_R1:
				return new daybreak.abilitywar.utils.base.minecraft.compat.v1_12_R1.block.BlocksImpl();
			case v1_13_R1:
				return new daybreak.abilitywar.utils.base.minecraft.compat.v1_13_R1.block.BlocksImpl();
			case v1_13_R2:
				return new daybreak.abilitywar.utils.base.minecraft.compat.v1_13_R2.block.BlocksImpl();
			case v1_14_R1:
				return new daybreak.abilitywar.utils.base.minecraft.compat.v1_14_R1.block.BlocksImpl();
			case v1_15_R1:
				return new daybreak.abilitywar.utils.base.minecraft.compat.v1_15_R1.block.BlocksImpl();
			default:
				throw new UnsupportedVersionException(version);
		}
	}

	public static Blocks getBlocks() {
		return blocks;
	}

}
