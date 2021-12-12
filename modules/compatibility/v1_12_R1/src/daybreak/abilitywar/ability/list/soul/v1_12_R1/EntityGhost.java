package daybreak.abilitywar.ability.list.soul.v1_12_R1;

import daybreak.abilitywar.AbilityWar;
import daybreak.abilitywar.ability.list.soul.Ghost;
import daybreak.abilitywar.game.AbstractGame;
import daybreak.abilitywar.game.AbstractGame.Participant;
import daybreak.abilitywar.game.module.DeathManager;
import daybreak.abilitywar.game.team.interfaces.Teamable;
import daybreak.abilitywar.utils.base.math.LocationUtil;
import net.minecraft.server.v1_12_R1.DamageSource;
import net.minecraft.server.v1_12_R1.DifficultyDamageScaler;
import net.minecraft.server.v1_12_R1.EntityHuman;
import net.minecraft.server.v1_12_R1.EntityTypes;
import net.minecraft.server.v1_12_R1.EntityVex;
import net.minecraft.server.v1_12_R1.EnumItemSlot;
import net.minecraft.server.v1_12_R1.ItemStack;
import net.minecraft.server.v1_12_R1.Items;
import net.minecraft.server.v1_12_R1.MinecraftKey;
import net.minecraft.server.v1_12_R1.PathfinderGoalLookAtPlayer;
import net.minecraft.server.v1_12_R1.World;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Comparator;
import java.util.function.Predicate;

public class EntityGhost extends EntityVex implements Ghost {

	@Override
	public void move(double x, double y, double z, float speed) {
		this.moveController.a(x, y, z, speed);
	}

	@Override
	public void remove() {
		die();
	}

	@Override
	public Location getLocation() {
		return new Location(world.getWorld(), locX, locY, locZ, yaw, pitch);
	}

	public static class DistanceComparator implements Comparator<Entity> {
		private final org.bukkit.entity.Entity entity;

		public DistanceComparator(org.bukkit.entity.Entity entity) {
			this.entity = entity;
		}

		public int compare(org.bukkit.entity.Entity entity1, org.bukkit.entity.Entity entity2) {
			final Location location = entity.getLocation();
			double d0 = location.distanceSquared(entity1.getLocation());
			double d1 = location.distanceSquared(entity2.getLocation());
			return Double.compare(d0, d1);
		}
	}

	static {
		final MinecraftKey key = new MinecraftKey("soul", "ghost");
		EntityTypes.b.a(35, key, EntityGhost.class);
	}

	private final Soul soul;
	private final EntityStand stand;
	private final JavaPlugin plugin = AbilityWar.getPlugin();
	private final Predicate<Player> predicate;
	private final boolean follow;

	public EntityGhost(Soul soul, World world, Location location, Color color, boolean follow) {
		super(world);
		this.soul = soul;
		this.predicate = new Predicate<Player>() {
			@Override
			public boolean test(Player player) {
				if (soul.getPlayer().equals(player)) return false;
				final AbstractGame abstractGame = soul.getGame();
				if (!abstractGame.isParticipating(player.getUniqueId())
						|| (abstractGame instanceof DeathManager.Handler && ((DeathManager.Handler) abstractGame).getDeathManager().isExcluded(player.getUniqueId()))
						|| !abstractGame.getParticipant(player.getUniqueId()).attributes().TARGETABLE.getValue()) {
					return false;
				}
				if (abstractGame instanceof Teamable) {
					final Teamable teamGame = (Teamable) abstractGame;
					final Participant entityParticipant = teamGame.getParticipant(player.getUniqueId()), participant = soul.getParticipant();
					return !teamGame.hasTeam(entityParticipant) || !teamGame.hasTeam(participant) || (!teamGame.getTeam(entityParticipant).equals(teamGame.getTeam(participant)));
				}
				return true;
			}
		};;
		this.stand = new EntityStand(world, this, location, color);
		setPosition(location.getX(), location.getY(), location.getZ());
		setInvisible(true);
		world.addEntity(stand, SpawnReason.CUSTOM);
		world.addEntity(this, SpawnReason.CUSTOM);
		stand.a(this, true);
		this.follow = follow;
	}

	@Override
	protected void a(DifficultyDamageScaler difficultydamagescaler) {
		this.setSlot(EnumItemSlot.MAINHAND, new ItemStack(Items.a));
		this.a(EnumItemSlot.MAINHAND, 0.0F);
	}

	@Override
	protected void r() {
		this.goalSelector.a(1, new PathfinderGoalLookAtPlayer(this, EntityHuman.class, 9.0F, 1.0F));
	}

	@Override
	public void B_() {
		if (!plugin.isEnabled()) {
			die();
			return;
		}
		setInvisible(true);

		if (follow) {
			final Player target = LocationUtil.getNearestEntity(Player.class, getLocation(), predicate);
			if (target != null) {
				final Location location = target.getLocation();
				moveController.a(location.getX(), location.getY(), location.getZ(), .75f);
			}
		}

		super.B_();
	}

	@Override
	public boolean damageEntity(DamageSource damagesource, float f) {
		return false;
	}

	@Override
	public void die() {
		stand.die();
		super.die();
	}

}
