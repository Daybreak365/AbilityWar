package daybreak.abilitywar.utils.base.math;

import daybreak.abilitywar.game.AbstractGame;
import daybreak.abilitywar.game.AbstractGame.CustomEntity;
import daybreak.abilitywar.game.AbstractGame.Participant;
import daybreak.abilitywar.game.GameManager;
import daybreak.abilitywar.game.interfaces.TeamGame;
import daybreak.abilitywar.game.manager.object.DeathManager;
import daybreak.abilitywar.utils.base.math.geometry.Boundary;
import daybreak.abilitywar.utils.base.math.geometry.Boundary.BoundingBox;
import daybreak.abilitywar.utils.base.math.geometry.Boundary.CenteredBoundingBox;
import daybreak.abilitywar.utils.base.minecraft.version.ServerVersion;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.function.Predicate;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Damageable;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.BlockIterator;
import org.bukkit.util.Vector;

/**
 * Location Util
 *
 * @author Daybreak 새벽
 */
public class LocationUtil {

	private LocationUtil() {
	}

	private static final double TWO_PI = 6.283185307179586D;

	public static float getYaw(Vector vector) {
		return (float) Math.toDegrees((Math.atan2(-vector.getX(), vector.getZ()) + TWO_PI) % TWO_PI);
	}

	public static float getPitch(Vector vector) {
		double x = vector.getX();
		double z = vector.getZ();
		if (x == 0.0D && z == 0.0D) {
			return vector.getY() > 0.0D ? -90 : 90;
		} else {
			return (float) Math.toDegrees(Math.atan(-vector.getY() / Math.sqrt((x * x) + (z * z))));
		}
	}

	/**
	 * 평면상에서 두 좌표의 거리의 제곱을 구합니다.
	 *
	 * @param ax 첫번째 X 좌표
	 * @param az 첫번째 Z 좌표
	 * @param bx 두번째 X 좌표
	 * @param bz 두번째 Z 좌표
	 * @return 두 좌표의 거리의 제곱
	 */
	public static double distanceSquared2D(double ax, double az, double bx, double bz) {
		return ((bx - ax) * (bx - ax)) + ((bz - az) * (bz - az));
	}

	/**
	 * {@link Location}이 범위 안에 있는지 확인합니다.
	 *
	 * @param center   중심
	 * @param location 확인할 위치
	 * @param radius   원의 반지름
	 */
	public static boolean isInCircle(Location center, Location location, double radius) {
		return center.getWorld().equals(location.getWorld()) && distanceSquared2D(center.getX(), center.getZ(), location.getX(), location.getZ()) <= (radius * radius);
	}

	public static <T extends Entity> List<T> getEntitiesInCircle(Class<T> entityType, Location center, double radius, Predicate<Entity> predicate) {
		double centerX = center.getX(), centerZ = center.getZ(), SQUARED_RADIUS = radius * radius;
		List<T> entities = new ArrayList<>();
		for (Entity e : collectEntities(center, (int) Math.floor(radius))) {
			if (entityType.isAssignableFrom(e.getClass())) {
				@SuppressWarnings("unchecked") T entity = (T) e;
				Location entityLocation = entity.getLocation();
				if (distanceSquared2D(centerX, centerZ, entityLocation.getX(), entityLocation.getZ()) <= SQUARED_RADIUS && (predicate == null || predicate.test(entity))) {
					entities.add(entity);
				}
			}
		}
		return entities;
	}

	@SuppressWarnings("unchecked")
	public static <T extends Entity> T getEntityLookingAt(Class<T> entityType, CenteredBoundingBox boundingBox, LivingEntity criterion, int maxDistance, Predicate<Entity> predicate) {
		if (criterion == null || maxDistance <= 0) return null;
		World world = criterion.getWorld();
		Iterator<Block> iterator = new BlockIterator(criterion, maxDistance);
		while (iterator.hasNext()) {
			Block block = iterator.next();
			if (block.getType().isOccluding()) return null;
			boundingBox.setCenter(block.getLocation());
			Chunk blockChunk = block.getChunk();
			int blockChunkX = blockChunk.getX(), blockChunkZ = blockChunk.getZ();
			for (int x = blockChunkX - 1; x <= blockChunkX + 1; x++) {
				for (int z = blockChunkZ - 1; z <= blockChunkZ + 1; z++) {
					Chunk chunk = world.getChunkAt(x, z);
					for (Entity e : chunk.getEntities()) {
						if (!criterion.equals(e) && entityType.isAssignableFrom(e.getClass())) {
							Boundary.BoundaryData boundaryData = Boundary.BoundaryData.of(e.getType());
							Location entityLocation = e.getLocation();
							double entityX = entityLocation.getX(), entityY = entityLocation.getY(), entityZ = entityLocation.getZ();
							if (entityX + boundaryData.getMinX() < boundingBox.getMaxX() && boundingBox.getMinX() < entityX + boundaryData.getMaxX() && entityY + boundaryData.getMinY() < boundingBox.getMaxY() &&
									boundingBox.getMinY() < entityY + boundaryData.getMaxY() && entityZ + boundaryData.getMinZ() < boundingBox.getMaxZ() && boundingBox.getMinZ() < entityZ + boundaryData.getMaxZ() &&
									(predicate == null || predicate.test(e))) {
								return (T) e;
							}
						}
					}
				}
			}
		}
		return null;
	}

	@SuppressWarnings("unchecked")
	public static <T extends Entity> T getEntityLookingAt(Class<T> entityType, LivingEntity criterion, int maxDistance, Predicate<Entity> predicate) {
		return getEntityLookingAt(entityType, CenteredBoundingBox.of(null, 0, 0, 0, 1, 1, 1), criterion, maxDistance, predicate);
	}

	@SuppressWarnings("unchecked")
	public static <T> T getCustomEntityLookingAt(Class<T> entityType, AbstractGame game, CenteredBoundingBox boundingBox, LivingEntity criterion, int maxDistance, Predicate<CustomEntity> predicate) {
		if (criterion == null || maxDistance <= 0) return null;
		World world = criterion.getWorld();
		Iterator<Block> iterator = new BlockIterator(criterion, maxDistance);
		while (iterator.hasNext()) {
			Block block = iterator.next();
			if (block.getType().isOccluding()) return null;
			boundingBox.setCenter(block.getLocation());
			Chunk blockChunk = block.getChunk();
			int blockChunkX = blockChunk.getX(), blockChunkZ = blockChunk.getZ();
			for (int x = blockChunkX - 1; x <= blockChunkX + 1; x++) {
				for (int z = blockChunkZ - 1; z <= blockChunkZ + 1; z++) {
					Chunk chunk = world.getChunkAt(x, z);
					for (CustomEntity entity : game.getCustomEntities(chunk)) {
						if (entityType.isAssignableFrom(entity.getClass())) {
							BoundingBox entityBox = entity.getBoundingBox();
							if (entityBox.getMinX() < boundingBox.getMaxX() && boundingBox.getMinX() < entityBox.getMaxX() && entityBox.getMinY() < boundingBox.getMaxY() &&
									boundingBox.getMinY() < entityBox.getMaxY() && entityBox.getMinZ() < boundingBox.getMaxZ() && boundingBox.getMinZ() < entityBox.getMaxZ() &&
									(predicate == null || predicate.test(entity))) {
								return (T) entity;
							}
						}
					}
				}
			}
		}
		return null;
	}

	public static int getFloorYAt(World world, double referenceY, int x, int z) {
		int y = getHighestBlockYAt(world, x, z);
		if (y > referenceY) {
			for (int yCheck = y; yCheck >= referenceY; yCheck--) {
				if (world.getBlockAt(x, yCheck, z).isEmpty()) y = yCheck;
			}
			if (world.getBlockAt(x, y - 1, z).isEmpty()) {
				while (world.getBlockAt(x, y - 1, z).isEmpty() && y >= 0) y--;
			}
		}
		return y;
	}

	public static int getHighestBlockYAt(World world, int x, int z) {
		return ServerVersion.getVersionNumber() >= 15 ? (world.getHighestBlockYAt(x, z) + 1) : world.getHighestBlockYAt(x, z);
	}

	public static int getHighestBlockYAt(World world, Location location) {
		return ServerVersion.getVersionNumber() >= 15 ? (world.getHighestBlockYAt(location) + 1) : world.getHighestBlockYAt(location);
	}

	/**
	 * 3차원 공간에서 범위 안에 있는 블록들을 {@link ArrayList}로  반환합니다.
	 *
	 * @param center     중심
	 * @param radius     범위
	 * @param hollow     참일 경우 바깥 부분의 블록들만 가져옵니다.
	 * @param includeAir 참일 경우 블록이 비어있어도 가져옵니다.
	 */
	public static List<Block> getBlocks3D(Location center, int radius, boolean hollow, boolean includeAir) {
		List<Block> blocks = new ArrayList<>();
		final int blockX = center.getBlockX(), blockY = center.getBlockY(), blockZ = center.getBlockZ();
		for (int x = blockX - radius; x <= blockX + radius; x++) {
			for (int y = blockY - radius; y <= blockY + radius; y++) {
				for (int z = blockZ - radius; z <= blockZ + radius; z++) {
					Block block = center.getWorld().getBlockAt(x, y, z);
					double distanceSquared = center.distanceSquared(block.getLocation());
					if (distanceSquared <= (radius * radius) && !(hollow && distanceSquared < ((radius - 1) * (radius - 1)))) {
						if (block.isEmpty()) {
							if (includeAir) {
								blocks.add(block);
							}
						} else {
							blocks.add(block);
						}
					}
				}
			}
		}
		return blocks;
	}

	/**
	 * 평면상에서 범위 안에 있는 블록들을 {@link ArrayList}로  반환합니다.
	 *
	 * @param center        중심
	 * @param radius        범위
	 * @param hollow        참일 경우 바깥 부분의 블록들만 가져옵니다.
	 * @param highestBlocks 참일 경우 각 위치에서 가장 높은 위치에 있는 블록들을 가져옵니다. 이 경우 모든 블록이 같은 평면 위에 있지 않을 수 있습니다.
	 */
	public static List<Block> getBlocks2D(Location center, int radius, boolean hollow, boolean highestBlocks, boolean includeAir) {
		List<Block> blocks = new ArrayList<>();
		final int blockX = center.getBlockX(), blockZ = center.getBlockZ();
		for (int x = blockX - radius; x <= blockX + radius; x++) {
			for (int z = blockZ - radius; z <= blockZ + radius; z++) {
				Block block = highestBlocks ? center.getWorld().getHighestBlockAt(x, z) : center.getWorld().getBlockAt(x, center.getBlockY(), z);
				Location compare = center.clone();
				compare.setY(0);
				Location blockCompare = block.getLocation().clone();
				blockCompare.setY(0);
				double distance = compare.distanceSquared(blockCompare);
				if (distance <= (radius * radius) && !(hollow && distance < ((radius - 1) * (radius - 1)))) {
					if (block.isEmpty()) {
						if (includeAir) {
							blocks.add(block);
						}
					} else {
						blocks.add(block);
					}
				}
			}
		}
		return blocks;
	}

	public static Locations getRandomLocations(Random random, Location center, double radius, int amount) {
		Locations locations = new Locations(amount);
		for (int i = 0; i < amount; i++) {
			double radians = Math.toRadians(random.nextDouble() * 360);
			double x = center.getX() + (random.nextDouble() * radius * FastMath.cos(radians));
			double z = center.getZ() + (random.nextDouble() * radius * FastMath.sin(radians));
			locations.add(new Location(center.getWorld(), x, getHighestBlockYAt(center.getWorld(), (int) x, (int) z), z));
		}
		return locations;
	}

	public static Locations getRandomLocations(Location center, double radius, int amount) {
		return getRandomLocations(new Random(), center, radius, amount);
	}

	/**
	 * 중점에서 가장 가까이에 있는 특정 타입의 엔티티를 반환합니다.
	 * 가장 가까이에 있는 특정 타입의 엔티티를 찾을 수 없을 경우 null을 반환합니다.
	 *
	 * @param entityType 탐색할 엔티티 타입
	 * @param center     중점
	 * @param predicate  커스텀 조건
	 * @return 중점에서 가장 가까이에 있는 특정 타입의 엔티티
	 */
	@SuppressWarnings("unchecked")
	public static <T extends Entity> T getNearestEntity(Class<T> entityType, Location center, Predicate<Entity> predicate) {
		double distance = Double.MAX_VALUE;
		T current = null;

		final Location centerLocation = center.clone();
		for (Entity e : center.getWorld().getEntities()) {
			if (entityType.isAssignableFrom(e.getClass())) {
				@SuppressWarnings("unchecked") T entity = (T) e;
				double compare = centerLocation.distanceSquared(entity.getLocation());
				if (compare < distance && (predicate == null || predicate.test(entity))) {
					distance = compare;
					current = entity;
				}
			}
		}

		return current;
	}

	/**
	 * 일정 범위 내에 있는 청크들의 엔티티 목록을 반환합니다.
	 *
	 * @param center     중점
	 * @param horizontal 수평 거리
	 * @return 엔티티 목록
	 */
	public static Entity[] collectEntities(Location center, int horizontal) {
		Entity[] entities = new Entity[0];
		World world = center.getWorld();
		final int maxX = (center.getBlockX() + horizontal) >> 4, maxZ = (center.getBlockZ() + horizontal) >> 4;
		for (int x = (center.getBlockX() - horizontal) >> 4; x <= maxX; x++) {
			for (int z = (center.getBlockZ() - horizontal) >> 4; z <= maxZ; z++) {
				Entity[] chunkEntities = world.getChunkAt(x, z).getEntities();
				Entity[] newEntities = new Entity[entities.length + chunkEntities.length];
				System.arraycopy(entities, 0, newEntities, 0, entities.length);
				System.arraycopy(chunkEntities, 0, newEntities, entities.length, chunkEntities.length);
				entities = newEntities;
			}
		}
		return entities;
	}

	/**
	 * 일정 범위 내에 있는 청크들의 커스텀 엔티티 목록을 반환합니다.
	 *
	 * @param center     중점
	 * @param horizontal 수평 거리
	 * @return 엔티티 목록
	 */
	public static List<CustomEntity> collectCustomEntities(Location center, int horizontal) {
		final List<CustomEntity> entities = new ArrayList<>();
		if (GameManager.isGameRunning()) {
			World world = center.getWorld();
			final int maxX = (center.getBlockX() + horizontal) >> 4, maxZ = (center.getBlockZ() + horizontal) >> 4;
			for (int x = (center.getBlockX() - horizontal) >> 4; x <= maxX; x++) {
				for (int z = (center.getBlockZ() - horizontal) >> 4; z <= maxZ; z++) {
					entities.addAll(GameManager.getGame().getCustomEntities(world.getChunkAt(x, z)));
				}
			}
		}
		return entities;
	}

	/**
	 * 일정 범위 내에 있는 청크들의 엔티티 목록을 반환합니다.
	 *
	 * @param center 중심 청크
	 * @param minX   엔티티 목록을 확인할 청크들의 X 좌표 중 가장 작은 값 (중심 청크 기준)
	 * @param minZ   엔티티 목록을 확인할 청크들의 Z 좌표 중 가장 작은 값 (중심 청크 기준)
	 * @param maxX   엔티티 목록을 확인할 청크들의 X 좌표 중 가장 큰 값 (중심 청크 기준)
	 * @param maxZ   엔티티 목록을 확인할 청크들의 Z 좌표 중 가장 큰 값 (중심 청크 기준)
	 * @return 엔티티 목록
	 */
	public static Entity[] collectEntities(Chunk center, int minX, int minZ, int maxX, int maxZ) {
		Entity[] entities = new Entity[0];
		World world = center.getWorld();
		for (int x = minX; x <= maxX; x++) {
			for (int z = minZ; z <= maxZ; z++) {
				Entity[] chunkEntities = world.getChunkAt(x, z).getEntities();
				Entity[] newEntities = new Entity[entities.length + chunkEntities.length];
				System.arraycopy(entities, 0, newEntities, 0, entities.length);
				System.arraycopy(chunkEntities, 0, newEntities, entities.length, chunkEntities.length);
				entities = newEntities;
			}
		}
		return entities;
	}

	/**
	 * 주변에 있는 특정 타입의 엔티티 목록을 반환합니다.
	 *
	 * @param entityType 탐색할 엔티티 타입
	 * @param center     중점
	 * @param horizontal 수평 거리
	 * @param vertical   수직 거리
	 * @param predicate  커스텀 조건
	 * @return 주변에 있는 특정 타입의 엔티티 목록
	 */
	public static <T extends Entity> ArrayList<T> getNearbyEntities(Class<T> entityType, Location center, double horizontal, double vertical, Predicate<Entity> predicate) {
		double centerX = center.getX(), centerZ = center.getZ();
		ArrayList<T> entities = new ArrayList<>();
		for (Entity e : collectEntities(center, (int) Math.floor(horizontal))) {
			if (entityType.isAssignableFrom(e.getClass())) {
				@SuppressWarnings("unchecked") T entity = (T) e;
				Location entityLocation = entity.getLocation();
				if (distanceSquared2D(centerX, centerZ, entityLocation.getX(), entityLocation.getZ()) <= (horizontal * horizontal) && NumberUtil.subtract(center.getY(), entityLocation.getY()) <= vertical && (predicate == null || predicate.test(entity))) {
					entities.add(entity);
				}
			}
		}
		return entities;
	}

	/**
	 * 주변에 있는 특정 타입의 엔티티 목록을 반환합니다.
	 * 만약 탐색된 엔티티가 플레이어일 경우, 게임에 참여하고 있지 않거나 탈락한 경우, 그리고 타게팅이 불가능한 경우 포함하지 않습니다.
	 * 만약 게임모드 종류가 팀 게임이고 중심 엔티티와 탐색된 엔티티가 플레이어일 경우 둘이 동일한 팀에 소속되었다면 포함하지 않습니다.
	 *
	 * @param entityType 탐색할 엔티티 타입
	 * @param center     중심 엔티티 (엔티티의 위치가 중점)
	 * @param horizontal 수평 거리
	 * @param vertical   수직 거리
	 * @return 주변에 있는 특정 타입의 엔티티 목록
	 */
	public static <E extends Entity> ArrayList<E> getNearbyEntities(Class<E> entityType, Entity center, double horizontal, double vertical) {
		return getNearbyEntities(entityType, center.getLocation(), horizontal, vertical, Predicates.STRICT(center));
	}

	/**
	 * 주변에 있는 특정 타입의 엔티티 목록을 반환합니다.
	 * 만약 탐색된 엔티티가 플레이어일 경우, 게임에 참여하고 있지 않거나 탈락한 경우, 그리고 타게팅이 불가능한 경우 포함하지 않습니다.
	 *
	 * @param entityType 탐색할 엔티티 타입
	 * @param center     중점
	 * @param horizontal 수평 거리
	 * @param vertical   수직 거리
	 * @return 주변에 있는 특정 타입의 엔티티 목록
	 */
	public static <E extends Entity> ArrayList<E> getNearbyEntities(Class<E> entityType, Location center, double horizontal, double vertical) {
		return getNearbyEntities(entityType, center, horizontal, vertical, Predicates.PARTICIPANTS());
	}

	public static ArrayList<Damageable> getNearbyDamageableEntities(Player p, double horizontal, double vertical) {
		return getNearbyEntities(Damageable.class, p, horizontal, vertical);
	}

	public static ArrayList<Damageable> getNearbyDamageableEntities(Location l, double horizontal, double vertical) {
		return getNearbyEntities(Damageable.class, l, horizontal, vertical);
	}

	public static ArrayList<Player> getNearbyPlayers(Player p, double horizontal, double vertical) {
		return getNearbyEntities(Player.class, p, horizontal, vertical);
	}

	public static ArrayList<Player> getNearbyPlayers(Location l, double horizontal, double vertical) {
		return getNearbyEntities(Player.class, l, horizontal, vertical);
	}

	/**
	 * 주변에 있는 특정 타입의 커스텀 엔티티 목록을 반환합니다.
	 *
	 * @param entityType 탐색할 엔티티 타입
	 * @param center     중점
	 * @param horizontal 수평 거리
	 * @param vertical   수직 거리
	 * @param predicate  커스텀 조건
	 * @return 주변에 있는 특정 타입의 커스텀 엔티티 목록
	 */
	@SuppressWarnings("unchecked")
	public static <T> ArrayList<T> getNearbyCustomEntities(Class<T> entityType, Location center, double horizontal, double vertical, Predicate<CustomEntity> predicate) {
		double centerX = center.getX(), centerZ = center.getZ();
		ArrayList<T> entities = new ArrayList<>();
		for (CustomEntity e : collectCustomEntities(center, (int) Math.floor(horizontal))) {
			if (entityType.isAssignableFrom(e.getClass())) {
				if (distanceSquared2D(centerX, centerZ, e.x(), e.z()) <= (horizontal * horizontal) && NumberUtil.subtract(center.getY(), e.y()) <= vertical && (predicate == null || predicate.test(e))) {
					entities.add((T) e);
				}
			}
		}
		return entities;
	}

	public static <T extends Entity> List<T> getConflictingEntities(Class<T> entityType, BoundingBox boundingBox, Predicate<Entity> predicate) {
		List<T> entities = new ArrayList<>();
		World world = boundingBox.getCenter().getWorld();
		Chunk chunk = boundingBox.getCenter().getChunk();
		int chunkX = chunk.getX(), chunkZ = chunk.getZ();
		for (int x = chunkX - 1; x <= chunkX + 1; x++) {
			for (int z = chunkZ - 1; z <= chunkZ + 1; z++) {
				for (Entity e : world.getChunkAt(x, z).getEntities()) {
					if (entityType.isAssignableFrom(e.getClass())) {
						@SuppressWarnings("unchecked") T entity = (T) e;
						Boundary.BoundaryData boundaryData = Boundary.BoundaryData.of(entity.getType());
						Location entityLocation = entity.getLocation();
						double entityX = entityLocation.getX(), entityY = entityLocation.getY(), entityZ = entityLocation.getZ();
						if (entityX + boundaryData.getMinX() < boundingBox.getMaxX() && boundingBox.getMinX() < entityX + boundaryData.getMaxX() && entityY + boundaryData.getMinY() < boundingBox.getMaxY() &&
								boundingBox.getMinY() < entityY + boundaryData.getMaxY() && entityZ + boundaryData.getMinZ() < boundingBox.getMaxZ() && boundingBox.getMinZ() < entityZ + boundaryData.getMaxZ() &&
								(predicate == null || predicate.test(entity))) {
							entities.add(entity);
						}
					}
				}
			}
		}
		return entities;
	}

	public static <T extends Entity> List<T> getConflictingEntities(Class<T> entityType, BoundingBox boundingBox) {
		return getConflictingEntities(entityType, boundingBox, Predicates.PARTICIPANTS());
	}

	public static List<Damageable> getConflictingDamageables(BoundingBox boundingBox) {
		return getConflictingEntities(Damageable.class, boundingBox);
	}

	public static class Locations extends ArrayList<Location> {

		public Locations(int initialCapacity) {
			super(initialCapacity);
		}

		public Locations() {
			super();
		}

		public Locations floor(double referenceY) {
			for (Location location : this) {
				location.setY(getFloorYAt(location.getWorld(), referenceY, location.getBlockX(), location.getBlockZ()) + 0.1);
			}
			return this;
		}

		public Locations highest() {
			for (Location location : this) {
				location.setY(location.getWorld().getHighestBlockYAt(location) + 0.1);
			}
			return this;
		}

		public Locations add(Vector vector) {
			for (Location location : this) {
				location.add(vector);
			}
			return this;
		}

		public Locations add(double x, double y, double z) {
			for (Location location : this) {
				location.add(x, y, z);
			}
			return this;
		}

	}

	public static class Predicates {

		private Predicates() {
		}

		public static Predicate<Entity> STRICT(Entity criterion) {
			return new Predicate<Entity>() {
				@Override
				public boolean test(Entity entity) {
					if (entity.equals(criterion)) return false;
					if (GameManager.isGameRunning() && entity instanceof Player) {
						AbstractGame game = GameManager.getGame();
						Player player = (Player) entity;
						if (!game.isParticipating(player) || (game instanceof DeathManager.Handler && ((DeathManager.Handler) game).getDeathManager().isExcluded(player)) || !game.getParticipant(player).attributes().TARGETABLE.getValue()) {
							return false;
						}
						if (game instanceof TeamGame && criterion instanceof Player) {
							TeamGame teamGame = (TeamGame) game;
							Participant criteriaParticipant = game.getParticipant((Player) criterion);
							if (criteriaParticipant != null) {
								Participant participant = game.getParticipant(player);
								return !teamGame.hasTeam(participant) || !teamGame.hasTeam(criteriaParticipant) || (!teamGame.getTeam(participant).equals(teamGame.getTeam(criteriaParticipant)));
							}
						}
					}
					return true;
				}
			};
		}

		public static Predicate<Entity> PARTICIPANTS_EXCLUDING_TEAMS(Entity criterion) {
			return new Predicate<Entity>() {
				@Override
				public boolean test(Entity entity) {
					if (GameManager.isGameRunning() && entity instanceof Player) {
						AbstractGame game = GameManager.getGame();
						Player player = (Player) entity;
						if (!game.isParticipating(player) || (game instanceof DeathManager.Handler && ((DeathManager.Handler) game).getDeathManager().isExcluded(player)) || !game.getParticipant(player).attributes().TARGETABLE.getValue()) {
							return false;
						}
						if (game instanceof TeamGame && criterion instanceof Player) {
							TeamGame teamGame = (TeamGame) game;
							Participant criteriaParticipant = game.getParticipant((Player) criterion);
							if (criteriaParticipant != null) {
								Participant participant = game.getParticipant(player);
								return !teamGame.hasTeam(participant) || !teamGame.hasTeam(criteriaParticipant) || (!teamGame.getTeam(participant).equals(teamGame.getTeam(criteriaParticipant)));
							}
						}
					}
					return true;
				}
			};
		}

		public static Predicate<Entity> PARTICIPANTS_UNEQUAL(Entity criterion) {
			return new Predicate<Entity>() {
				@Override
				public boolean test(Entity entity) {
					if (entity.equals(criterion)) return false;
					if (GameManager.isGameRunning() && entity instanceof Player) {
						AbstractGame game = GameManager.getGame();
						Player player = (Player) entity;
						return game.isParticipating(player) && (!(game instanceof DeathManager.Handler) || !((DeathManager.Handler) game).getDeathManager().isExcluded(player)) && game.getParticipant(player).attributes().TARGETABLE.getValue();
					}
					return true;
				}
			};
		}

		public static Predicate<Entity> PARTICIPANTS() {
			return new Predicate<Entity>() {
				@Override
				public boolean test(Entity entity) {
					if (GameManager.isGameRunning() && entity instanceof Player) {
						AbstractGame game = GameManager.getGame();
						Player player = (Player) entity;
						return game.isParticipating(player) && (!(game instanceof DeathManager.Handler) || !((DeathManager.Handler) game).getDeathManager().isExcluded(player)) && game.getParticipant(player).attributes().TARGETABLE.getValue();
					}
					return true;
				}
			};
		}

	}

}
