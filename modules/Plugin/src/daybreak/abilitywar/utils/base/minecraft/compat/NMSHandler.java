package daybreak.abilitywar.utils.base.minecraft.compat;

import daybreak.abilitywar.utils.base.minecraft.version.ServerVersion;
import daybreak.abilitywar.utils.base.minecraft.version.UnsupportedVersionException;

public class NMSHandler {

	private static final NMS nms = newInstance(ServerVersion.getVersion());

	private static NMS newInstance(int version) {
		switch (version) {
			case 12:
				return new daybreak.abilitywar.utils.base.minecraft.compat.v1_12_R1.NMSImpl();
			case 13:
				return new daybreak.abilitywar.utils.base.minecraft.compat.v1_13_R1.NMSImpl();
			case 14:
				return new daybreak.abilitywar.utils.base.minecraft.compat.v1_14_R1.NMSImpl();
			case 15:
				return new daybreak.abilitywar.utils.base.minecraft.compat.v1_15_R1.NMSImpl();
			default:
				throw new UnsupportedVersionException(version);
		}
	}

	public static NMS getNMS() {
		return nms;
	}

}
