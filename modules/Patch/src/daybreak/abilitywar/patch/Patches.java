package daybreak.abilitywar.patch;

import com.google.common.collect.Sets;
import daybreak.abilitywar.patch.list.EssentialsTeleportInvulnerability;
import java.util.Collections;
import java.util.Set;

public class Patches {

	public static final Set<IPatch> patches = Collections.unmodifiableSet(Sets.newHashSet(
			EssentialsTeleportInvulnerability.instance
	));

}
