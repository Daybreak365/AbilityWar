package daybreak.abilitywar.game.manager.object;

import daybreak.abilitywar.config.Configuration;
import daybreak.abilitywar.config.Configuration.Settings;
import daybreak.abilitywar.game.AbstractGame.Participant;
import daybreak.abilitywar.utils.library.SoundLib;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.Collection;
import java.util.List;

public interface DefaultKitHandler {

	default void giveDefaultKit(Participant participant) {
		Player player = participant.getPlayer();
		player.setLevel(0);
		if (Configuration.Settings.getStartLevel() > 0) {
			player.giveExpLevels(Configuration.Settings.getStartLevel());
			SoundLib.ENTITY_PLAYER_LEVELUP.playSound(player);
		}
		Inventory inventory = player.getInventory();
		if (Configuration.Settings.getInventoryClear()) {
			inventory.clear();
		}

		final List<ItemStack> stacks;
		if (participant.hasAbility())
			stacks = Settings.getAbilityKit().getKits(participant.getAbility().getRegistration().getAbilityClass().getName());
		else stacks = Settings.getDefaultKit();

		for (ItemStack stack : stacks) {
			inventory.addItem(stack);
		}
	}

	default void giveDefaultKit(Collection<Participant> participants) {
		for (Participant p : participants) {
			giveDefaultKit(p);
		}
	}

}
