package daybreak.abilitywar.game.manager.effect;

import daybreak.abilitywar.AbilityWar;
import daybreak.abilitywar.game.AbstractGame;
import daybreak.abilitywar.game.AbstractGame.GameTimer;
import daybreak.abilitywar.game.AbstractGame.Participant;
import daybreak.abilitywar.game.event.participant.ParticipantEvent;
import daybreak.abilitywar.game.manager.effect.registry.ApplicationMethod;
import daybreak.abilitywar.game.manager.effect.registry.EffectConstructor;
import daybreak.abilitywar.game.manager.effect.registry.EffectManifest;
import daybreak.abilitywar.game.manager.effect.registry.EffectRegistry;
import daybreak.abilitywar.game.manager.effect.registry.EffectRegistry.EffectRegistration;
import daybreak.abilitywar.utils.base.concurrent.TimeUnit;
import daybreak.abilitywar.utils.library.MaterialX;
import daybreak.abilitywar.utils.library.ParticleLib;
import org.bukkit.Bukkit;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.jetbrains.annotations.NotNull;

public class Bleed {

	public static class ParticipantBleedEvent extends ParticipantEvent {

		private static final HandlerList handlers = new HandlerList();

		public static HandlerList getHandlerList() {
			return handlers;
		}

		@Override
		public @NotNull HandlerList getHandlers() {
			return handlers;
		}

		private final double amount;

		public ParticipantBleedEvent(@NotNull Participant who, final double amount) {
			super(who);
			this.amount = amount;
		}

		public double getAmount() {
			return amount;
		}
	}

	public static final EffectRegistration<ParticipantBleed> registration = EffectRegistry.registerEffect(ParticipantBleed.class);

	private Bleed() {
	}

	public static void apply(Participant participant, TimeUnit timeUnit, int duration) {
		registration.apply(participant, timeUnit, duration);
	}

	public static void apply(Participant participant, TimeUnit timeUnit, int duration, int period) {
		registration.apply(participant, timeUnit, duration, "with-period", period);
	}

	public static void apply(AbstractGame game, LivingEntity livingEntity, TimeUnit timeUnit, int duration) {
		if (game.isParticipating(livingEntity.getUniqueId())) {
			apply(game.getParticipant(livingEntity.getUniqueId()), timeUnit, duration);
		} else {
			new LivingEntityBleed(game, livingEntity, timeUnit, duration).start();
		}
	}

	@EffectManifest(name = "출혈", displayName = "§c출혈", method = ApplicationMethod.MULTIPLE)
	public static final class ParticipantBleed extends AbstractGame.Effect implements Listener {

		private final Participant participant;
		private final Player player;
		private boolean bleed = false;
		private double damage = 0.5;

		@EffectConstructor(name = "with-period")
		public ParticipantBleed(Participant participant, TimeUnit timeUnit, int duration, int period) {
			participant.getGame().super(registration, participant, timeUnit.toTicks(duration) / period);
			this.participant = participant;
			this.player = participant.getPlayer();
			setPeriod(TimeUnit.TICKS, period);
		}

		public ParticipantBleed(Participant participant, TimeUnit timeUnit, int duration) {
			this(participant, timeUnit, duration, 5);
		}

		@Override
		protected void onStart() {
			Bukkit.getPluginManager().registerEvents(this, AbilityWar.getPlugin());
		}

		@EventHandler
		private void onPlayerDeath(PlayerDeathEvent e) {
			if (player.getUniqueId().equals(e.getEntity().getUniqueId())) {
				stop(false);
			}
		}

		@Override
		protected void run(int count) {
			if (!bleed) {
				player.setNoDamageTicks(0);
				player.damage(damage);
				Bukkit.getPluginManager().callEvent(new ParticipantBleedEvent(participant, damage));
				ParticleLib.BLOCK_CRACK.spawnParticle(player.getEyeLocation(), .3f, .3f, .3f, 10, MaterialX.REDSTONE_BLOCK);
			}
			bleed = !bleed;
			super.run(count);
		}

		public void setDamage(double damage) {
			this.damage = damage;
		}

		public double getDamage() {
			return damage;
		}

		@Override
		protected void onEnd() {
			HandlerList.unregisterAll(this);
			super.onEnd();
		}

		@Override
		protected void onSilentEnd() {
			HandlerList.unregisterAll(this);
			super.onSilentEnd();
		}

	}

	private static final class LivingEntityBleed extends GameTimer {

		private final LivingEntity livingEntity;

		private LivingEntityBleed(AbstractGame game, LivingEntity livingEntity, TimeUnit timeUnit, int duration) {
			game.super(TaskType.REVERSE, timeUnit.toTicks(duration) / 5);
			this.livingEntity = livingEntity;
			setPeriod(TimeUnit.TICKS, 5);
		}

		@Override
		protected void run(int count) {
			if (count % 2 == 0) {
				if (livingEntity.isDead()) {
					stop(false);
					return;
				}
				livingEntity.setNoDamageTicks(0);
				livingEntity.damage(0.5);
				ParticleLib.BLOCK_CRACK.spawnParticle(livingEntity.getEyeLocation(), .3f, .3f, .3f, 8, MaterialX.REDSTONE_BLOCK);
			}
		}

	}

}
