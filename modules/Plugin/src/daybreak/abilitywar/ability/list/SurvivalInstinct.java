package daybreak.abilitywar.ability.list;

import daybreak.abilitywar.ability.AbilityBase;
import daybreak.abilitywar.ability.AbilityManifest;
import daybreak.abilitywar.ability.AbilityManifest.Rank;
import daybreak.abilitywar.ability.AbilityManifest.Species;
import daybreak.abilitywar.ability.SubscribeEvent;
import daybreak.abilitywar.game.AbstractGame.Participant;
import daybreak.abilitywar.game.AbstractGame.Participant.ActionbarNotification.ActionbarChannel;
import daybreak.abilitywar.game.module.DeathManager;
import daybreak.abilitywar.game.team.interfaces.Teamable;
import daybreak.abilitywar.utils.base.concurrent.TimeUnit;
import daybreak.abilitywar.utils.base.math.LocationUtil;
import daybreak.abilitywar.utils.base.minecraft.entity.health.event.PlayerSetHealthEvent;
import daybreak.abilitywar.utils.base.minecraft.nms.NMS;
import daybreak.abilitywar.utils.library.PotionEffects;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.entity.EntityDamageByBlockEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.util.Vector;

import java.util.function.Predicate;

@AbilityManifest(name = "생존 본능", rank = Rank.B, species = Species.HUMAN, explain = {
		"게임 중 단 한 번 §c치명적인 공격§f을 받았을 때 체력을 §c반 칸 §f남기고 살아납니다.",
		"능력이 발동되면 주변의 생명체들을 모두 밀쳐내고 §b신속 II §f효과를 5초간 받으며,",
		"4초간 §d무적§f/§d공격 불능 §f상태가 됩니다."
})
public class SurvivalInstinct extends AbilityBase {

	private final Invincibility invincibility = new Invincibility(80);

	private final Predicate<Entity> predicate = new Predicate<Entity>() {
		@Override
		public boolean test(Entity entity) {
			if (entity.equals(getPlayer())) return false;
			if (entity instanceof Player) {
				if (!getGame().isParticipating(entity.getUniqueId())
						|| (getGame() instanceof DeathManager.Handler && ((DeathManager.Handler) getGame()).getDeathManager().isExcluded(entity.getUniqueId()))
						|| !getGame().getParticipant(entity.getUniqueId()).attributes().TARGETABLE.getValue()) {
					return false;
				}
				if (getGame() instanceof Teamable) {
					final Teamable teamGame = (Teamable) getGame();
					final Participant entityParticipant = teamGame.getParticipant(entity.getUniqueId()), participant = getParticipant();
					return !teamGame.hasTeam(entityParticipant) || !teamGame.hasTeam(participant) || (!teamGame.getTeam(entityParticipant).equals(teamGame.getTeam(participant)));
				}
			}
			return true;
		}
	};

	public SurvivalInstinct(Participant participant) {
		super(participant);
	}

	private void ability() {
		invincibility.start();
		getPlayer().setHealth(1);
		NMS.broadcastEntityEffect(getPlayer(), (byte) 2);
		NMS.broadcastEntityEffect(getPlayer(), (byte) 35);
		final Vector playerLocation = getPlayer().getLocation().toVector();
		for (LivingEntity entity : LocationUtil.getNearbyEntities(LivingEntity.class, getPlayer().getLocation(), 6, 6, predicate)) {
			entity.setVelocity(entity.getLocation().toVector().subtract(playerLocation).normalize().multiply(2).setY(0));
		}
		PotionEffects.SPEED.addPotionEffect(getPlayer(), 100, 1, true);
	}

	@SubscribeEvent(onlyRelevant = true, priority = 6, ignoreCancelled = true)
	private void onEntityDamage(EntityDamageEvent e) {
		if (!invincibility.started) {
			if (getPlayer().getHealth() - e.getFinalDamage() <= 0) {
				e.setCancelled(true);
				ability();
			}
		} else if (invincibility.isRunning()) {
			e.setCancelled(true);
		}
	}

	@SubscribeEvent(onlyRelevant = true, priority = 6, ignoreCancelled = true)
	private void onEntityDamageByBlock(EntityDamageByBlockEvent e) {
		onEntityDamage(e);
	}

	@SubscribeEvent(priority = 6, ignoreCancelled = true)
	private void onEntityDamageByEntity(EntityDamageByEntityEvent e) {
		if (getPlayer().equals(e.getEntity())) {
			onEntityDamage(e);
		} else if (invincibility.isRunning() && (getPlayer().equals(e.getDamager()) || (e.getDamager() instanceof Projectile && getPlayer().equals(((Projectile) e.getDamager()).getShooter())))) {
			e.setCancelled(true);
		}
	}

	@SubscribeEvent(onlyRelevant = true, ignoreCancelled = true)
	private void onPlayerSetHealth(PlayerSetHealthEvent e) {
		if (invincibility.started) return;
		if (e.getHealth() == 0.0) {
			e.setCancelled(true);
			ability();
		}
	}

	private class Invincibility extends AbilityTimer {

		private final ActionbarChannel actionbarChannel = newActionbarChannel();
		private boolean started = false;

		private Invincibility(final int ticks) {
			super(ticks);
			setPeriod(TimeUnit.TICKS, 1);
		}

		@Override
		public boolean start() {
			if (started) return false;
			return super.start();
		}

		@Override
		protected void onStart() {
			started = true;
		}

		@Override
		protected void run(int count) {
			actionbarChannel.update("§d무적§f/§d공격 불능§f: " + (getCount() / 20.0) + "초");
		}

		@Override
		protected void onEnd() {
			onSilentEnd();
		}

		@Override
		protected void onSilentEnd() {
			actionbarChannel.unregister();
			getRestriction().new Condition() {
				@Override
				public boolean condition() {
					return true;
				}
			}.register();
		}
	}

}
