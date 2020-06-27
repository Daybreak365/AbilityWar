package daybreak.abilitywar.game.list.mix.synergy.list;

import daybreak.abilitywar.ability.AbilityManifest;
import daybreak.abilitywar.ability.AbilityManifest.Rank;
import daybreak.abilitywar.ability.AbilityManifest.Species;
import daybreak.abilitywar.ability.Scheduled;
import daybreak.abilitywar.ability.SubscribeEvent;
import daybreak.abilitywar.config.ability.AbilitySettings.SettingObject;
import daybreak.abilitywar.game.AbstractGame.Participant;
import daybreak.abilitywar.game.AbstractGame.Participant.ActionbarNotification.ActionbarChannel;
import daybreak.abilitywar.game.list.mix.synergy.Synergy;
import daybreak.abilitywar.utils.base.Formatter;
import daybreak.abilitywar.utils.base.collect.PushingList;
import daybreak.abilitywar.utils.base.concurrent.TimeUnit;
import daybreak.abilitywar.utils.library.SoundLib;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import org.bukkit.Location;
import org.bukkit.Note;
import org.bukkit.Note.Tone;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.potion.PotionEffect;

@AbilityManifest(name = "타임 루프", rank = Rank.S, species = Species.HUMAN, explain = {
		"다른 플레이어가 나를 공격하거나, 내가 다른 플레이어를 공격한 경우",
		"전투 상태가 시작됩니다. 전투 상태 중 죽을 만큼의 치명적인 대미지를",
		"받으면, 이 전투에 참여한 모든 플레이어의 시간이 전투 시작 전으로 돌아갑니다.",
		"단, 전투 중간에 참여한 플레이어의 경우에는 참여한 시점으로 돌아갑니다.",
		"전투 상태는 시작 후 10초간 전투를 하지 않을 경우 종료되며,",
		"다른 플레이어가 나를 공격하거나, 내가 다른 플레이어를 공격한 경우",
		"전투 시간이 연장됩니다. 전투에 참여한 플레이어를 공격한 플레이어,",
		"전투에 참여한 플레이어가 공격한 플레이어는 모두",
		"전투에 참여한 것으로 간주됩니다. $[CooldownConfig]"
})
public class TimeLoop extends Synergy {

	public static final SettingObject<Integer> CooldownConfig = synergySettings.new SettingObject<Integer>(TimeLoop.class, "Cooldown", 100,
			"# 쿨타임") {

		@Override
		public boolean condition(Integer value) {
			return value >= 0;
		}

		@Override
		public String toString() {
			return Formatter.formatCooldown(getValue());
		}

	};
	private final CooldownTimer cooldownTimer = new CooldownTimer(CooldownConfig.getValue());
	private final Map<Participant, PlayerLogger> loggers = new HashMap<>();
	@Scheduled
	private final Timer save = new Timer() {
		@Override
		public void run(int count) {
			for (PlayerLogger value : loggers.values()) {
				value.log();
			}
		}

	}.setPeriod(TimeUnit.TICKS, 2);
	private final ActionbarChannel actionbarChannel = newActionbarChannel();
	private final Timer inCombat = new Timer(10) {
		@Override
		protected void onStart() {
			loggers.put(getParticipant(), new PlayerLogger(getPlayer()));
			actionbarChannel.update("§a전투 중");
		}

		@Override
		protected void run(int count) {
		}

		@Override
		protected void onEnd() {
			loggers.clear();
			actionbarChannel.update("§a전투 종료", 2);
		}
	};

	public TimeLoop(Participant participant) {
		super(participant);
	}

	@SubscribeEvent
	public void onEntityDamage(EntityDamageEvent e) {
		if (e.getEntity() instanceof Player) {
			Player entity = (Player) e.getEntity();
			if (getGame().isParticipating(entity)) {
				Participant participant = getGame().getParticipant(entity);
				if (loggers.containsKey(participant) && loggers.get(participant).isRewinding()) {
					e.setCancelled(true);
				}
			}
		}
		if (cooldownTimer.isCooldown()) return;
		if (e.getEntity().equals(getPlayer()) && getPlayer().getHealth() - e.getFinalDamage() <= 0) {
			e.setCancelled(true);
			for (PlayerLogger value : loggers.values()) {
				value.rewind();
			}
			cooldownTimer.start();
		}
	}

	@SubscribeEvent
	public void onEntityDamageByEntity(EntityDamageByEntityEvent e) {
		onEntityDamage(e);
		if (cooldownTimer.isCooldown() || e.getEntity().equals(e.getDamager())) return;
		if (e.getEntity().equals(getPlayer()) && e.getDamager() instanceof Player) {
			Player damager = (Player) e.getDamager();
			if (getGame().isParticipating(damager)) {
				Participant dParticipant = getGame().getParticipant(damager);
				if (!loggers.containsKey(dParticipant)) {
					loggers.put(dParticipant, new PlayerLogger(damager));
				}
				if (inCombat.isRunning()) inCombat.setCount(10);
				else inCombat.start();
				return;
			}
		}
		if (e.getDamager().equals(getPlayer()) && e.getEntity() instanceof Player) {
			Player entity = (Player) e.getEntity();
			if (getGame().isParticipating(entity)) {
				Participant eParticipant = getGame().getParticipant(entity);
				if (!loggers.containsKey(eParticipant)) {
					loggers.put(eParticipant, new PlayerLogger(entity));
				}
				if (inCombat.isRunning()) inCombat.setCount(10);
				else inCombat.start();
				return;
			}
		}
		if (e.getEntity() instanceof Player && e.getDamager() instanceof Player) {
			Player entity = (Player) e.getEntity(), damager = (Player) e.getDamager();
			if (getGame().isParticipating(entity) && getGame().isParticipating(damager)) {
				Participant participant = getGame().getParticipant(entity), dParticipant = getGame().getParticipant(damager);
				if (loggers.containsKey(participant) && !loggers.containsKey(dParticipant)) {
					loggers.put(dParticipant, new PlayerLogger(damager));
					return;
				}
				if (loggers.containsKey(dParticipant) && !loggers.containsKey(participant)) {
					loggers.put(participant, new PlayerLogger(entity));
				}
			}
		}
	}

	private static class PlayerData {

		private final Player player;
		private final Location location;
		private final double health;
		private final int fireTicks;
		private final float fallDistance;
		private final Collection<PotionEffect> potionEffects;

		private PlayerData(Player player) {
			this.player = player;
			this.location = player.getLocation();
			this.health = player.getHealth();
			this.fireTicks = player.getFireTicks();
			this.fallDistance = player.getFallDistance();
			this.potionEffects = player.getActivePotionEffects();
		}

		private void apply() {
			player.teleport(location);
			if (health > 0.0) {
				player.setHealth(Math.min(player.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue(), health));
			}
			player.setFireTicks(fireTicks);
			player.setFallDistance(fallDistance);
			for (PotionEffect effect : player.getActivePotionEffects()) {
				player.removePotionEffect(effect.getType());
			}
			player.addPotionEffects(potionEffects);
		}

	}

	private class PlayerLogger {

		private final Player player;
		private final PlayerData primal;
		private final PushingList<PlayerData> playerDatas = new PushingList<>(100);
		private final Timer rewind = new Timer(100) {
			@Override
			public void run(int seconds) {
				PlayerData data = playerDatas.pollLast();
				if (data != null && !player.isDead()) {
					data.apply();
				} else {
					stop(false);
				}
			}

			@Override
			public void onEnd() {
				if (!player.isDead()) {
					primal.apply();
				}
				SoundLib.BELL.playInstrument(player, Note.natural(0, Tone.D));
				SoundLib.BELL.playInstrument(player, Note.sharp(0, Tone.F));
				SoundLib.BELL.playInstrument(player, Note.natural(1, Tone.A));
				inCombat.stop(false);
			}
		}.setPeriod(TimeUnit.TICKS, 1);

		private PlayerLogger(Player player) {
			this.player = player;
			this.primal = new PlayerData(player);
		}

		public void rewind() {
			rewind.start();
		}

		public boolean isRewinding() {
			return rewind.isRunning();
		}

		public void log() {
			if (isRewinding()) return;
			playerDatas.add(new PlayerData(player));
		}

	}

}
