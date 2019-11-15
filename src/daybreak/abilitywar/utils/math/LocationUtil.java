package daybreak.abilitywar.utils.math;

import daybreak.abilitywar.game.games.mode.AbstractGame;
import daybreak.abilitywar.game.games.mode.AbstractGame.Participant;
import daybreak.abilitywar.game.games.mode.decorator.TeamGame;
import daybreak.abilitywar.game.manager.object.DeathManager;
import daybreak.abilitywar.utils.thread.AbilityWarThread;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Random;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Damageable;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

/**
 * Location Util
 *
 * @author Daybreak 새벽
 */
public class LocationUtil {

	private LocationUtil() {
	}

	/**
	 * Location이 원 안에 위치하는지 확인합니다.
	 *
	 * @param c           원의 중심
	 * @param l           Location
	 * @param radius      원의 반지름
	 * @param flatsurface true: Y 좌표를 따로 계산하지 않습니다. false: Y 좌표 또한 포함하여 계산합니다.
	 */
	public static boolean isInCircle(final Location c, final Location l, final double radius,
									 final boolean flatsurface) {
		final Location center = c.clone();
		final Location location = l.clone();

		if (flatsurface) {
			center.setY(0);
			location.setY(0);
		}
		if (center.getWorld().equals(location.getWorld())) {
			double distance = center.distance(location);
			return (distance <= radius);
		} else {
			return false;
		}
	}

	public static Vector getRandomVector(int Max, int Y) {
		Random r = new Random();
		return new Vector(r.nextInt(Max), Y, r.nextInt(Max));
	}

	/**
	 * 범위 안에 있는 블록들을 List로 반환합니다.
	 *
	 * @param center 중심
	 * @param radius 반지름
	 */
	public static List<Block> getBlocks(Location center, Integer radius, boolean hollow, boolean top, boolean alsoAir) {
		List<Block> Blocks = new ArrayList<Block>();

		Integer X = center.getBlockX();
		Integer Y = center.getBlockY();
		Integer Z = center.getBlockZ();

		for (int x = X - radius; x <= X + radius; x++) {
			for (int y = Y - radius; y <= Y + radius; y++) {
				for (int z = Z - radius; z <= Z + radius; z++) {
					Location l = new Location(center.getWorld(), x, y, z);
					double distance = center.distanceSquared(l);
					if (distance < (radius * radius) && !(hollow && distance < ((radius - 1) * (radius - 1)))) {
						if (top) {
							Location highest = l.getWorld().getHighestBlockAt(l).getLocation();
							Block highestBlock = highest.getBlock();
							if (highestBlock.getType().equals(Material.AIR)) {
								Block lowBlock = highest.clone().subtract(0, 1, 0).getBlock();
								if (!lowBlock.getType().equals(Material.AIR)) {
									if (!Blocks.contains(lowBlock)) {
										if (!alsoAir) {
											if (!lowBlock.getType().equals(Material.AIR)) {
												Blocks.add(lowBlock);
											}
										} else {
											Blocks.add(lowBlock);
										}
									}
								}
							} else {
								if (!Blocks.contains(highestBlock)) {
									if (!alsoAir) {
										if (!highestBlock.getType().equals(Material.AIR)) {
											Blocks.add(highestBlock);
										}
									} else {
										Blocks.add(highestBlock);
									}
								}
							}
						} else {
							Block block = l.getBlock();
							if (!alsoAir) {
								if (!block.getType().equals(Material.AIR)) {
									Blocks.add(block);
								}
							} else {
								Blocks.add(block);
							}
						}
					}
				}
			}
		}

		return Blocks.stream().distinct().collect(Collectors.toList());
	}

	/**
	 * 범위 안에서 같은 Y 좌표에 있는 블록들을 List로 반환합니다.
	 *
	 * @param center 중심
	 * @param radius 반지름
	 */
	public static List<Block> getBlocksAtSameY(Location center, Integer radius, boolean hollow, boolean top) {
		List<Block> blocks = new ArrayList<Block>();

		Integer X = center.getBlockX();
		int Y = center.getBlockY();
		Integer Z = center.getBlockZ();

		for (int x = X - radius; x <= X + radius; x++) {
			for (int z = Z - radius; z <= Z + radius; z++) {
				Location l = new Location(center.getWorld(), x, Y, z);
				double distance = center.distance(l);
				if (distance <= radius && !(hollow && distance < (radius - 1))) {
					if (top) {
						Location highest = l.getWorld().getHighestBlockAt(l).getLocation();
						blocks.add(highest.getBlock());
					} else {
						blocks.add(l.getBlock());
					}
				}
			}
		}

		return blocks;
	}

	public static ArrayList<Location> getRandomLocations(Location center, double radius, int amount) {
		Random r = new Random();

		ArrayList<Location> locations = new ArrayList<Location>();
		for (int i = 0; i < amount; i++) {
			double Angle = r.nextDouble() * 360;
			double x = center.getX() + (r.nextDouble() * radius * Math.cos(Math.toRadians(Angle)));
			double z = center.getZ() + (r.nextDouble() * radius * Math.sin(Math.toRadians(Angle)));
			double y = center.getWorld().getHighestBlockYAt((int) x, (int) z);

			Location l = new Location(center.getWorld(), x, y, z);

			locations.add(l);
		}
		return locations;
	}

	public static List<Location> getSphere(Location center, double r, int amount) {
		List<Location> locations = new ArrayList<>();

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
						if (!game.isParticipating(p) || (game instanceof DeathManager.Handler && ((DeathManager.Handler) game).getDeathManager().isEliminated(p))) {
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
				if (!game.isParticipating(p) || (game instanceof DeathManager.Handler && ((DeathManager.Handler) game).getDeathManager().isEliminated(p)))
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
				return game.isParticipating(p) && (!(game instanceof DeathManager.Handler) || !((DeathManager.Handler) game).getDeathManager().isEliminated(p));
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
