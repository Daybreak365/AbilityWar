package daybreak.abilitywar.utils.base.minecraft.compat.nms;

import daybreak.abilitywar.utils.base.minecraft.version.ServerVersion;
import daybreak.abilitywar.utils.base.minecraft.version.UnsupportedVersionException;

public class NMSHandler {

	private static final NMS nms = newInstance(ServerVersion.getVersion());

	private static NMS newInstance(ServerVersion.Version version) {
		switch (version) {
			case v1_8_R1:
				return new daybreak.abilitywar.utils.base.minecraft.compat.v1_8_R1.nms.NMSImpl();
			default:
				throw new UnsupportedVersionException(version);
		}
	}

	public static NMS getNMS() {
		return nms;
	}

}
