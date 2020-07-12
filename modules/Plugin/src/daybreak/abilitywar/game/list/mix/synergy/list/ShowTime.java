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
import daybreak.abilitywar.utils.base.math.FastMath;
import daybreak.abilitywar.utils.base.math.LocationUtil;
import daybreak.abilitywar.utils.base.math.LocationUtil.Locations;
import daybreak.abilitywar.utils.base.math.NumberUtil;
import daybreak.abilitywar.utils.base.math.geometry.Circle;
import daybreak.abilitywar.utils.base.math.geometry.Line;
import daybreak.abilitywar.utils.base.minecraft.FireworkUtil;
import daybreak.abilitywar.utils.base.minecraft.compat.block.BlockHandler;
import daybreak.abilitywar.utils.base.minecraft.compat.block.BlockSnapshot;
import daybreak.abilitywar.utils.base.minecraft.compat.nms.NMSHandler;
import daybreak.abilitywar.utils.base.minecraft.version.ServerVersion.Version;
import daybreak.abilitywar.utils.library.BlockX;
import daybreak.abilitywar.utils.library.MaterialX;
import daybreak.abilitywar.utils.library.ParticleLib;
import daybreak.abilitywar.utils.library.ParticleLib.RGB;
import daybreak.abilitywar.utils.library.PotionEffects;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.FireworkEffect.Type;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.attribute.Attribute;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Firework;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.FireworkExplodeEvent;
import org.bukkit.util.Vector;

@AbilityManifest(name = "쇼 타임", rank = Rank.A, species = Species.HUMAN, explain = {
		"철괴를 우클릭하면 레드 카펫이 천천히 앞으로 나아가며 깔립니다. $[COOLDOWN_CONFIG]",
		"능력으로 인해 깔린 레드 카펫 위에 있을 때 같은 월드의 모든 생명체가",
		"자신을 바라보며, 깔린 레드 카펫은 $[DurationConfig]초 후 사라집니다.",
		"주변 7칸 이내에 있는 생명체 수에 따라 효과를 받으며,",
		"플레이어는 1명, 플레이어가 아닌 생명체는 0.2명 취급합니다.",
		"§a0명 이상, 2명 미만 §7: §f나약함  §a2명 이상, 4명 미만 §7: §f힘 II",
		"§a4명 이상 §7: §f힘 III 및 체력이 30% 미만인 적 처형",
		"능력 사용 중에는 주변의 생명체 수와 관계 없이 4명 이상의 효과를 받습니다."
})
@Support(min = Version.v1_11_R1)
public class ShowTime extends Synergy implements ActiveHandler {

	public static final SettingObject<Integer> COOLDOWN_CONFIG = synergySettings.new SettingObject<Integer>(ShowTime.class, "Cooldown", 40,
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
	public static final SettingObject<Integer> DurationConfig = synergySettings.new SettingObject<Integer>(ShowTime.class, "Duration", 10,
			"# 쿨타임") {

		@Override
		public boolean condition(Integer value) {
			return value >= 1;
		}

	};
	private static final double radians = Math.toRadians(90);
	private static final Color[] colors = {
			Color.YELLOW, Color.RED, Color.ORANGE, Color.WHITE, Color.FUCHSIA
	};
	private static final Type[] types = {
			Type.BALL_LARGE, Type.STAR
	};
	private static final int radius = 7;
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
				for (LivingEntity entity : getPlayer().getWorld().getLivingEntities()) {
					if (getPlayer().equals(entity)) continue;
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
	private final RGB WEAK = new RGB(214, 255, 212);
	private final RGB POWER = new RGB(255, 184, 150);
	private final RGB POWERFUL = new RGB(255, 59, 59);
	private final Circle circle = Circle.of(radius, 100);
	private final Map<Firework, LivingEntity> execution = new HashMap<>();
	private final Predicate<Entity> strictPredicate = new Predicate<Entity>() {
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
	private final Predicate<Entity> unequalPredicate = new Predicate<Entity>() {
		@Override
		public boolean test(Entity entity) {
			if (entity.equals(getPlayer())) return false;
			return (!(entity instanceof Player)) || (getGame().isParticipating(entity.getUniqueId())
					&& (!(getGame() instanceof DeathManager.Handler) || !((DeathManager.Handler) getGame()).getDeathManager().isExcluded(entity.getUniqueId()))
					&& getGame().getParticipant(entity.getUniqueId()).attributes().TARGETABLE.getValue());
		}
	};

	private final Timer passive = new Timer() {

		@Override
		public void run(int count) {
			final double point = getPoint(7, 7);
			final RGB color;
			Location playerLocation = getPlayer().getLocation();
			if (point < 2) {
				PotionEffects.WEAKNESS.addPotionEffect(getPlayer(), 4, 0, true);
				color = WEAK;
			} else if (point >= 2 && point < 4) {
				PotionEffects.INCREASE_DAMAGE.addPotionEffect(getPlayer(), 4, 1, true);
				color = POWER;
			} else {
				PotionEffects.INCREASE_DAMAGE.addPotionEffect(getPlayer(), 4, 2, true);
				color = POWERFUL;
				for (LivingEntity livingEntity : LocationUtil.getEntitiesInCircle(LivingEntity.class, playerLocation, radius, strictPredicate)) {
					if (livingEntity.getHealth() < (livingEntity.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue() / 3.3333333333) && !livingEntity.isDead()) {
						if (!execution.containsValue(livingEntity)) {
							Firework firework = FireworkUtil.spawnRandomFirework(livingEntity.getEyeLocation().clone().add(0, 0.5, 0), colors, colors, types, 1);
							firework.addPassenger(livingEntity);
							execution.put(firework, livingEntity);
						}
					}
				}
			}

			for (Location loc : circle.toLocations(playerLocation).floor(playerLocation.getY())) {
				ParticleLib.REDSTONE.spawnParticle(getPlayer(), loc, color);
			}
		}

	}.setPeriod(TimeUnit.TICKS, 1);

	@Override
	protected void onUpdate(Update update) {
		if (update == Update.RESTRICTION_CLEAR) {
			passive.start();
		}
	}

	public ShowTime(Participant participant) {
		super(participant);
	}

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

	@SubscribeEvent
	private void onFireworkExplode(FireworkExplodeEvent e) {
		if (execution.containsKey(e.getEntity())) {
			Firework firework = e.getEntity();
			LivingEntity livingEntity = execution.get(firework);
			if (!livingEntity.isDead()) {
				livingEntity.damage(0, getPlayer());
				livingEntity.getWorld().createExplosion(livingEntity.getLocation(), 2);
				if (!livingEntity.isDead()) livingEntity.setHealth(0);
				Location location = livingEntity.getEyeLocation().clone().add(0, 0.5, 0);
				for (int i = 0; i < 4; i++) FireworkUtil.spawnRandomFirework(location, colors, colors, types, 1);
			}
			execution.remove(firework);
		}
	}

	private double getPoint(int horizontal, int vertical) {
		Location center = getPlayer().getLocation();
		double centerX = center.getX(), centerZ = center.getZ();
		double point = 0;
		for (Entity entity : LocationUtil.collectEntities(center, horizontal)) {
			Location entityLocation = entity.getLocation();
			if (LocationUtil.distanceSquared2D(centerX, centerZ, entityLocation.getX(), entityLocation.getZ()) <= (horizontal * horizontal) && NumberUtil.subtract(center.getY(), entityLocation.getY()) <= vertical && (unequalPredicate == null || unequalPredicate.test(entity))) {
				if (entity instanceof Player) {
					point += 1.0;
				} else if (entity instanceof LivingEntity) {
					point += 0.2;
				}
			}
		}
		if (skillTimer.isRunning()) point += 4.0;
		return point;
	}

}
