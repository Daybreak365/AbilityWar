package daybreak.abilitywar.game.list.tnt.ability;

import daybreak.abilitywar.ability.AbilityBase;
import daybreak.abilitywar.ability.SubscribeEvent;
import daybreak.abilitywar.ability.SubscribeEvent.Priority;
import daybreak.abilitywar.game.AbstractGame.Participant;
import daybreak.abilitywar.game.list.tnt.TNTTag;
import daybreak.abilitywar.game.list.tnt.TNTTag.TNTParticipant;
import daybreak.abilitywar.utils.base.minecraft.version.ServerVersion;
import org.bukkit.GameMode;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerGameModeChangeEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;

public abstract class TNTAbility extends AbilityBase {

	protected TNTAbility(Participant participant) throws IllegalStateException {
		super(participant);
		if (!(participant instanceof TNTParticipant)) throw new IllegalStateException("Participant is not a instance of TNTParticipant");
		if (!(getGame() instanceof TNTTag)) throw new IllegalStateException("Game is not a instance of TNTTag");
		if (ServerVersion.getVersion() < 12) {
			subscribeEvent(PlayerPickupItemEvent.class, new EventConsumer<PlayerPickupItemEvent>() {
				@Override
				public void onEvent(PlayerPickupItemEvent event) {
					event.setCancelled(true);
				}
			}, true, true, Priority.NORMAL);
		} else {
			subscribeEvent(EntityPickupItemEvent.class, new EventConsumer<EntityPickupItemEvent>() {
				@Override
				public void onEvent(EntityPickupItemEvent event) {
					event.setCancelled(true);
				}
			}, true, true, Priority.NORMAL);
		}
		getPlayer().setGameMode(GameMode.ADVENTURE);
	}

	public abstract void resetInventory();

	@SubscribeEvent(onlyRelevant = true)
	private void onPlayerSwapHandItems(final PlayerSwapHandItemsEvent e) {
		e.setCancelled(true);
	}

	@SubscribeEvent
	private void onInventoryClick(final InventoryClickEvent e) {
		if (getPlayer().getInventory().equals(e.getClickedInventory()) || getPlayer().getInventory().equals(e.getInventory())) {
			e.setCancelled(true);
		}
	}

	@SubscribeEvent(onlyRelevant = true)
	private void onPlayerDropItem(final PlayerDropItemEvent e) {
		e.setCancelled(true);
	}

	@SubscribeEvent(onlyRelevant = true)
	private void onPlayerGameModeChange(final PlayerGameModeChangeEvent e) {
		if (e.getNewGameMode() != GameMode.ADVENTURE) {
			e.setCancelled(true);
		}
	}

}
