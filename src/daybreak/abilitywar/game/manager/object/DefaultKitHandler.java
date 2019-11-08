package daybreak.abilitywar.game.manager.object;

import daybreak.abilitywar.config.AbilityWarSettings;
import daybreak.abilitywar.game.games.mode.AbstractGame.Participant;
import daybreak.abilitywar.utils.library.SoundLib;
import java.util.Collection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public interface DefaultKitHandler {

	default void giveDefaultKit(Player p) {
		p.setLevel(0);
		if (AbilityWarSettings.Settings.getStartLevel() > 0) {
			p.giveExpLevels(AbilityWarSettings.Settings.getStartLevel());
			SoundLib.ENTITY_PLAYER_LEVELUP.playSound(p);
		}
		Inventory inventory = p.getInventory();
		if (AbilityWarSettings.Settings.getInventoryClear()) {
			inventory.clear();
		}
		for (ItemStack itemStack : AbilityWarSettings.Settings.getDefaultKit()) {
			inventory.addItem(itemStack);
		}
	}

	default void giveDefaultKit(Collection<Participant> participants) {
		for(Participant p : participants) {
			giveDefaultKit(p.getPlayer());
		}
	}
	
}
