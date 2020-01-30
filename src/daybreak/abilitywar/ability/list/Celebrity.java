package daybreak.abilitywar.ability.list;

import daybreak.abilitywar.ability.AbilityBase;
import daybreak.abilitywar.ability.AbilityManifest;
import daybreak.abilitywar.ability.AbilityManifest.Rank;
import daybreak.abilitywar.ability.AbilityManifest.Species;
import daybreak.abilitywar.config.ability.AbilitySettings.SettingObject;
import daybreak.abilitywar.game.games.mode.AbstractGame.Participant;
import daybreak.abilitywar.utils.Messager;
import daybreak.abilitywar.utils.library.BlockX;
import daybreak.abilitywar.utils.library.MaterialX;
import daybreak.abilitywar.utils.math.FastMath;
import daybreak.abilitywar.utils.math.LocationUtil;
import daybreak.abilitywar.utils.math.LocationUtil.Locations;
import daybreak.abilitywar.utils.math.geometry.Line;
import daybreak.abilitywar.utils.versioncompat.NMSUtil.EntityUtil;
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
import java.util.Map;
import java.util.Map.Entry;

@AbilityManifest(Name = "유명 인사", Rank = Rank.D, Species = Species.HUMAN)
public class Celebrity extends AbilityBase {

	public static final SettingObject<Integer> CooldownConfig = new SettingObject<Integer>(Celebrity.class, "Cooldown", 40,
			"# 쿨타임") {

		@Override
		public boolean Condition(Integer value) {
			return value >= 0;
		}

	};

	public static final SettingObject<Integer> DurationConfig = new SettingObject<Integer>(Celebrity.class, "Duration", 5,
			"# 쿨타임") {

		@Override
		public boolean Condition(Integer value) {
			return value >= 0;
		}

	};

	public Celebrity(Participant participant) {
		super(participant,
				ChatColor.translateAlternateColorCodes('&', "&f철괴를 우클릭하면 모든 플레이어가 자신의 방향을 바라봅니다. " + Messager.formatCooldown(CooldownConfig.getValue())));
	}

	private static final double radians = Math.toRadians(90);
	private final Map<Block, Material> carpets = new HashMap<>();
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
			new Timer(20) {
				@Override
				protected void onProcess(int count) {
					locations.add(direction);
					for (Location location : locations) {
						Block block = world.getBlockAt(
								location.getBlockX(),
								LocationUtil.getFloorYAt(world, playerLocation.getY(), location.getBlockX(), location.getBlockZ()),
								location.getBlockZ()
						);
						if (!carpets.containsKey(block)) {
							carpets.put(block, block.getType());
							BlockX.setType(block, MaterialX.RED_CARPET);
						}
					}
				}
			}.setPeriod(2).startTimer();
		}

		@Override
		protected void onDurationProcess(int seconds) {
			Block block = getPlayer().getLocation().getBlock();
			if (carpets.containsKey(block) || carpets.containsKey(block.getRelative(BlockFace.DOWN))) {
				for (LivingEntity entity : LocationUtil.getNearbyEntities(LivingEntity.class, getPlayer(), 10, 10)) {
					for (Player player : Bukkit.getOnlinePlayers()) {
						EntityUtil.rotateHead(player, entity, getPlayer().getEyeLocation().toVector().subtract(entity.getEyeLocation().toVector()));
					}
				}
			}
		}

		@Override
		protected void onDurationEnd() {
			for (Entry<Block, Material> entry : carpets.entrySet()) {
				entry.getKey().setType(entry.getValue());
			}
			carpets.clear();
		}
	}.setPeriod(1);

	@Override
	public boolean ActiveSkill(Material materialType, ClickType ct) {
		if (materialType.equals(Material.IRON_INGOT) && ct.equals(ClickType.RIGHT_CLICK) && !skillTimer.isDuration() && !cooldownTimer.isCooldown()) {
			skillTimer.startTimer();
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
