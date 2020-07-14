package daybreak.abilitywar.utils.base.minecraft.compat.nms;

import daybreak.abilitywar.utils.base.minecraft.version.ServerVersion;
import daybreak.abilitywar.utils.base.minecraft.version.ServerVersion.Version;
import daybreak.abilitywar.utils.base.minecraft.version.UnsupportedVersionException;

@Deprecated
public class NMSHandler {

	private static final iNMS nms = newInstance(ServerVersion.getVersion());

	private static iNMS newInstance(Version version) {
		try {
			return (iNMS) Class.forName("daybreak.abilitywar.utils.base.minecraft.compat." + version.name() + ".nms.NMSImpl").getConstructor().newInstance();
		} catch (Exception ex) {
			throw new UnsupportedVersionException(version);
		}
	}

	public static iNMS getNMS() {
		return nms;
	}

}
