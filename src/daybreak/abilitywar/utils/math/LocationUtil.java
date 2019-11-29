package daybreak.abilitywar.utils.math;

import daybreak.abilitywar.game.games.mode.AbstractGame;
import daybreak.abilitywar.game.games.mode.AbstractGame.Participant;
import daybreak.abilitywar.game.games.mode.decorator.TeamGame;
import daybreak.abilitywar.game.manager.object.DeathManager;
import daybreak.abilitywar.utils.thread.AbilityWarThread;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Damageable;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Random;
import java.util.function.Predicate;

/**
 * Location Util
 *
 * @author Daybreak 새벽
 */
public class LocationUtil {

	private LocationUtil() {}

	/**
	 * {@link Location}이 범위 안에 있는지 확인합니다.
	 *
	 * @param center      중심
	 * @param location    확인할 위치
	 * @param radius      원의 반지름
	 */
	public static boolean isInCircle(Location center, Location location, double radius) {
		return center.getWorld().equals(location.getWorld()) && center.distanceSquared(location) <= (radius * radius);
	}

	/**
	 * {@link Location}이 범위 안에 있는지 확인합니다.
	 *
	 * @param center      중심
	 * @param location    확인할 위치
	 * @param radius      원의 반지름
	 */
	public static boolean isInCircle(Location center, Location location, int radius) {
		return center.getWorld().equals(location.getWorld()) && center.distanceSquared(location) <= (radius * radius);
	}

	/**
	 * 3차원 공간에서 범위 안에 있는 블록들을 {@link ArrayList}로  반환합니다.
	 *
	 * @param center        중심
	 * @param radius    	범위
	 * @param hollow        참일 경우 바깥 부분의 블록들만 가져옵니다.
	 * @param alsoAir 		참일 경우 블록이 비어있어도 가져옵니다.
	 */
	public static ArrayList<Block> getBlocks3D(Location center, int radius, boolean hollow, boolean alsoAir) {
		ArrayList<Block> blocks = new ArrayList<>();
		int blockX = center.getBlockX();
		int blockY = center.getBlockY();
		int blockZ = center.getBlockZ();
		for (int x = blockX - radius; x <= blockX + radius; x++) {
			for (int y = blockY - radius; y <= blockY + radius; y++) {
				for (int z = blockZ - radius; z <= blockZ + radius; z++) {
					Block block = center.getWorld().getBlockAt(x, y, z);
					double distanceSquared = center.distanceSquared(block.getLocation());
					if (distanceSquared <= (radius * radius) && !(hollow && distanceSquared < ((radius - 1) * (radius - 1)))) {
						if (block.isEmpty()) {
							if (alsoAir) {
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
	 * @param horizontal    범위
	 * @param hollow        참일 경우 바깥 부분의 블록들만 가져옵니다.
	 * @param highestBlocks 참일 경우 각 위치에서 가장 높은 위치에 있는 블록들을 가져옵니다. 이 경우 모든 블록이 같은 평면 위에 있지 않을 수 있습니다.
	 */
	public static ArrayList<Block> getBlocks2D(Location center, int horizontal, boolean hollow, boolean highestBlocks) {
		ArrayList<Block> blocks = new ArrayList<>();

		int blockX = center.getBlockX();
		int blockZ = center.getBlockZ();

		for (int x = blockX - horizontal; x <= blockX + horizontal; x++) {
			for (int z = blockZ - horizontal; z <= blockZ + horizontal; z++) {
				Block block = highestBlocks ? center.getWorld().getHighestBlockAt(x, z) : center.getWorld().getBlockAt(x, center.getBlockY(), z);
				if (highestBlocks && block.isEmpty()) block = block.getRelative(0, -1, 0);
				double distance = center.distanceSquared(block.getLocation());
				if (!block.isEmpty() && distance <= (horizontal * horizontal) && !(hollow && distance < ((horizontal - 1) * (horizontal - 1)))) {
					blocks.add(block);
				}
			}
		}

		return blocks;
	}

	public static ArrayList<Location> getRandomLocations(Location center, double radius, int amount) {
		Random random = new Random();
		ArrayList<Location> locations = new ArrayList<>();
		for (int i = 0; i < amount; i++) {
			double angle = random.nextDouble() * 360;
			double x = center.getX() + (random.nextDouble() * radius * Math.cos(Math.toRadians(angle)));
			double z = center.getZ() + (random.nextDouble() * radius * Math.sin(Math.toRadians(angle)));
			Location l = new Location(center.getWorld(), x, center.getWorld().getHighestBlockYAt((int) x, (int) z), z);
			locations.add(l);
		}
		return locations;
	}

	public static ArrayList<Location> getSphere(Location center, double r, int amount) {
		ArrayList<Location> locations = new ArrayList<>();
		if (amount > 0) {
			for (double i = 0; i <= Math.PI; i += Math.PI / amount) {
				double radius = Math.sin(i) * r;
				double y = Math.cos(i) * r;
				for (double a = 0; a < Math.PI * 2; a += Math.PI / amount) {
					double x = Math.cos(a) * radius;
					double z = Math.sin(a) * radius;
					locations.add(center.clone().add(x, y, z));
				}
			}
		}
		return locations;
	}

	@SuppressWarnings("unchecked")
	public static <E extends Entity> E getNearestEntity(Class<E> entityType, Entity center) {
		double distance = Double.MAX_VALUE;
		E current = null;

		final Location centerLocation = center.getLocation().clone();
		for (Entity e : center.getWorld().getEntities()) {
			if (!e.equals(center)) {
				if (entityType.isAssignableFrom(e.getClass())) {
					if (AbilityWarThread.isGameTaskRunning() && e instanceof Player) {
						AbstractGame game = AbilityWarThread.getGame();
						Player p = (Player) e;
						if (!game.isParticipating(p) || (game instanceof DeathManager.Handler && ((DeathManager.Handler) game).getDeathManager().isDead(p))) {
							continue;
						}
						if (game instanceof TeamGame && center instanceof Player) {
							Participant participant = game.getParticipant(p);
							TeamGame teamGame = (TeamGame) game;
							Participant centerParticipant = game.getParticipant((Player) center);
							if (centerParticipant != null) {
								if (teamGame.hasTeam(participant) && teamGame.hasTeam(centerParticipant) && (teamGame.getTeam(participant).equals(teamGame.getTeam(centerParticipant)))) {
									continue;
								}
							}
						}
					}

					double compare = centerLocation.distanceSquared(e.getLocation());
					if (compare < distance) {
						distance = compare;
						current = (E) e;
					}
				}
			}
		}

		return current;
	}

	public static Player getNearestPlayer(Player center) {
		return getNearestEntity(Player.class, center);
	}


	public static <T extends Entity> Collection<T> getNearbyEntities(Class<T> entityType, Location location, double horizontal, double vertical, Predicate<T> predicate) {
		ArrayList<T> entities = new ArrayList<>();
		for (Entity e : collectEntities(location, horizontal)) {
			if (entityType.isAssignableFrom(e.getClass())) {
				@SuppressWarnings("unchecked") T entity = (T) e;
				Location entityLocation = entity.getLocation();
				if (location.distanceSquared(entityLocation) <= (horizontal * horizontal) && NumberUtil.subtract(location.getY(), entityLocation.getY()) <= vertical && (predicate == null || predicate.test(entity))) {
					entities.add(entity);
				}
			}
		}
		return entities;
	}

	public static Collection<Entity> collectEntities(Location location, double horizontal) {
		ArrayList<Entity> entities = new ArrayList<>();
		World world = location.getWorld();
		Chunk leftTop = location.clone().add(horizontal, 0, -horizontal).getChunk();
		Chunk rightBottom = location.clone().add(-horizontal, 0, horizontal).getChunk();
		for (int x = rightBottom.getX(); x <= leftTop.getX(); x++) {
			for (int z = leftTop.getZ(); z <= rightBottom.getZ(); z++) {
				entities.addAll(Arrays.asList(world.getChunkAt(x, z).getEntities()));
			}
		}
		return entities;
	}

	public static <E extends Entity> Collection<E> getNearbyEntities(Class<E> clazz, Entity center, double horizontal, double vertical) {
		return getNearbyEntities(clazz, center.getLocation(), horizontal, vertical, e -> {
			if (e.equals(center)) return false;
			if (AbilityWarThread.isGameTaskRunning() && e instanceof Player) {
				AbstractGame game = AbilityWarThread.getGame();
				Player p = (Player) e;
				if (!game.isParticipating(p) || (game instanceof DeathManager.Handler && ((DeathManager.Handler) game).getDeathManager().isDead(p)))
					return false;
				if (game instanceof TeamGame && center instanceof Player) {
					Participant participant = game.getParticipant(p);
					TeamGame teamGame = (TeamGame) game;
					Participant centerParticipant = game.getParticipant((Player) center);
					if (centerParticipant != null) {
						return !teamGame.hasTeam(participant) || !teamGame.hasTeam(centerParticipant) || (!teamGame.getTeam(participant).equals(teamGame.getTeam(centerParticipant)));
					}
				}
			}
			return true;
		});
	}

	public static <E extends Entity> Collection<E> getNearbyEntities(Class<E> clazz, Location center, double horizontal, double vertical) {
		return getNearbyEntities(clazz, center, horizontal, vertical, e -> {
			if (AbilityWarThread.isGameTaskRunning() && e instanceof Player) {
				AbstractGame game = AbilityWarThread.getGame();
				Player p = (Player) e;
				return game.isParticipating(p) && (!(game instanceof DeathManager.Handler) || !((DeathManager.Handler) game).getDeathManager().isDead(p));
			}
			return true;
		});
	}

	public static Collection<Damageable> getNearbyDamageableEntities(Player p, double horizontal, double vertical) {
		return getNearbyEntities(Damageable.class, p, horizontal, vertical);
	}

	public static Collection<Damageable> getNearbyDamageableEntities(Location l, double horizontal, double vertical) {
		return getNearbyEntities(Damageable.class, l, horizontal, vertical);
	}

	public static Collection<Player> getNearbyPlayers(Player p, double horizontal, double vertical) {
		return getNearbyEntities(Player.class, p, horizontal, vertical);
	}

	public static Collection<Player> getNearbyPlayers(Location l, double horizontal, double vertical) {
		return getNearbyEntities(Player.class, l, horizontal, vertical);
	}

}
