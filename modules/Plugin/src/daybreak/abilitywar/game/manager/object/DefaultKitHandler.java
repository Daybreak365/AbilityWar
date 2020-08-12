package daybreak.abilitywar.game.manager.object;

import daybreak.abilitywar.config.Configuration;
import daybreak.abilitywar.config.kitpreset.KitConfiguration.KitSettings;
import daybreak.abilitywar.config.serializable.KitPreset;
import daybreak.abilitywar.game.AbstractGame.Participant;
import daybreak.abilitywar.utils.library.SoundLib;
import java.util.Collection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

public interface DefaultKitHandler {

	default void giveDefaultKit(Participant participant) {
		Player player = participant.getPlayer();
		player.setLevel(0);
		if (Configuration.Settings.getStartLevel() > 0) {
			player.giveExpLevels(Configuration.Settings.getStartLevel());
			SoundLib.ENTITY_PLAYER_LEVELUP.playSound(player);
		}
		final PlayerInventory inventory = player.getInventory();
		if (Configuration.Settings.getInventoryClear()) {
			inventory.clear();
		}

		final KitPreset kit;
		if (participant.hasAbility()) kit = KitSettings.getAbilityKit().getKits(participant.getAbility().getRegistration().getAbilityClass().getName());
		else kit = KitSettings.getKit();

		for (ItemStack stack : kit.getItems()) {
			inventory.addItem(stack);
		}
		inventory.setHelmet(kit.getHelmet());
		inventory.setChestplate(kit.getChestplate());
		inventory.setLeggings(kit.getLeggings());
		inventory.setBoots(kit.getBoots());
	}

	default void giveDefaultKit(Collection<? extends Participant> participants) {
		for (Participant p : participants) {
			giveDefaultKit(p);
		}
	}

}
