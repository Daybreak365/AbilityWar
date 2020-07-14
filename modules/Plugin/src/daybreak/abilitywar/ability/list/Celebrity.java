package daybreak.abilitywar.ability.list;

import daybreak.abilitywar.ability.AbilityBase;
import daybreak.abilitywar.ability.AbilityManifest;
import daybreak.abilitywar.ability.AbilityManifest.Rank;
import daybreak.abilitywar.ability.AbilityManifest.Species;
import daybreak.abilitywar.ability.decorator.ActiveHandler;
import daybreak.abilitywar.config.ability.AbilitySettings.SettingObject;
import daybreak.abilitywar.game.AbstractGame.Participant;
import daybreak.abilitywar.game.interfaces.TeamGame;
import daybreak.abilitywar.game.manager.object.DeathManager;
import daybreak.abilitywar.utils.base.Formatter;
import daybreak.abilitywar.utils.base.concurrent.TimeUnit;
import daybreak.abilitywar.utils.base.math.FastMath;
import daybreak.abilitywar.utils.base.math.LocationUtil;
import daybreak.abilitywar.utils.base.math.LocationUtil.Locations;
import daybreak.abilitywar.utils.base.math.geometry.Line;
import daybreak.abilitywar.utils.base.minecraft.compat.block.BlockHandler;
import daybreak.abilitywar.utils.base.minecraft.compat.block.BlockSnapshot;
import daybreak.abilitywar.utils.base.minecraft.compat.nms.NMS;
import daybreak.abilitywar.utils.library.BlockX;
import daybreak.abilitywar.utils.library.MaterialX;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

@AbilityManifest(name = "유명 인사", rank = Rank.C, species = Species.HUMAN, explain = {
		"철괴를 우클릭하면 레드 카펫이 천천히 앞으로 나아가며 깔립니다. $[COOLDOWN_CONFIG]",
		"능력으로 인해 깔린 레드 카펫 위에 있을 때 주변 $[DistanceConfig]칸 이내의 모든 생명체가",
		"자신을 바라보며, 깔린 레드 카펫은 $[DurationConfig]초 후 사라집니다."
})
public class Celebrity extends AbilityBase implements ActiveHandler {

	public static final SettingObject<Integer> COOLDOWN_CONFIG = abilitySettings.new SettingObject<Integer>(Celebrity.class, "Cooldown", 40,
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

	public static final SettingObject<Double> DistanceConfig = abilitySettings.new SettingObject<Double>(Celebrity.class, "Distance", 10.0,
			"# 능력 거리") {

		@Override
		public boolean condition(Double value) {
			return value > 0;
		}

	};

	public static final SettingObject<Integer> DurationConfig = abilitySettings.new SettingObject<Integer>(Celebrity.class, "Duration", 5,
			"# 쿨타임") {

		@Override
		public boolean condition(Integer value) {
			return value >= 1;
		}

	};

	public Celebrity(Participant participant) {
		super(participant);
	}

	private static final double radians = Math.toRadians(90);

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

	private final double distance = DistanceConfig.getValue();
	private final Map<Block, BlockSnapshot> carpets = new HashMap<>();
	private final CooldownTimer cooldownTimer = new CooldownTimer(COOLDOWN_CONFIG.getValue());
	private final DurationTimer skillTimer = new DurationTimer(DurationConfig.getValue() * 20, cooldownTimer) {
		@Override
		protected void onDurationStart() {
			final World world = getPlayer().getWorld();
			final Location playerLocation = getPlayer().getLocation();
			Vector direction = playerLocation.getDirection().clone().normalize();
			Locations locations = new Locations();
			for (Vector vector : Line.between(playerLocation, playerLocation.clone().add(direction), 2)) {
				final double originX = vector.getX();
				final double originZ = vector.getZ();
				locations.add(playerLocation.clone().add(vector.clone()
						.setX(rotateX(originX, originZ, radians))
						.setZ(rotateZ(originX, originZ, radians))));
				locations.add(playerLocation.clone().add(vector.clone()
						.setX(rotateX(originX, originZ, radians * 3))
						.setZ(rotateZ(originX, originZ, radians * 3))));
			}
			direction.multiply(0.75);
			new Timer(30) {
				final Set<String> set = new HashSet<>();

				@Override
				protected void run(int count) {
					locations.add(direction);
					for (Location location : locations) {
						if (set.add(location.getBlockX() + ":" + location.getBlockZ())) {
							Block block = world.getBlockAt(
									location.getBlockX(),
									LocationUtil.getFloorYAt(world, playerLocation.getY(), location.getBlockX(), location.getBlockZ()),
									location.getBlockZ()
							);
							if (!carpets.containsKey(block)) {
								carpets.put(block, BlockHandler.createSnapshot(block));
								BlockX.setType(block, MaterialX.RED_CARPET);
							}
						}
					}
				}
			}.setPeriod(TimeUnit.TICKS, 2).start();
		}

		@Override
		protected void onDurationProcess(int seconds) {
			Block block = getPlayer().getLocation().getBlock();
			if (carpets.containsKey(block) || carpets.containsKey(block.getRelative(BlockFace.DOWN))) {
				for (LivingEntity entity : LocationUtil.getNearbyEntities(LivingEntity.class, getPlayer().getLocation(), distance, distance, predicate)) {
					for (Player player : Bukkit.getOnlinePlayers()) {
						Vector direction = getPlayer().getEyeLocation().toVector().subtract(entity.getEyeLocation().toVector());
						NMS.rotateHead(player, entity, LocationUtil.getYaw(direction), LocationUtil.getPitch(direction));
					}
				}
			}
		}

		@Override
		protected void onDurationEnd() {
			for (BlockSnapshot snapshot : carpets.values()) {
				snapshot.apply();
			}
			carpets.clear();
		}

		@Override
		protected void onDurationSilentEnd() {
			for (BlockSnapshot snapshot : carpets.values()) {
				snapshot.apply();
			}
			carpets.clear();
		}
	}.setPeriod(TimeUnit.TICKS, 1);

	@Override
	public boolean ActiveSkill(Material materialType, ClickType clickType) {
		if (materialType.equals(Material.IRON_INGOT) && clickType.equals(ClickType.RIGHT_CLICK) && !skillTimer.isDuration() && !cooldownTimer.isCooldown()) {
			skillTimer.start();
			return true;
		}
		return false;
	}

	private double rotateX(double x, double z, double radians) {
		return (x * FastMath.cos(radians)) + (z * FastMath.sin(radians));
	}

	private double rotateZ(double x, double z, double radians) {
		return (-x * FastMath.sin(radians)) + (z * FastMath.cos(radians));
	}

}
