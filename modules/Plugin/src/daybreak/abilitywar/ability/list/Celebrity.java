package daybreak.abilitywar.ability.list;

import daybreak.abilitywar.ability.AbilityBase;
import daybreak.abilitywar.ability.AbilityManifest;
import daybreak.abilitywar.ability.AbilityManifest.Rank;
import daybreak.abilitywar.ability.AbilityManifest.Species;
import daybreak.abilitywar.ability.decorator.ActiveHandler;
import daybreak.abilitywar.config.ability.AbilitySettings.SettingObject;
import daybreak.abilitywar.game.games.mode.AbstractGame.Participant;
import daybreak.abilitywar.utils.base.Messager;
import daybreak.abilitywar.utils.base.concurrent.TimeUnit;
import daybreak.abilitywar.utils.base.minecraft.compat.block.BlockHandler;
import daybreak.abilitywar.utils.base.minecraft.compat.block.BlockSnapshot;
import daybreak.abilitywar.utils.base.minecraft.compat.nms.NMSHandler;
import daybreak.abilitywar.utils.library.BlockX;
import daybreak.abilitywar.utils.library.MaterialX;
import daybreak.abilitywar.utils.math.FastMath;
import daybreak.abilitywar.utils.math.LocationUtil;
import daybreak.abilitywar.utils.math.LocationUtil.Locations;
import daybreak.abilitywar.utils.math.geometry.Line;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@AbilityManifest(Name = "유명 인사", Rank = Rank.C, Species = Species.HUMAN)
public class Celebrity extends AbilityBase implements ActiveHandler {

	public static final SettingObject<Integer> CooldownConfig = new SettingObject<Integer>(Celebrity.class, "Cooldown", 40,
			"# 쿨타임") {

		@Override
		public boolean Condition(Integer value) {
			return value >= 0;
		}

	};

	public static final SettingObject<Double> DistanceConfig = new SettingObject<Double>(Celebrity.class, "Distance", 10.0,
			"# 능력 거리") {

		@Override
		public boolean Condition(Double value) {
			return value > 0;
		}

	};

	public static final SettingObject<Integer> DurationConfig = new SettingObject<Integer>(Celebrity.class, "Duration", 5,
			"# 쿨타임") {

		@Override
		public boolean Condition(Integer value) {
			return value >= 1;
		}

	};

	public Celebrity(Participant participant) {
		super(participant,
				ChatColor.translateAlternateColorCodes('&', "&f철괴를 우클릭하면 레드 카펫이 천천히 앞으로 나아가며 깔립니다. " + Messager.formatCooldown(CooldownConfig.getValue())),
				ChatColor.translateAlternateColorCodes('&', "&f능력으로 인해 깔린 레드 카펫 위에 있을 때 주변 " + DistanceConfig.getValue() + "칸 이내의 모든 생명체가"),
				ChatColor.translateAlternateColorCodes('&', "&f자신을 바라보며, 깔린 레드 카펫은 " + DurationConfig.getValue() + "초 후 사라집니다."));
	}

	private static final double radians = Math.toRadians(90);
	private final double distance = DistanceConfig.getValue();
	private final Map<Block, BlockSnapshot> carpets = new HashMap<>();
	private final CooldownTimer cooldownTimer = new CooldownTimer(CooldownConfig.getValue());
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
				Set<String> set = new HashSet<>();

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
				for (LivingEntity entity : LocationUtil.getNearbyEntities(LivingEntity.class, getPlayer(), distance, distance)) {
					for (Player player : Bukkit.getOnlinePlayers()) {
						Vector direction = getPlayer().getEyeLocation().toVector().subtract(entity.getEyeLocation().toVector());
						NMSHandler.getNMS().rotateHead(player, entity, LocationUtil.getYaw(direction), LocationUtil.getPitch(direction));
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

	@Override
	public void TargetSkill(Material materialType, LivingEntity entity) {
	}

	private double rotateX(double x, double z, double radians) {
		return (x * FastMath.cos(radians)) + (z * FastMath.sin(radians));
	}

	private double rotateZ(double x, double z, double radians) {
		return (-x * FastMath.sin(radians)) + (z * FastMath.cos(radians));
	}

}
