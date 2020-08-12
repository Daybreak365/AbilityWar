package daybreak.abilitywar.game.list.murdermystery.ability;

import daybreak.abilitywar.AbilityWar;
import daybreak.abilitywar.ability.AbilityBase;
import daybreak.abilitywar.ability.SubscribeEvent;
import daybreak.abilitywar.game.AbstractGame.Participant;
import daybreak.abilitywar.game.list.murdermystery.Items;
import daybreak.abilitywar.utils.library.PotionEffects;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

public abstract class AbstractMurderer extends AbilityBase {

	public AbstractMurderer(Participant participant) {
		super(participant);
	}

	@SubscribeEvent
	private void onEntityDamageByEntity(EntityDamageByEntityEvent e) {
		if (e.getDamager().equals(getPlayer()) && e.getEntity() instanceof Player && getGame().isParticipating(e.getEntity().getUniqueId()) && getPlayer().getInventory().getItemInMainHand().isSimilar(Items.MURDERER_SWORD.getStack())) {
			e.setCancelled(false);
			final MurderEvent event = new MurderEvent(this, getGame().getParticipant(e.getEntity().getUniqueId()));
			Bukkit.getPluginManager().callEvent(event);
			if (!event.isCancelled()) {
				new BukkitRunnable() {
					@Override
					public void run() {
						((Player) e.getEntity()).setHealth(0);
						PotionEffects.INVISIBILITY.addPotionEffect(getPlayer(), 100, 1, true);
					}
				}.runTaskLater(AbilityWar.getPlugin(), 2L);
			} else e.setCancelled(true);
		}
	}

	public boolean hasSword() {
		return Items.isMurdererSword(getPlayer().getInventory().getItem(1));
	}

	public boolean hasBow() {
		ItemStack stack = getPlayer().getInventory().getItem(2);
		return stack != null && stack.getType() == Material.BOW;
	}

	public boolean addArrow() {
		ItemStack stack = getPlayer().getInventory().getItem(3);
		if (stack != null && stack.getType() == Material.ARROW) {
			if (stack.getAmount() < 64) {
				stack.setAmount(stack.getAmount() + 1);
				getPlayer().getInventory().setItem(3, stack);
				getPlayer().sendMessage("§8+ §f1 화살");
				return true;
			} else return false;
		} else {
			getPlayer().getInventory().setItem(3, new ItemStack(Material.ARROW));
			getPlayer().sendMessage("§8+ §f1 화살");
			return true;
		}
	}

	public static class MurderEvent extends Event implements Cancellable {

		private static final HandlerList handlers = new HandlerList();

		@NotNull
		@Override
		public HandlerList getHandlers() {
			return handlers;
		}

		public static HandlerList getHandlerList() {
			return handlers;
		}

		private boolean cancelled;
		private final AbstractMurderer murderer;
		private final Participant target;

		private MurderEvent(final AbstractMurderer murderer, final Participant target) {
			this.murderer = murderer;
			this.target = target;
		}

		public AbstractMurderer getMurderer() {
			return murderer;
		}

		public Participant getTarget() {
			return target;
		}

		@Override
		public boolean isCancelled() {
			return cancelled;
		}

		@Override
		public void setCancelled(final boolean cancelled) {
			this.cancelled = cancelled;
		}
	}

}
