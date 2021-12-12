package daybreak.abilitywar.ability.list.soul.v1_12_R1;

import daybreak.abilitywar.ability.decorator.ActiveHandler;
import daybreak.abilitywar.ability.list.soul.AbstractSoul;
import daybreak.abilitywar.ability.list.soul.Ghost;
import daybreak.abilitywar.game.AbstractGame.Participant;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_12_R1.CraftWorld;

public class Soul extends AbstractSoul implements ActiveHandler {

	public Soul(Participant participant) {
		super(participant);
	}

	@Override
	protected Ghost createGhost(Location location, boolean follow) {
		return new EntityGhost(this, ((CraftWorld) location.getWorld()).getHandle(), location, Color.WHITE, follow);
	}

	@Override
	protected Ghost createGhost(Location location, Color color, boolean follow) {
		return new EntityGhost(this, ((CraftWorld) location.getWorld()).getHandle(), location, color, follow);
	}

}
