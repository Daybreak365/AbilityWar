package daybreak.abilitywar.game.list.mix.synergy.list;

import daybreak.abilitywar.ability.AbilityManifest;
import daybreak.abilitywar.ability.AbilityManifest.Rank;
import daybreak.abilitywar.ability.AbilityManifest.Species;
import daybreak.abilitywar.ability.SubscribeEvent;
import daybreak.abilitywar.ability.decorator.ActiveHandler;
import daybreak.abilitywar.config.ability.AbilitySettings.SettingObject;
import daybreak.abilitywar.game.AbstractGame.Participant;
import daybreak.abilitywar.game.interfaces.TeamGame;
import daybreak.abilitywar.game.list.mix.synergy.Synergy;
import daybreak.abilitywar.game.manager.object.DeathManager;
import daybreak.abilitywar.utils.annotations.Support;
import daybreak.abilitywar.utils.base.Formatter;
import daybreak.abilitywar.utils.base.concurrent.TimeUnit;
import daybreak.abilitywar.utils.base.math.VectorUtil;
import daybreak.abilitywar.utils.base.minecraft.version.NMSVersion;
import daybreak.abilitywar.utils.library.PotionEffects;
import java.util.function.Predicate;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityEvent;
import org.bukkit.event.player.PlayerGameModeChangeEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.projectiles.ProjectileSource;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

@Support.Version(min = NMSVersion.v1_11_R1)
@AbilityManifest(name = "투포환", rank = Rank.B, species = Species.HUMAN, explain = {
		"아무 생명체나 철괴로 우클릭해 대상을 자신에게 태울 수 있습니다. $[COOLDOWN_CONFIG]",
		"능력 사용중에는 신속 버프를 받고, 납치 대상은 지속 시간동안",
		"실명에 걸리며 대미지를 받지 않습니다. 대상은 $[DurationConfig]초 뒤에",
		"자동으로 내려지며, 지속 시간이 끝나기 전에 스스로 내릴 수 없습니다.",
		"능력 사용 중 철괴를 좌클릭하면 지속 시간을 즉시 끝냅니다.",
		"지속 시간이 종료되면 대상을 바라보는 방향으로 §c강하게 §f던집니다."
})
public class ShotPut extends Synergy implements ActiveHandler {

	public static final SettingObject<Integer> COOLDOWN_CONFIG = synergySettings.new SettingObject<Integer>(ShotPut.class, "Cooldown", 30,
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

	public static final SettingObject<Integer> DurationConfig = synergySettings.new SettingObject<Integer>(ShotPut.class, "Duration", 6,
			"# 지속 시간") {

		@Override
		public boolean condition(Integer value) {
			return value >= 1;
		}

	};
	private final Cooldown cooldownTimer = new Cooldown(COOLDOWN_CONFIG.getValue());
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
				if (getGame() instanceof TeamGame) {
					final TeamGame teamGame = (TeamGame) getGame();
					final Participant entityParticipant = getGame().getParticipant(entity.getUniqueId());
					return !teamGame.hasTeam(entityParticipant) || !teamGame.hasTeam(getParticipant()) || (!teamGame.getTeam(entityParticipant).equals(teamGame.getTeam(getParticipant())));
				}
			}
			return true;
		}
	};
	private Entity target = null;
	private final Duration skill = new Duration(DurationConfig.getValue() * 20, cooldownTimer) {
		@Override
		protected void onDurationStart() {
			if (target != null && target.isValid()) {
				target.setInvulnerable(true);
				target.setGravity(false);
			} else {
				stop(true);
			}
		}

		@Override
		protected void onDurationProcess(int seconds) {
			PotionEffects.SPEED.addPotionEffect(getPlayer(), 25, 1, true);
			if (target.getVehicle() != null) {
				target.eject();
			}
			final Vector direction = getPlayer().getLocation().getDirection().setY(0).normalize();
			final Location teleportLocation = getPlayer().getEyeLocation();

			teleportLocation.setYaw(getPlayer().getLocation().getYaw());
			teleportLocation.setPitch(0);
			teleportLocation.add(direction.clone().multiply(-0.5));
			teleportLocation.add(VectorUtil.rotateAroundAxisY(direction.clone(), 90).multiply(0.65));
			target.teleport(teleportLocation, TeleportCause.PLUGIN);
			if (target instanceof Player) {
				final Player targetPlayer = (Player) target;
				targetPlayer.setGliding(true);
				PotionEffects.BLINDNESS.addPotionEffect(targetPlayer, 40, 0, true);
			}
		}

		@Override
		protected void onDurationEnd() {
			if (target != null && target.isValid()) {
				target.setInvulnerable(false);
				target.setGravity(true);
				Bukkit.getPluginManager().callEvent(new KidnapEndEvent(target));
				target.setVelocity(getPlayer().getLocation().getDirection().multiply(5));
				target = null;
			}
		}

		@Override
		protected void onDurationSilentEnd() {
			if (target != null && target.isValid()) {
				target.setInvulnerable(false);
				target.setGravity(true);
				Bukkit.getPluginManager().callEvent(new KidnapEndEvent(target));
				target = null;
			}
		}
	}.setPeriod(TimeUnit.TICKS, 1);

	public ShotPut(Participant participant) {
		super(participant);
	}

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

	@SubscribeEvent(ignoreCancelled = true)
	private void onToggleSneakEvent(PlayerToggleSneakEvent e) {
		if (e.getPlayer().equals(target)) e.setCancelled(true);
	}

	@SubscribeEvent(ignoreCancelled = true)
	private void onGameModeChange(PlayerGameModeChangeEvent e) {
		if (e.getPlayer().equals(target)) e.setCancelled(true);
	}

	@SubscribeEvent(ignoreCancelled = true)
	private void onTeleport(PlayerTeleportEvent e) {
		if (e.getPlayer().equals(target) && e.getCause() != TeleportCause.PLUGIN) e.setCancelled(true);
	}

	@SubscribeEvent(onlyRelevant = true, ignoreCancelled = true)
	private void onPlayerInteractAtEntity(PlayerInteractAtEntityEvent e) {
		if (predicate.test(e.getRightClicked()) && getPlayer().getInventory().getItemInMainHand().getType() == Material.IRON_INGOT && !skill.isDuration() && !cooldownTimer.isCooldown()) {
			KidnapStartEvent startEvent = new KidnapStartEvent(e.getRightClicked());
			Bukkit.getPluginManager().callEvent(startEvent);
			if (!startEvent.isCancelled()) {
				this.target = e.getRightClicked();
				skill.start();
				e.setCancelled(true);
			} else {
				getPlayer().sendMessage(String.valueOf(startEvent.cancelMessage));
			}
		}
	}

	@Override
	public boolean ActiveSkill(@NotNull Material material, @NotNull ClickType clickType) {
		if (material == Material.IRON_INGOT && clickType == ClickType.LEFT_CLICK && skill.isRunning()) {
			skill.stop(false);
		}
		return false;
	}

	@SubscribeEvent
	private void onKidnapStart(KidnapStartEvent e) {
		if (skill.isRunning()) {
			if (e.getEntity().equals(target)) {
				e.setCancelled(true);
				e.setCancelMessage("§c이미 다른 플레이어가 납치 중인 대상입니다.");
			} else if (e.getKidnapper().getPlayer().equals(target)) {
				e.setCancelMessage("§c지금 납치할 수 없는 대상입니다.");
			}
		}
	}

	public static class KidnapEvent extends EntityEvent {

		private static final HandlerList handlers = new HandlerList();
		private final Participant kidnapper;

		private KidnapEvent(ShotPut kidnap, Entity victim) {
			super(victim);
			this.kidnapper = kidnap.getParticipant();
		}

		public static HandlerList getHandlerList() {
			return handlers;
		}

		@Override
		public HandlerList getHandlers() {
			return handlers;
		}

		public Participant getKidnapper() {
			return kidnapper;
		}

	}

	public class KidnapStartEvent extends KidnapEvent implements Cancellable {

		private boolean cancelled = false;
		private String cancelMessage = null;

		private KidnapStartEvent(Entity victim) {
			super(ShotPut.this, victim);
		}

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
			super(ShotPut.this, victim);
		}

	}

}
