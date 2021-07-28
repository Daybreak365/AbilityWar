package daybreak.abilitywar.game.manager.effect;

import daybreak.abilitywar.AbilityWar;
import daybreak.abilitywar.game.AbstractGame;
import daybreak.abilitywar.game.AbstractGame.Participant;
import daybreak.abilitywar.game.manager.effect.registry.ApplicationMethod;
import daybreak.abilitywar.game.manager.effect.registry.EffectConstructor;
import daybreak.abilitywar.game.manager.effect.registry.EffectManifest;
import daybreak.abilitywar.game.manager.effect.registry.EffectRegistry;
import daybreak.abilitywar.game.manager.effect.registry.EffectRegistry.EffectRegistration;
import daybreak.abilitywar.game.manager.effect.registry.EffectType;
import daybreak.abilitywar.utils.base.concurrent.TimeUnit;
import daybreak.abilitywar.utils.base.minecraft.nms.NMS;
import kotlin.ranges.RangesKt;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent.RegainReason;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

@EffectManifest(name = "수면", displayName = "§5수면", method = ApplicationMethod.UNIQUE_LONGEST, type = {
		EffectType.MOVEMENT_RESTRICTION
}, description = {
		"이동과 시야 전환이 불가능해지지만, 체력을 서서히 회복합니다.",
		"공격을 받으면 즉시 해제됩니다."
})
public class Sleep extends AbstractGame.Effect implements Listener {

	private static final PotionEffect BLINDNESS = new PotionEffect(PotionEffectType.BLINDNESS, Integer.MAX_VALUE, 0);
	public static final EffectRegistration<Sleep> registration = EffectRegistry.registerEffect(Sleep.class);

	public static void apply(Participant participant, TimeUnit timeUnit, int duration) {
		registration.apply(participant, timeUnit, duration);
	}

	public static void apply(Participant participant, TimeUnit timeUnit, int duration, double healAmount) {
		registration.apply(participant, timeUnit, duration, "heal-amount", healAmount);
	}

	private final Participant participant;
	private final ArmorStand hologram;
	private int stack = 0;
	private boolean direction = true;
	private double healAmount = .5;

	public Sleep(Participant participant, TimeUnit timeUnit, int duration) {
		participant.getGame().super(registration, participant, timeUnit.toTicks(duration));
		this.participant = participant;
		final Player player = participant.getPlayer();
		this.hologram = player.getWorld().spawn(player.getLocation(), ArmorStand.class);
		hologram.setVisible(false);
		hologram.setGravity(false);
		hologram.setInvulnerable(true);
		NMS.removeBoundingBox(hologram);
		hologram.setCustomNameVisible(true);
		hologram.setCustomName("§5수면...");
		setPeriod(TimeUnit.TICKS, 1);
	}

	@EffectConstructor(name = "heal-amount")
	public Sleep(Participant participant, TimeUnit timeUnit, int duration, double healAmount) {
		this(participant, timeUnit, duration);
		this.healAmount = healAmount;
	}

	@Override
	protected void onStart() {
		Bukkit.getPluginManager().registerEvents(this, AbilityWar.getPlugin());
	}

	@EventHandler
	private void onPlayerMove(final PlayerMoveEvent e) {
		if (e.getPlayer().getUniqueId().equals(participant.getPlayer().getUniqueId())) {
			final Location to = e.getTo(), from = e.getFrom();
			if (to != null) {
				from.setY(to.getY());
			}
			e.setTo(from);
		}
	}

	@EventHandler(priority = EventPriority.MONITOR)
	private void onEntityDamage(final EntityDamageEvent e) {
		if (participant.getPlayer().equals(e.getEntity())) {
			stop(true);
		}
	}

	@Override
	protected void run(int count) {
		super.run(count);
		final Player player = participant.getPlayer();
		player.removePotionEffect(PotionEffectType.BLINDNESS);
		player.addPotionEffect(BLINDNESS);
		if (count % 10 == 0) {
			final EntityRegainHealthEvent event = new EntityRegainHealthEvent(player, healAmount, RegainReason.CUSTOM);
			Bukkit.getPluginManager().callEvent(event);
			if (!event.isCancelled()) {
				player.setHealth(RangesKt.coerceIn(player.getHealth() + event.getAmount(), 0, player.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue()));
			}
		}
		if (hologram.isValid()) {
			if (direction) stack++;
			else stack--;
			if (stack <= 0 || stack >= 30) {
				this.direction = !direction;
			}
			hologram.teleport(participant.getPlayer().getLocation().clone().add(0, 2.2 + (stack * 0.008), 0));
		}
	}

	@Override
	protected void onEnd() {
		hologram.remove();
		HandlerList.unregisterAll(this);
		participant.getPlayer().removePotionEffect(PotionEffectType.BLINDNESS);
		super.onEnd();
	}

	@Override
	protected void onSilentEnd() {
		hologram.remove();
		HandlerList.unregisterAll(this);
		participant.getPlayer().removePotionEffect(PotionEffectType.BLINDNESS);
		super.onSilentEnd();
	}

}
