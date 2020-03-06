package daybreak.abilitywar.utils.base.minecraft.compat.nms;

import daybreak.abilitywar.utils.base.minecraft.version.ServerVersion;

public class SoundsHandler {

	private static final Sounds sounds = newInstance(ServerVersion.getVersion());

	private static Sounds newInstance(ServerVersion.Version version) {
		switch (version) {
			case v1_8_R1:
				return new daybreak.abilitywar.utils.base.minecraft.compat.v1_8_R1.nms.SoundsImpl();
		}
		return null;
	}

	public static boolean isHandled() {
		return sounds != null;
	}

	public static Sounds getSounds() {
		return sounds;
	}

}
