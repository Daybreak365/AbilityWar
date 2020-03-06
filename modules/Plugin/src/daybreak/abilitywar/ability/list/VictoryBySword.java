package daybreak.abilitywar.ability.list;

import daybreak.abilitywar.AbilityWar;
import daybreak.abilitywar.ability.AbilityBase;
import daybreak.abilitywar.ability.AbilityManifest;
import daybreak.abilitywar.ability.AbilityManifest.Rank;
import daybreak.abilitywar.ability.AbilityManifest.Species;
import daybreak.abilitywar.ability.decorator.TargetHandler;
import daybreak.abilitywar.game.AbstractGame.Participant;
import daybreak.abilitywar.utils.annotations.Beta;
import daybreak.abilitywar.utils.base.concurrent.TimeUnit;
import daybreak.abilitywar.utils.library.ParticleLib;
import daybreak.abilitywar.utils.library.ParticleLib.RGB;
import daybreak.abilitywar.utils.math.LocationUtil;
import daybreak.abilitywar.utils.math.LocationUtil.Locations;
import daybreak.abilitywar.utils.math.geometry.Circle;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByBlockEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

@Beta
@AbilityManifest(Name = "진검승부", Rank = Rank.A, Species = Species.HUMAN)
public class VictoryBySword extends AbilityBase implements TargetHandler {

	public VictoryBySword(Participant participant) {
		super(participant,
				ChatColor.translateAlternateColorCodes('&', "&fBETA"));
	}

	@Override
	public boolean ActiveSkill(Material materialType, ClickType clickType) {
		return false;
	}

	private final CooldownTimer cooldownTimer = new CooldownTimer(100);

	@Override
	public void TargetSkill(Material materialType, LivingEntity entity) {
		if (entity instanceof Player && getGame().isParticipating((Player) entity) && !cooldownTimer.isCooldown()) {
			Ring ring = new Ring(cooldownTimer, 5, (Player) entity);
			ring.start();
		}
	}

	private static final RGB COLOR = new RGB(138, 25, 115);

	public class Ring extends DurationTimer implements Listener {

		private final double radius;
		private final Location center;
		private final Locations locations;
		private final Participant targetParticipant;
		private final Player target;

		public Ring(CooldownTimer cooldownTimer, double radius, Player target) {
			super(1200, cooldownTimer);
			this.radius = radius;
			this.center = getPlayer().getLocation().clone();
			this.locations = Circle.of(radius, (int) (radius * 30)).toLocations(center).floor(center.getY());
			this.target = target;
			this.targetParticipant = getGame().getParticipant(target);
			setPeriod(TimeUnit.TICKS, 1);
		}

		@Override
		protected void onDurationStart() {
			getParticipant().attributes().TARGETABLE.setValue(false);
			targetParticipant.attributes().TARGETABLE.setValue(false);
			Bukkit.getPluginManager().registerEvents(this, AbilityWar.getPlugin());
		}

		@EventHandler
		public void onPlayerMove(PlayerMoveEvent e) {
			Player player = e.getPlayer();
			if ((player.equals(getPlayer()) || player.equals(target)) && e.getTo() != null && !LocationUtil.isInCircle(center, e.getTo(), radius)) {
				if (LocationUtil.isInCircle(center, e.getFrom(), radius)) {
					e.setTo(e.getFrom());
				} else {
					e.setCancelled(true);
					player.teleport(center);
				}
			}
		}

		@EventHandler
		public void onPlayerTeleport(PlayerTeleportEvent e) {
			Player player = e.getPlayer();
			if ((player.equals(getPlayer()) || player.equals(target)) && e.getTo() != null && !LocationUtil.isInCircle(center, e.getTo(), radius)) {
				if (LocationUtil.isInCircle(center, e.getFrom(), radius)) {
					e.setTo(e.getFrom());
				} else {
					e.setCancelled(true);
					player.teleport(center);
				}
			}
		}

		@EventHandler
		public void onEntityDamageByEntity(EntityDamageByEntityEvent e) {
			Entity entity = e.getEntity();
			Entity damager = e.getDamager();
			if (damager instanceof Projectile) {
				Projectile projectile = (Projectile) damager;
				if (projectile.getShooter() instanceof Entity) {
					damager = (Entity) projectile.getShooter();
				}
			}
			if (entity.equals(getPlayer()) || entity.equals(target)) {
				if (damager.equals(getPlayer()) || damager.equals(target)) {
					e.setCancelled(false);
				} else {
					e.setCancelled(true);
					if (damager instanceof Player) damager.sendMessage(ChatColor.RED + "진검승부 중인 상대를 공격할 수 없습니다!");
				}
			} else {
				if (damager.equals(getPlayer()) || damager.equals(target)) {
					e.setCancelled(true);
					if (damager instanceof Player) damager.sendMessage(ChatColor.RED + "진검승부 중에 다른 상대를 공격할 수 없습니다!");
				}
			}
		}

		@EventHandler
		public void onEntityDamageByBlock(EntityDamageByBlockEvent e) {
			onEntityDamage(e);
		}

		@EventHandler
		public void onEntityDamage(EntityDamageEvent e) {
			if (e.getEntity().equals(getPlayer()) || e.getEntity().equals(target)) e.setCancelled(true);
		}

		@Override
		protected void onDurationProcess(int count) {
			for (Location loc : locations) {
				ParticleLib.REDSTONE.spawnParticle(loc, COLOR);
			}
		}

		@Override
		protected void onDurationEnd() {
			getParticipant().attributes().TARGETABLE.setValue(true);
			targetParticipant.attributes().TARGETABLE.setValue(true);
			HandlerList.unregisterAll(this);
		}

		@Override
		protected void onDurationSilentEnd() {
			getParticipant().attributes().TARGETABLE.setValue(true);
			targetParticipant.attributes().TARGETABLE.setValue(true);
			HandlerList.unregisterAll(this);
		}

	}

}
