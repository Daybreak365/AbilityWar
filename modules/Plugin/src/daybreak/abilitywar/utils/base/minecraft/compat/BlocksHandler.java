package daybreak.abilitywar.utils.base.minecraft.compat;

import daybreak.abilitywar.utils.base.minecraft.version.ServerVersion;
import daybreak.abilitywar.utils.base.minecraft.version.UnsupportedVersionException;

public class BlocksHandler {

	private static final Blocks blocks = newInstance(ServerVersion.getVersion());

	private static Blocks newInstance(int version) {
		switch (version) {
			case 12:
				return new daybreak.abilitywar.utils.base.minecraft.compat.v1_12_R1.BlocksImpl();
			case 13:
				return new daybreak.abilitywar.utils.base.minecraft.compat.v1_13_R1.BlocksImpl();
			case 14:
				return new daybreak.abilitywar.utils.base.minecraft.compat.v1_14_R1.BlocksImpl();
			case 15:
				return new daybreak.abilitywar.utils.base.minecraft.compat.v1_15_R1.BlocksImpl();
			default:
				throw new UnsupportedVersionException(version);
		}
	}

	public static Blocks getBlocks() {
		return blocks;
	}

}
