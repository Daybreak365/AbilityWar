package daybreak.abilitywar.utils.base.minecraft.compat.nms;

import daybreak.abilitywar.utils.base.minecraft.nms.INMS;
import daybreak.abilitywar.utils.base.minecraft.nms.NMS;

@Deprecated
public class NMSHandler {

	public static INMS getNMS() {
		return NMS.INSTANCE;
	}

}
