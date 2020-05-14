package daybreak.abilitywar.ability.list;

import daybreak.abilitywar.AbilityWar;
import daybreak.abilitywar.ability.AbilityBase;
import daybreak.abilitywar.ability.AbilityManifest;
import daybreak.abilitywar.ability.AbilityManifest.Rank;
import daybreak.abilitywar.ability.AbilityManifest.Species;
import daybreak.abilitywar.ability.decorator.TargetHandler;
import daybreak.abilitywar.ability.event.PreAbilityRestrictionEvent;
import daybreak.abilitywar.game.AbstractGame.Participant;
import daybreak.abilitywar.utils.annotations.Beta;
import daybreak.abilitywar.utils.base.concurrent.TimeUnit;
import daybreak.abilitywar.utils.base.math.LocationUtil;
import daybreak.abilitywar.utils.base.math.LocationUtil.Locations;
import daybreak.abilitywar.utils.base.math.geometry.Circle;
import daybreak.abilitywar.utils.library.ParticleLib;
import daybreak.abilitywar.utils.library.ParticleLib.RGB;
import daybreak.abilitywar.utils.library.PotionEffects;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByBlockEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.inventory.ItemStack;

@Beta
@AbilityManifest(name = "진검승부", rank = Rank.A, species = Species.HUMAN, explain = {
		"BETA"
})
public class VictoryBySword extends AbilityBase implements TargetHandler {

	public VictoryBySword(Participant participant) {
		super(participant);
	}

	private final CooldownTimer cooldownTimer = new CooldownTimer(100);

	private Ring ring = null;

	@Override
	public void TargetSkill(Material materialType, LivingEntity entity) {
		if (entity instanceof Player && getGame().isParticipating((Player) entity) && !cooldownTimer.isCooldown() && ring == null) {
			this.ring = new Ring(cooldownTimer, 5, (Player) entity);
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
		private final ItemStack[] contents;
		private final ItemStack[] targetContents;
		private final double health;
		private final double targetHealth;

		public Ring(CooldownTimer cooldownTimer, double radius, Player target) {
			super(1200, cooldownTimer);
			this.radius = radius;
			this.center = getPlayer().getLocation().clone();
			this.locations = Circle.of(radius, (int) (radius * 30)).toLocations(center).floor(center.getY());
			this.target = target;
			this.contents = getPlayer().getInventory().getContents();
			this.targetContents = target.getInventory().getContents();
			this.targetParticipant = getGame().getParticipant(target);
			getPlayer().getInventory().clear();
			target.getInventory().clear();
			if (targetParticipant.hasAbility()) {
				targetParticipant.getAbility().setRestricted(true);
			}
			this.health = getPlayer().getHealth();
			this.targetHealth = target.getHealth();
			getPlayer().setHealth(getPlayer().getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue());
			target.setHealth(target.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue());
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
					e.setTo(e.getFrom().setDirection(e.getTo().getDirection()));
				} else {
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
					e.setTo(center);
				}
			}
		}

		@EventHandler(priority = EventPriority.HIGHEST)
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
			if (e instanceof EntityDamageByEntityEvent) return;
			if (e.getEntity().equals(getPlayer()) || e.getEntity().equals(target)) e.setCancelled(true);
		}

		@EventHandler
		private void onPreAbilityRestriction(PreAbilityRestrictionEvent e) {
			if ((e.getAbility().getParticipant().equals(targetParticipant) || e.getAbility().getParticipant().equals(getParticipant())) && !e.isRestricted())
				e.setRestricted(true);
		}

		@EventHandler
		private void onItemPickup(EntityPickupItemEvent e) {

		}

		@Override
		protected void onDurationProcess(int count) {
			getPlayer().getInventory().clear();
			target.getInventory().clear();
			for (PotionEffects effect : PotionEffects.values()) {
				effect.removePotionEffect(getPlayer());
				effect.removePotionEffect(target);
			}
			for (Location loc : locations) {
				ParticleLib.REDSTONE.spawnParticle(loc, COLOR);
			}
		}

		@EventHandler
		protected void onDeath(PlayerDeathEvent e) {
			if (target.equals(e.getEntity()) || getPlayer().equals(e.getEntity())) stop(false);
		}

		@Override
		protected void onDurationEnd() {
			getParticipant().attributes().TARGETABLE.setValue(true);
			targetParticipant.attributes().TARGETABLE.setValue(true);
			HandlerList.unregisterAll(this);
			getPlayer().getInventory().setContents(contents);
			target.getInventory().setContents(targetContents);
			if (targetParticipant.hasAbility()) {
				targetParticipant.getAbility().setRestricted(false);
			}
			if (!target.isDead()) {
				target.setHealth(targetHealth);
			}
			if (!getPlayer().isDead()) {
				getPlayer().setHealth(health);
			}
			ring = null;
		}

		@Override
		protected void onDurationSilentEnd() {
			onDurationEnd();
		}

	}

}
