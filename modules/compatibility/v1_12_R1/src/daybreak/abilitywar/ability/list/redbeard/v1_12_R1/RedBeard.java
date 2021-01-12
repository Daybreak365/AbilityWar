package daybreak.abilitywar.ability.list.redbeard.v1_12_R1;

import daybreak.abilitywar.ability.list.redbeard.AbstractRedBeard;
import daybreak.abilitywar.game.AbstractGame.Participant;
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftPlayer;

public class RedBeard extends AbstractRedBeard {

	public RedBeard(Participant participant) {
		super(participant);
	}

	@Override
	protected void test() {
		ReinforceContainer.openInventory(((CraftPlayer) getPlayer()).getHandle());
	}

}
