package daybreak.abilitywar.ability.list;

import daybreak.abilitywar.AbilityWar;
import daybreak.abilitywar.ability.AbilityBase;
import daybreak.abilitywar.ability.AbilityManifest;
import daybreak.abilitywar.ability.AbilityManifest.Rank;
import daybreak.abilitywar.ability.AbilityManifest.Species;
import daybreak.abilitywar.game.games.mode.AbstractGame.Participant;
import daybreak.abilitywar.utils.annotations.Beta;
import daybreak.abilitywar.utils.base.concurrent.TimeUnit;
import daybreak.abilitywar.utils.library.ParticleLib;
import daybreak.abilitywar.utils.math.LocationUtil;
import daybreak.abilitywar.utils.math.LocationUtil.Locations;
import daybreak.abilitywar.utils.math.geometry.Circle;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByBlockEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.util.Vector;

@Beta
@AbilityManifest(Name = "진검승부", Rank = Rank.A, Species = Species.HUMAN)
public class VictoryBySword extends AbilityBase {

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
		if (entity instanceof Player) {
			Ring ring = new Ring(cooldownTimer, 5, (Player) entity);
			ring.start();
		}
	}

	private static final ParticleLib.RGB COLOR = new ParticleLib.RGB(43, 209, 224);

	public class Ring extends DurationTimer implements Listener {

		private final double radius;
		private final Location center;
		private final Locations locations;
		private final Player target;

		public Ring(CooldownTimer cooldownTimer, double radius, Player target) {
			super(1200, cooldownTimer);
			this.radius = radius;
			this.center = getPlayer().getLocation().clone();
			this.locations = Circle.of(radius, (int) (radius * 15)).toLocations(center).floor(center.getY());
			this.target = target;
			setPeriod(TimeUnit.TICKS, 1);
		}

		@Override
		protected void onDurationStart() {
			Bukkit.getPluginManager().registerEvents(this, AbilityWar.getPlugin());
		}

		@EventHandler
		public void onPlayerMove(PlayerMoveEvent e) {
			Player player = null;
			if (e.getPlayer().equals(getPlayer())) player = getPlayer();
			else if (e.getPlayer().equals(target)) player = target;
			if (player != null && e.getTo() != null && !LocationUtil.isInCircle(center, e.getTo(), radius)) {
				Vector vector = e.getTo().getDirection().normalize().multiply(-0.1).setY(0);
				Location location = e.getFrom().clone();
				while (LocationUtil.isInCircle(center, location, radius)) {
					location.add(vector);
				}
				location.setY(LocationUtil.getFloorYAt(location.getWorld(), player.getLocation().getY(), location.getBlockX(), location.getBlockZ()));
				Bukkit.broadcastMessage(location.toString());
				player.teleport(location);
			}
		}

		@EventHandler
		public void onEntityDamageByEntity(EntityDamageByEntityEvent e) {
			if ((e.getEntity().equals(getPlayer()) || e.getEntity().equals(target)) && (e.getDamager().equals(getPlayer()) || e.getDamager().equals(target))) {
				e.setCancelled(false);
			} else {
				e.setCancelled(true);
			}
		}

		@EventHandler
		public void onEntityDamageByEntity(EntityDamageByBlockEvent e) {
			e.setCancelled(true);
		}

		@EventHandler
		public void onEntityDamageByEntity(EntityDamageEvent e) {
			e.setCancelled(true);
		}

		@Override
		protected void onDurationProcess(int count) {
			for (Location loc : locations) {
				ParticleLib.REDSTONE.spawnParticle(loc, COLOR);
			}
		}

		@Override
		protected void onDurationEnd() {
			HandlerList.unregisterAll(this);
		}

		@Override
		protected void onDurationSilentEnd() {
			HandlerList.unregisterAll(this);
		}

	}

}
