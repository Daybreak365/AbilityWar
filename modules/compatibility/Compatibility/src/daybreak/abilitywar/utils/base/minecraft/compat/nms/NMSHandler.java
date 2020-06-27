package daybreak.abilitywar.utils.base.minecraft.compat.nms;

import daybreak.abilitywar.utils.base.minecraft.version.ServerVersion;
import daybreak.abilitywar.utils.base.minecraft.version.UnsupportedVersionException;

public class NMSHandler {

	private static final NMS nms = newInstance(ServerVersion.getVersion());

	private static NMS newInstance(ServerVersion.Version version) {
		switch (version) {
			case v1_9_R1:
				return new daybreak.abilitywar.utils.base.minecraft.compat.v1_9_R1.nms.NMSImpl();
			case v1_9_R2:
				return new daybreak.abilitywar.utils.base.minecraft.compat.v1_9_R2.nms.NMSImpl();
			case v1_10_R1:
				return new daybreak.abilitywar.utils.base.minecraft.compat.v1_10_R1.nms.NMSImpl();
			case v1_11_R1:
				return new daybreak.abilitywar.utils.base.minecraft.compat.v1_11_R1.nms.NMSImpl();
			case v1_12_R1:
				return new daybreak.abilitywar.utils.base.minecraft.compat.v1_12_R1.nms.NMSImpl();
			case v1_13_R1:
				return new daybreak.abilitywar.utils.base.minecraft.compat.v1_13_R1.nms.NMSImpl();
			case v1_13_R2:
				return new daybreak.abilitywar.utils.base.minecraft.compat.v1_13_R2.nms.NMSImpl();
			case v1_14_R1:
				return new daybreak.abilitywar.utils.base.minecraft.compat.v1_14_R1.nms.NMSImpl();
			case v1_15_R1:
				return new daybreak.abilitywar.utils.base.minecraft.compat.v1_15_R1.nms.NMSImpl();
			case v1_16_R1:
				return new daybreak.abilitywar.utils.base.minecraft.compat.v1_16_R1.nms.NMSImpl();
			default:
				throw new UnsupportedVersionException(version);
		}
	}

	public static NMS getNMS() {
		return nms;
	}

}
