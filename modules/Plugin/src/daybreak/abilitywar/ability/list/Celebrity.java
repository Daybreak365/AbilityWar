package daybreak.abilitywar.ability.list;

import daybreak.abilitywar.ability.AbilityBase;
import daybreak.abilitywar.ability.AbilityManifest;
import daybreak.abilitywar.ability.AbilityManifest.Rank;
import daybreak.abilitywar.ability.AbilityManifest.Species;
import daybreak.abilitywar.ability.Tips;
import daybreak.abilitywar.ability.Tips.Description;
import daybreak.abilitywar.ability.Tips.Difficulty;
import daybreak.abilitywar.ability.Tips.Level;
import daybreak.abilitywar.ability.Tips.Stats;
import daybreak.abilitywar.ability.decorator.ActiveHandler;
import daybreak.abilitywar.config.ability.AbilitySettings.SettingObject;
import daybreak.abilitywar.game.AbstractGame.Participant;
import daybreak.abilitywar.game.manager.object.DeathManager;
import daybreak.abilitywar.game.team.interfaces.Teamable;
import daybreak.abilitywar.utils.base.Formatter;
import daybreak.abilitywar.utils.base.concurrent.TimeUnit;
import daybreak.abilitywar.utils.base.math.FastMath;
import daybreak.abilitywar.utils.base.math.LocationUtil;
import daybreak.abilitywar.utils.base.math.LocationUtil.Locations;
import daybreak.abilitywar.utils.base.math.geometry.Line;
import daybreak.abilitywar.utils.base.minecraft.block.Blocks;
import daybreak.abilitywar.utils.base.minecraft.block.IBlockSnapshot;
import daybreak.abilitywar.utils.base.minecraft.nms.NMS;
import daybreak.abilitywar.utils.library.BlockX;
import daybreak.abilitywar.utils.library.MaterialX;
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
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;

@AbilityManifest(name = "유명 인사", rank = Rank.C, species = Species.HUMAN, explain = {
		"철괴를 우클릭하면 레드 카펫이 천천히 앞으로 나아가며 깔립니다. $[COOLDOWN_CONFIG]",
		"능력 사용 중 레드 카펫 위에 있으면 주변 $[DistanceConfig]칸 이내의 모든 생명체가",
		"자신을 바라보며, 깔린 레드 카펫은 $[DurationConfig]초 후 사라집니다."
})
@Tips(tip = {
		"주위 사람들의 관심을 끌기 딱 좋은 능력입니다.",
		"도망가는 상대가 나만 바라보도록 해 도망가기 어렵게 하는 플레이를",
		"펼칠 수도 있습니다. 만약 팀전을 플레이 중이라면, 모든 적이 나를",
		"바라보게 해 팀원들을 지키는 탱킹 플레이를 할 수도 있습니다."
}, strong = {
		@Description(subject = "관심 받기", explain = {
				"상대가 나에게 싸움 중이더라도, 도망가는 중이더라도 관심을",
				"손쉽게 받을 수 있습니다. 마음 속을 따듯한 관심으로 채우세요!"
		})
}, weak = {
		@Description(subject = "공격 노출", explain = {
				"능력을 사용하는 순간 주위의 모든 플레이어가 나를 바라보게 되며,",
				"모두가 당신을 공격하게 될 것입니다."
		})
}, stats = @Stats(offense = Level.ZERO, survival = Level.ZERO, crowdControl = Level.SIX, mobility = Level.ZERO, utility = Level.FOUR), difficulty = Difficulty.EASY)
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
				if (getGame() instanceof Teamable) {
					final Teamable teamGame = (Teamable) getGame();
					final Participant entityParticipant = teamGame.getParticipant(entity.getUniqueId()), participant = getParticipant();
					return !teamGame.hasTeam(entityParticipant) || !teamGame.hasTeam(participant) || (!teamGame.getTeam(entityParticipant).equals(teamGame.getTeam(participant)));
				}
			}
			return true;
		}
	};

	private final double distance = DistanceConfig.getValue();
	private final Map<Block, IBlockSnapshot> carpets = new HashMap<>();
	private final Cooldown cooldownTimer = new Cooldown(COOLDOWN_CONFIG.getValue());
	private final Duration skillTimer = new Duration(DurationConfig.getValue() * 20, cooldownTimer) {
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
			new AbilityTimer(30) {
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
								carpets.put(block, Blocks.createSnapshot(block));
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
			for (IBlockSnapshot snapshot : carpets.values()) {
				snapshot.apply();
			}
			carpets.clear();
		}

		@Override
		protected void onDurationSilentEnd() {
			for (IBlockSnapshot snapshot : carpets.values()) {
				snapshot.apply();
			}
			carpets.clear();
		}
	}.setPeriod(TimeUnit.TICKS, 1);

	@Override
	public boolean ActiveSkill(@NotNull Material material, @NotNull ClickType clickType) {
		if (material == Material.IRON_INGOT && clickType == ClickType.RIGHT_CLICK && !skillTimer.isDuration() && !cooldownTimer.isCooldown()) {
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
