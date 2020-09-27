package daybreak.abilitywar.game.manager.effect;

import daybreak.abilitywar.AbilityWar;
import daybreak.abilitywar.game.AbstractGame;
import daybreak.abilitywar.game.AbstractGame.GameTimer;
import daybreak.abilitywar.game.AbstractGame.Participant;
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

public class Bleed {

	private Bleed() {
	}

	public static void apply(Participant participant, TimeUnit timeUnit, int duration) {
		new ParticipantBleed(participant, timeUnit.toTicks(duration)).start();
	}

	public static void apply(Participant participant, TimeUnit timeUnit, int duration, int period) {
		new ParticipantBleed(participant, timeUnit.toTicks(duration), period).start();
	}

	public static void apply(AbstractGame game, LivingEntity livingEntity, TimeUnit timeUnit, int duration) {
		if (game.isParticipating(livingEntity.getUniqueId())) {
			new ParticipantBleed(game.getParticipant(livingEntity.getUniqueId()), timeUnit.toTicks(duration)).start();
		} else {
			new LivingEntityBleed(game, livingEntity, timeUnit, duration).start();
		}
	}

	private static final class ParticipantBleed extends AbstractGame.Effect implements Listener {

		private final Player player;

		private ParticipantBleed(Participant participant, int duration, int period) {
			participant.getGame().super(participant, "§c출혈", TaskType.REVERSE, duration / period);
			this.player = participant.getPlayer();
			setPeriod(TimeUnit.TICKS, period);
		}

		private ParticipantBleed(Participant participant, int duration) {
			this(participant, duration, 5);
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
			if (count % 2 == 0) {
				player.setNoDamageTicks(0);
				player.damage(0.5);
				ParticleLib.BLOCK_CRACK.spawnParticle(player.getEyeLocation(), .3f, .3f, .3f, 8, MaterialX.REDSTONE_BLOCK);
			}
			super.run(count);
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
