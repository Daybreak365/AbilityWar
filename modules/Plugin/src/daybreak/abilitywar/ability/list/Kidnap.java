package daybreak.abilitywar.ability.list;

import daybreak.abilitywar.ability.AbilityBase;
import daybreak.abilitywar.ability.AbilityManifest;
import daybreak.abilitywar.ability.AbilityManifest.Rank;
import daybreak.abilitywar.ability.AbilityManifest.Species;
import daybreak.abilitywar.ability.SubscribeEvent;
import daybreak.abilitywar.ability.decorator.TargetHandler;
import daybreak.abilitywar.config.ability.AbilitySettings.SettingObject;
import daybreak.abilitywar.game.AbstractGame.Participant;
import daybreak.abilitywar.utils.annotations.Support;
import daybreak.abilitywar.utils.base.Formatter;
import daybreak.abilitywar.utils.base.concurrent.TimeUnit;
import daybreak.abilitywar.utils.base.minecraft.version.ServerVersion.Version;
import daybreak.abilitywar.utils.library.PotionEffects;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Projectile;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityEvent;
import org.bukkit.projectiles.ProjectileSource;

@Support(min = Version.v1_11_R1)
@AbilityManifest(name = "납치", rank = Rank.B, species = Species.HUMAN, explain = {
		"아무 생명체나 철괴로 우클릭해 대상을 자신에게 태울 수 있습니다. $[CooldownConfig]",
		"능력 사용중에는 신속 버프를 받고, 납치 대상은 지속 시간동안",
		"실명에 걸리며 대미지를 받지 않습니다. 대상은 $[DurationConfig]초 뒤에",
		"자동으로 내려지며, 지속 시간이 끝나기 전에 스스로 내릴 수 없습니다."
})
public class Kidnap extends AbilityBase implements TargetHandler {

	public static final SettingObject<Integer> CooldownConfig = abilitySettings.new SettingObject<Integer>(Kidnap.class, "Cooldown", 30,
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

	public static final SettingObject<Integer> DurationConfig = abilitySettings.new SettingObject<Integer>(Kidnap.class, "Duration", 6,
			"# 지속 시간") {

		@Override
		public boolean condition(Integer value) {
			return value >= 1;
		}

	};

	public Kidnap(Participant participant) {
		super(participant);
	}

	private LivingEntity target = null;
	private final CooldownTimer cooldownTimer = new CooldownTimer(CooldownConfig.getValue());
	private final DurationTimer skill = new DurationTimer(DurationConfig.getValue() * 20, cooldownTimer) {
		@Override
		protected void onDurationStart() {
			if (target != null && target.isValid()) {
				getPlayer().addPassenger(target);
				target.setInvulnerable(true);
			} else {
				stop(true);
			}
		}

		@Override
		protected void onDurationProcess(int seconds) {
			PotionEffects.SPEED.addPotionEffect(getPlayer(), 25, 1, true);
			PotionEffects.BLINDNESS.addPotionEffect(target, 40, 0, true);
			if (!getPlayer().equals(target.getVehicle())) {
				getPlayer().addPassenger(target);
			}
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
		protected void onDurationSilentEnd() {
			if (target != null && target.isValid()) {
				getPlayer().removePassenger(target);
				target.setInvulnerable(false);
				Bukkit.getPluginManager().callEvent(new KidnapEndEvent(target));
				target = null;
			}
		}
	}.setPeriod(TimeUnit.TICKS, 1);

	@SubscribeEvent
	private void onEntityDamageByEntity(EntityDamageByEntityEvent e) {
		if (skill.isRunning()) {
			Entity damager = e.getDamager();
			if (damager instanceof Projectile) {
				ProjectileSource source = ((Projectile) damager).getShooter();
				if (source instanceof Entity) {
					damager = (Entity) source;
				}
			}
			if (damager.equals(target) && getPlayer().equals(e.getEntity())) {
				e.setCancelled(true);
				target.sendMessage("§c지금 공격할 수 없습니다!");
			}
		}
	}

	@Override
	public void TargetSkill(Material materialType, LivingEntity entity) {
		if (materialType == Material.IRON_INGOT && !skill.isDuration() && !cooldownTimer.isCooldown()) {
			KidnapStartEvent startEvent = new KidnapStartEvent(entity);
			Bukkit.getPluginManager().callEvent(startEvent);
			if (!startEvent.isCancelled()) {
				this.target = entity;
				skill.start();
			} else {
				getPlayer().sendMessage(String.valueOf(startEvent.cancelMessage));
			}
		}
	}

	@SubscribeEvent
	private void onKidnapStart(KidnapStartEvent e) {
		if (skill.isRunning() && e.getEntity().equals(target)) {
			e.setCancelled(true);
			e.setCancelMessage("§c이미 다른 플레이어가 납치 중인 대상입니다.");
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
