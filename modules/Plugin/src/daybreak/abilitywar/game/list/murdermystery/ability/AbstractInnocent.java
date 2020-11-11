package daybreak.abilitywar.game.list.murdermystery.ability;

import daybreak.abilitywar.game.AbstractGame.Participant;
import daybreak.abilitywar.game.list.murdermystery.MurderMystery;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

public abstract class AbstractInnocent extends AbstractJob {

	protected AbstractInnocent(Participant participant) {
		super(participant);
	}

	@Override
	protected void onUpdate(Update update) {
		if (update == Update.RESTRICTION_CLEAR) {
			final PlayerInventory inventory = getPlayer().getInventory();
			final ItemStack two = inventory.getItem(2), three = inventory.getItem(3);
			inventory.clear();
			inventory.setItem(2, two);
			inventory.setItem(3, three);
			getPlayer().getInventory().setHeldItemSlot(0);
			((MurderMystery) getGame()).updateGold(getParticipant());
		}
	}

}
