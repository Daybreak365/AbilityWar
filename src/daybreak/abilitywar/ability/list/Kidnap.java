package daybreak.abilitywar.ability.list;

import daybreak.abilitywar.ability.AbilityBase;
import daybreak.abilitywar.ability.AbilityManifest;
import daybreak.abilitywar.ability.AbilityManifest.Rank;
import daybreak.abilitywar.ability.AbilityManifest.Species;
import daybreak.abilitywar.ability.SubscribeEvent;
import daybreak.abilitywar.config.ability.AbilitySettings.SettingObject;
import daybreak.abilitywar.game.games.mode.AbstractGame.Participant;
import daybreak.abilitywar.utils.library.PotionEffects;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.event.entity.EntityEvent;
import org.spigotmc.event.entity.EntityDismountEvent;

@AbilityManifest(Name = "납치", Rank = Rank.B, Species = Species.HUMAN)
public class Kidnap extends AbilityBase {

	public static final SettingObject<Integer> CooldownConfig = new SettingObject<Integer>(Kidnap.class, "Cooldown", 30,
			"# 쿨타임") {

		@Override
		public boolean Condition(Integer value) {
			return value >= 0;
		}

	};

	public static final SettingObject<Integer> DurationConfig = new SettingObject<Integer>(Kidnap.class, "Duration", 6,
			"# 지속 시간") {

		@Override
		public boolean Condition(Integer value) {
			return value >= 1;
		}

	};

	public Kidnap(Participant participant) throws IllegalStateException {
		super(participant,
				ChatColor.translateAlternateColorCodes('&', "&f아무 생명체나 철괴로 우클릭해 대상을 자신에게 태울 수 있습니다."),
				ChatColor.translateAlternateColorCodes('&', "&f능력 사용중에는 신속 버프를 받고, 납치 대상은 지속 시간동안"),
				ChatColor.translateAlternateColorCodes('&', "&f실명에 걸리며 대미지를 받지 않습니다. 대상은 " + DurationConfig.getValue() + "초 뒤에"),
				ChatColor.translateAlternateColorCodes('&', "&f자동으로 내려지며, 지속 시간이 끝나기 전에 스스로 내릴 수 없습니다."));
	}

	private LivingEntity target = null;
	private final CooldownTimer cooldownTimer = new CooldownTimer(CooldownConfig.getValue());
	private final DurationTimer skill = new DurationTimer(DurationConfig.getValue(), cooldownTimer) {
		@Override
		protected void onDurationStart() {
			if (target != null && target.isValid()) {
				getPlayer().addPassenger(target);
				target.setInvulnerable(true);
			} else {
				stopTimer(true);
			}
		}

		@Override
		protected void onDurationProcess(int seconds) {
			PotionEffects.SPEED.addPotionEffect(getPlayer(), 25, 1, true);
			PotionEffects.BLINDNESS.addPotionEffect(target, 40, 0, true);
		}

		@Override
		protected void onDurationEnd() {
			if (target != null && target.isValid()) {
				getPlayer().removePassenger(target);
				target.setInvulnerable(false);
				Bukkit.getPluginManager().callEvent(new KidnapEndEvent(target));
				target = null;
			}
		}

		@Override
		protected void onSilentEnd() {
			if (target != null && target.isValid()) {
				getPlayer().removePassenger(target);
				target.setInvulnerable(false);
				Bukkit.getPluginManager().callEvent(new KidnapEndEvent(target));
				target = null;
			}
		}
	};

	@SubscribeEvent
	private void onPlayerDismount(EntityDismountEvent e) {
		if (skill.isRunning() && e.getDismounted().equals(getPlayer()) && e.getEntity().equals(target)) {
			e.setCancelled(true);
		}
	}

	@Override
	public boolean ActiveSkill(Material materialType, ClickType ct) {
		return false;
	}

	@Override
	public void TargetSkill(Material materialType, LivingEntity entity) {
		if (materialType == Material.IRON_INGOT && !skill.isDuration() && !cooldownTimer.isCooldown()) {
			KidnapStartEvent startEvent = new KidnapStartEvent(entity);
			Bukkit.getPluginManager().callEvent(startEvent);
			if (!startEvent.isCancelled()) {
				this.target = entity;
				skill.startTimer();
			} else {
				getPlayer().sendMessage(String.valueOf(startEvent.cancelMessage));
			}
		}
	}

	@SubscribeEvent
	private void onKidnapStart(KidnapStartEvent e) {
		if (skill.isRunning() && e.getEntity().equals(target)) {
			e.setCancelled(true);
			e.setCancelMessage(ChatColor.translateAlternateColorCodes('&', "&c이미 다른 플레이어가 납치 중인 대상입니다."));
		}
	}

	public static class KidnapEvent extends EntityEvent {

		private static final HandlerList handlers = new HandlerList();

		public static HandlerList getHandlerList() {
			return handlers;
		}

		@Override
		public HandlerList getHandlers() {
			return handlers;
		}

		private final Participant kidnapper;

		private KidnapEvent(Kidnap kidnap, Entity victim) {
			super(victim);
			this.kidnapper = kidnap.getParticipant();
		}

		public Participant getKidnapper() {
			return kidnapper;
		}

	}

	public class KidnapStartEvent extends KidnapEvent implements Cancellable {

		private KidnapStartEvent(Entity victim) {
			super(Kidnap.this, victim);
		}

		private boolean cancelled = false;
		private String cancelMessage = null;

		@Override
		public boolean isCancelled() {
			return cancelled;
		}

		@Override
		public void setCancelled(boolean cancelled) {
			this.cancelled = cancelled;
		}

		public void setCancelMessage(String cancelMessage) {
			this.cancelMessage = cancelMessage;
		}

	}

	public class KidnapEndEvent extends KidnapEvent {

		private KidnapEndEvent(Entity victim) {
			super(Kidnap.this, victim);
		}

	}

}
