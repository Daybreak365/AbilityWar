package daybreak.abilitywar.ability.list.grapplinghook.v1_16_R3;

import daybreak.abilitywar.ability.list.grapplinghook.AbstractGrapplingHook;
import daybreak.abilitywar.ability.list.grapplinghook.HookEntity;
import daybreak.abilitywar.game.AbstractGame.Participant;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_16_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_16_R3.entity.CraftPlayer;

public class GrapplingHook extends AbstractGrapplingHook {

	public GrapplingHook(Participant participant) {
		super(participant);
	}

	@Override
	protected HookEntity createHook(Location targetLoc) {
		return new EntityHook(((CraftWorld) getPlayer().getWorld()).getHandle(), ((CraftPlayer) getPlayer()).getHandle(), targetLoc);
	}

}
