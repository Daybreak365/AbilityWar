package daybreak.abilitywar.utils.base.minecraft.compat.nms;

import daybreak.abilitywar.utils.base.minecraft.version.ServerVersion;
import daybreak.abilitywar.utils.base.minecraft.version.ServerVersion.Version;
import daybreak.abilitywar.utils.base.minecraft.version.UnsupportedVersionException;

public class NMSHandler {

	private static final NMS nms = newInstance(ServerVersion.getVersion());

	private static NMS newInstance(Version version) {
		try {
			return (NMS) Class.forName("daybreak.abilitywar.utils.base.minecraft.compat." + version.name() + ".nms.NMSImpl").getConstructor().newInstance();
		} catch (Exception ex) {
			throw new UnsupportedVersionException(version);
		}
	}

	public static NMS getNMS() {
		return nms;
	}

}
