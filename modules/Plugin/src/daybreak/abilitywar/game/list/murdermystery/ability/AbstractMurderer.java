package daybreak.abilitywar.game.list.murdermystery.ability;

import daybreak.abilitywar.AbilityWar;
import daybreak.abilitywar.ability.SubscribeEvent;
import daybreak.abilitywar.game.AbstractGame.Participant;
import daybreak.abilitywar.game.list.murdermystery.Items;
import daybreak.abilitywar.game.list.murdermystery.MurderMystery;
import daybreak.abilitywar.utils.base.concurrent.TimeUnit;
import daybreak.abilitywar.utils.base.minecraft.nms.NMS;
import daybreak.abilitywar.utils.library.PotionEffects;
import org.bukkit.Bukkit;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

public abstract class AbstractMurderer extends AbstractJob {

	protected final AbilityTimer PASSIVE = new AbilityTimer() {
		@Override
		protected void run(int count) {
			if (Items.isMurdererSword(getPlayer().getInventory().getItemInMainHand())) {
				getPlayer().getAttribute(Attribute.GENERIC_MOVEMENT_SPEED).setBaseValue(0.15);
			}
		}
	}.setPeriod(TimeUnit.TICKS, 1).register();

	public AbstractMurderer(Participant participant) {
		super(participant);
	}

	@Override
	protected void onUpdate(Update update) {
		if (update == Update.RESTRICTION_CLEAR) {
			final PlayerInventory inventory = getPlayer().getInventory();
			final boolean hadSword = Items.isMurdererSword(inventory.getItem(1));
			final int arrowCount = getArrowCount();
			inventory.clear();
			if (arrowCount > 0 && !hasBow()) {
				getPlayer().getInventory().setItem(2, Items.NORMAL_BOW.getStack());
			}
			addArrow(arrowCount, false);
			getPlayer().getInventory().setHeldItemSlot(0);
			((MurderMystery) getGame()).updateGold(getParticipant());
			if (!hadSword) {
				getPlayer().sendMessage("§e15초 §f뒤에 §4살인자§c의 검§f을 얻습니다.");
				new AbilityTimer(1) {
					@Override
					protected void run(int count) {
						getPlayer().getInventory().setHeldItemSlot(0);
						inventory.setItem(1, Items.MURDERER_SWORD.getStack());
						getPlayer().sendMessage("§4살인자§c의 검§f을 들고 있을 때 더 빠르게 움직일 수 있습니다.");
						for (Player player : Bukkit.getOnlinePlayers()) {
							NMS.sendTitle(player, "§4머더§c가 검을 얻었습니다.", "", 10, 80, 10);
							new AbilityTimer(1) {
								@Override
								protected void onEnd() {
									NMS.clearTitle(player);
								}
							}.setInitialDelay(TimeUnit.SECONDS, 5).start();
						}
					}
				}.setInitialDelay(TimeUnit.SECONDS, 15).start();
			} else {
				getPlayer().getInventory().setHeldItemSlot(0);
				inventory.setItem(1, Items.MURDERER_SWORD.getStack());
			}
			PASSIVE.start();
		}
	}

	@SubscribeEvent
	private void onEntityDamageByEntity(EntityDamageByEntityEvent e) {
		if (e.getDamager().equals(getPlayer()) && e.getEntity() instanceof Player && getGame().isParticipating(e.getEntity().getUniqueId()) && getPlayer().getInventory().getItemInMainHand().isSimilar(Items.MURDERER_SWORD.getStack())) {
			e.setCancelled(false);
			final Participant target = getGame().getParticipant(e.getEntity().getUniqueId());
			if (!(target.getAbility() instanceof AbstractMurderer)) {
				final MurderEvent event = new MurderEvent(this, target);
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
			} else {
				getPlayer().sendMessage("§c머더 팀을 죽일 수 없습니다.");
				e.setCancelled(true);
			}
		}
	}

	public boolean hasSword() {
		return Items.isMurdererSword(getPlayer().getInventory().getItem(1));
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

		public MurderEvent(final AbstractMurderer murderer, final Participant target) {
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
