package daybreak.abilitywar.ability.list;

import daybreak.abilitywar.ability.AbilityBase;
import daybreak.abilitywar.ability.AbilityManifest;
import daybreak.abilitywar.ability.AbilityManifest.Rank;
import daybreak.abilitywar.ability.AbilityManifest.Species;
import daybreak.abilitywar.ability.SubscribeEvent;
import daybreak.abilitywar.ability.decorator.ActiveHandler;
import daybreak.abilitywar.config.ability.AbilitySettings.SettingObject;
import daybreak.abilitywar.game.AbstractGame.Participant;
import daybreak.abilitywar.game.module.DeathManager;
import daybreak.abilitywar.game.team.interfaces.Teamable;
import daybreak.abilitywar.utils.base.Formatter;
import daybreak.abilitywar.utils.base.collect.Pair;
import daybreak.abilitywar.utils.base.concurrent.TimeUnit;
import daybreak.abilitywar.utils.base.math.LocationUtil;
import daybreak.abilitywar.utils.base.math.geometry.Wing;
import daybreak.abilitywar.utils.base.minecraft.FallingBlocks;
import daybreak.abilitywar.utils.base.minecraft.FallingBlocks.Behavior;
import daybreak.abilitywar.utils.base.minecraft.version.ServerVersion;
import daybreak.abilitywar.utils.library.ParticleLib;
import daybreak.abilitywar.utils.library.ParticleLib.RGB;
import daybreak.abilitywar.utils.library.SoundLib;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Damageable;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import java.util.function.Predicate;

@AbilityManifest(name = "넥스", rank = Rank.A, species = Species.GOD, explain = {
		"철괴를 우클릭하면 공중으로 올라갔다 바라보는 방향으로 날아가",
		"내려 찍으며 주변의 플레이어들에게 대미지를 입히고 날려보냅니다. $[COOLDOWN_CONFIG]"
})
public class Nex extends AbilityBase implements ActiveHandler {

	private static final Pair<Wing, Wing> NEX_WING = Wing.of(new boolean[][] {
			{false, false, false, false, false, false, false, false, false, false, false, false, false, false, false},
			{true, false, false, false, false, false, false, false, false, false, false, false, false, false, false},
			{true, false, false, false, false, false, false, false, false, false, false, false, false, false, false},
			{true, true, true, false, false, false, false, false, false, false, false, false, false, false, false},
			{true, true, true, true, false, false, false, false, false, false, false, false, false, false, false},
			{true, true, true, true, true, false, false, false, false, false, false, false, false, false, false},
			{true, true, true, true, true, true, false, false, false, false, false, false, false, false, false},
			{true, true, true, true, true, true, true, true, false, false, false, false, false, false, false},
			{false, true, true, true, true, true, true, true, true, false, false, false, false, false, false},
			{false, true, true, true, true, true, true, true, true, true, true, false, false, false, false},
			{false, true, true, true, true, true, true, true, true, true, true, true, false, false, false},
			{false, true, true, true, true, true, true, true, true, true, true, true, true, false, false},
			{false, false, true, true, true, true, true, true, true, true, true, true, true, true, false},
			{false, false, true, true, true, true, true, true, true, true, true, true, true, true, false},
			{false, false, true, true, true, true, true, true, true, true, true, true, true, true, false},
			{false, false, false, true, true, true, true, true, true, true, true, true, true, true, true},
			{false, false, false, false, false, true, true, true, true, true, true, true, true, true, true},
			{false, false, false, false, false, true, true, true, true, true, true, true, true, true, false},
			{false, false, false, false, false, true, true, true, true, true, true, true, true, true, false},
			{false, false, false, false, false, false, false, true, true, true, false, true, true, true, true},
			{false, false, false, false, false, false, false, false, false, false, false, true, false, false, false},
			{false, false, false, false, false, false, false, false, false, false, false, false, false, false, false},
			{false, false, false, false, false, false, false, false, false, false, false, false, false, false, false}
	});

	public static final SettingObject<Integer> COOLDOWN_CONFIG = abilitySettings.new SettingObject<Integer>(Nex.class, "cooldown", 120, "# 쿨타임") {

		@Override
		public boolean condition(Integer value) {
			return value >= 0;
		}

		@Override
		public String toString() {
			return Formatter.formatCooldown(getValue());
		}

	};

	public static final SettingObject<Integer> DamageConfig = abilitySettings.new SettingObject<Integer>(Nex.class, "damage", 20, "# 대미지") {

		@Override
		public boolean condition(Integer value) {
			return value >= 1;
		}

	};

	public Nex(Participant participant) {
		super(participant);
	}

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

	private final Cooldown cooldownTimer = new Cooldown(COOLDOWN_CONFIG.getValue());
	private final AbilityTimer fallBlockTimer = new AbilityTimer(5) {

		Location center;

		@Override
		public void onStart() {
			this.center = getPlayer().getLocation();
		}

		@SuppressWarnings("deprecation")
		@Override
		public void run(int count) {
			int distance = 6 - count;

			if (ServerVersion.getVersion() >= 13) {
				for (Block block : LocationUtil.getBlocks2D(center, distance, true, true, true)) {
					if (block.getType() == Material.AIR) block = block.getRelative(BlockFace.DOWN);
					if (block.getType() == Material.AIR) continue;
					Location location = block.getLocation().add(0, 1, 0);
					FallingBlocks.spawnFallingBlock(location, block.getType(), false, getPlayer().getLocation().toVector().subtract(location.toVector()).multiply(-0.1).setY(Math.random()), Behavior.FALSE);
				}
			} else {
				for (Block block : LocationUtil.getBlocks2D(center, distance, true, true, true)) {
					if (block.getType() == Material.AIR) block = block.getRelative(BlockFace.DOWN);
					if (block.getType() == Material.AIR) continue;
					Location location = block.getLocation().add(0, 1, 0);
					FallingBlocks.spawnFallingBlock(location, block.getType(), block.getData(), false, getPlayer().getLocation().toVector().subtract(location.toVector()).multiply(-0.1).setY(Math.random()), Behavior.FALSE);
				}
			}

			for (Damageable damageable : LocationUtil.getNearbyEntities(Damageable.class, center, 5, 5, predicate)) {
				if (!damageable.equals(getPlayer())) {
					damageable.setVelocity(center.toVector().subtract(damageable.getLocation().toVector()).multiply(-1).setY(1.2));
				}
			}
		}

	}.setPeriod(TimeUnit.TICKS, 4).register();
	private boolean noFallDamage = false;
	private boolean skillEnabled = false;
	private static final RGB COLOR_DARK = new RGB(38, 38, 38);
	private final AbilityTimer skill = new AbilityTimer(10) {

		@Override
		public void onStart() {
			noFallDamage = true;
			getPlayer().setVelocity(getPlayer().getVelocity().add(new Vector(0, 4, 0)));
		}

		@Override
		public void run(int count) {
			float yaw = getPlayer().getLocation().getYaw();
			for (Location loc : NEX_WING.getLeft().clone().rotateAroundAxisY(-yaw + 30).toLocations(getPlayer().getLocation().clone().subtract(0, 0.5, 0))) {
				ParticleLib.REDSTONE.spawnParticle(loc, COLOR_DARK);
			}
			for (Location loc : NEX_WING.getRight().clone().rotateAroundAxisY(-yaw - 30).toLocations(getPlayer().getLocation().clone().subtract(0, 0.5, 0))) {
				ParticleLib.REDSTONE.spawnParticle(loc, COLOR_DARK);
			}
		}

		@Override
		public void onEnd() {
			skillEnabled = true;
			getPlayer().setVelocity(getPlayer().getVelocity().add(getPlayer().getLocation().getDirection().normalize().multiply(8).setY(-4)));
			new AbilityTimer() {
				@Override
				protected void run(int count) {
					if (skillEnabled) {
						final Block blockHere = getPlayer().getLocation().getBlock(), blockBelow = blockHere.getRelative(BlockFace.DOWN);
						if (!blockHere.getType().equals(Material.AIR) || !blockBelow.getType().equals(Material.AIR)) {
							skillEnabled = false;
							ability();
							stop(false);
						}
						float yaw = getPlayer().getLocation().getYaw();
						for (Location loc : NEX_WING.getLeft().clone().rotateAroundAxisY(-yaw + 30).toLocations(getPlayer().getLocation().clone().subtract(0, 0.5, 0))) {
							ParticleLib.REDSTONE.spawnParticle(loc, COLOR_DARK);
						}
						for (Location loc : NEX_WING.getRight().clone().rotateAroundAxisY(-yaw - 30).toLocations(getPlayer().getLocation().clone().subtract(0, 0.5, 0))) {
							ParticleLib.REDSTONE.spawnParticle(loc, COLOR_DARK);
						}
					} else stop(false);
				}
			}.setPeriod(TimeUnit.TICKS, 4).start();
		}

	}.setPeriod(TimeUnit.TICKS, 4).register();

	@SubscribeEvent
	private void onEntityDamage(EntityDamageEvent e) {
		if (e.getEntity().equals(getPlayer()) && noFallDamage) {
			if (e.getCause().equals(DamageCause.FALL)) {
				e.setCancelled(true);
				noFallDamage = false;
			}
		}
	}

	@SubscribeEvent
	private void onPlayerMove(PlayerMoveEvent e) {
		if (e.getPlayer().equals(getPlayer())) {
			if (skillEnabled) {
				final Block blockHere = getPlayer().getLocation().getBlock(), blockBelow = blockHere.getRelative(BlockFace.DOWN);
				if (!blockHere.getType().equals(Material.AIR) || !blockBelow.getType().equals(Material.AIR)) {
					skillEnabled = false;
					ability();
				}
			}
		}
	}

	private void ability() {
		final double damage = DamageConfig.getValue();
		for (Damageable d : LocationUtil.getNearbyEntities(Damageable.class, getPlayer().getLocation(), 5, 5, predicate)) {
			if (d instanceof Player) SoundLib.ENTITY_GENERIC_EXPLODE.playSound((Player) d);
			d.damage(damage, getPlayer());
		}
		SoundLib.ENTITY_GENERIC_EXPLODE.playSound(getPlayer());

		fallBlockTimer.start();
	}

	@Override
	public boolean ActiveSkill(@NotNull Material material, @NotNull ClickType clickType) {
		if (material == Material.IRON_INGOT && clickType == ClickType.RIGHT_CLICK && !cooldownTimer.isCooldown()) {
			for (Player player : LocationUtil.getNearbyEntities(Player.class, getPlayer().getLocation(), 5, 5, null)) {
				SoundLib.ENTITY_WITHER_SPAWN.playSound(player);
			}
			SoundLib.ENTITY_WITHER_SPAWN.playSound(getPlayer());
			skill.start();
			cooldownTimer.start();
			return true;
		}

		return false;
	}

}
