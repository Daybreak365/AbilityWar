package daybreak.abilitywar.utils.base.minecraft.compat.nms;

import daybreak.abilitywar.utils.base.minecraft.version.ServerVersion;

public class SoundsHandler {

	private static final Sounds sounds = newInstance(ServerVersion.getVersion());

	private static Sounds newInstance(ServerVersion.Version version) {
		try {
			return (Sounds) Class.forName("daybreak.abilitywar.utils.base.minecraft.compat." + version.name() + ".nms.SoundsImpl").getConstructor().newInstance();
		} catch (Exception ex) {
			return null;
		}
	}

	public static boolean isHandled() {
		return sounds != null;
	}

	public static Sounds getSounds() {
		return sounds;
	}

}
