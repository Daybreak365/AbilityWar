package DayBreak.AbilityWar.Utils.Math;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Damageable;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import DayBreak.AbilityWar.Game.Games.Mode.AbstractGame;
import DayBreak.AbilityWar.Utils.Thread.AbilityWarThread;
import DayBreak.AbilityWar.Utils.VersionCompat.ServerVersion;

/**
 * Location Util
 * @author DayBreak 새벽
 */
public class LocationUtil {

	private LocationUtil() {}
	
	public static boolean isInCircle(Location center, Location location, double radius) {
		if(center.getWorld().equals(location.getWorld())) {
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
	 * @param center 중심
	 * @param radius 반지름
	 */
	public static List<Block> getBlocks(Location center, Integer radius, boolean hollow, boolean top,
			boolean alsoAir) {
		List<Block> Blocks = new ArrayList<Block>();

		Integer X = center.getBlockX();
		Integer Y = center.getBlockY();
		Integer Z = center.getBlockZ();

		for (Integer x = X - radius; x <= X + radius; x++) {
			for (Integer y = Y - radius; y <= Y + radius; y++) {
				for (Integer z = Z - radius; z <= Z + radius; z++) {
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

		return Blocks;
	}
	
	/**
	 * 범위 안에서 같은 Y 좌표에 있는 블록들을 List로 반환합니다.
	 * @param center 중심
	 * @param radius 반지름
	 */
	public static List<Block> getBlocksAtSameY(Location center, Integer radius, boolean hollow) {
		List<Block> blocks = new ArrayList<Block>();

		Integer X = center.getBlockX();
		Integer Y = center.getBlockY();
		Integer Z = center.getBlockZ();

		for (Integer x = X - radius; x <= X + radius; x++) {
			for (Integer z = Z - radius; z <= Z + radius; z++) {
				Location l = new Location(center.getWorld(), x, Y, z);
				double distance = center.distance(l);
				if (distance <= radius && !(hollow && distance < (radius - 1))) {
					blocks.add(l.getBlock());
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

	public static List<Location> getSphere(Location center, double r, int Amount) {
		List<Location> locations = new ArrayList<Location>();
		
		if(Amount > 0) {
			for (double i = 0; i <= Math.PI; i += Math.PI / Amount) {
				double radius = Math.sin(i) * r;
				double y = Math.cos(i) * r;
				for (double a = 0; a < Math.PI * 2; a += Math.PI / Amount) {
					double x = Math.cos(a) * radius;
					double z = Math.sin(a) * radius;
					locations.add(center.clone().add(x, y, z));
				}
			}
		}
		
		return locations;
	}

	public static ArrayList<Location> getCircle(Location center, int radius, int amount, boolean HighestY) {
		return getCircle(center, Double.valueOf(radius), amount, HighestY);
	}

	public static ArrayList<Location> getCircle(Location center, double radius, int amount, boolean HighestY) {
		ArrayList<Location> locations = new ArrayList<Location>();
		
		if(amount > 0) {
			for(double degree = 0; degree < 360; degree += (360 / amount)) {
				double radians = Math.toRadians(degree);
				double X = Math.cos(radians) * radius;
				double Z = Math.sin(radians) * radius;
				
				Location location = center.clone().add(X, 0, Z);
				if(HighestY) location.setY(location.getWorld().getHighestBlockYAt(location) + 1);
				locations.add(location);
			}
		}
		
		return locations;
	}

	public static <E> E getNearestEntity(Class<E> clazz, Location center, List<Entity> exceptions) {
		double distance = Double.MAX_VALUE;
		E entity = null;
		
		for(Entity e : center.getWorld().getEntities()) {
			if(clazz.isAssignableFrom(e.getClass())) {
				if(!exceptions.contains(e)) {
					if (e instanceof Player && AbilityWarThread.isGameTaskRunning()) {
						AbstractGame game = AbilityWarThread.getGame();
						Player player = (Player) e;
						if (!game.isParticipating(player) || game.getDeathManager().isEliminated(player)) {
							continue;
						}
					}
					double compare = center.distance(e.getLocation());
					if(compare < distance) {
						distance = compare;
						entity = clazz.cast(e);
					}
				}
			}
		}
		
		return entity;
	}

	public static Player getNearestPlayer(Player base) {
		return getNearestEntity(Player.class, base.getLocation(), Arrays.asList(base));
	}
	
	public static <E> List<E> getNearbyEntities(Class<E> clazz, Location center, int HorizontalDis, int VerticalDis, List<Entity> exceptions) {
		List<E> entities = new ArrayList<E>();
		
		if(ServerVersion.getVersion() >= 9) {
			for (Entity e : center.getWorld().getNearbyEntities(center, HorizontalDis, VerticalDis, HorizontalDis)) {
				if (clazz.isAssignableFrom(e.getClass())) {
					if(!exceptions.contains(e)) {
						if (e instanceof Player && AbilityWarThread.isGameTaskRunning()) {
							AbstractGame game = AbilityWarThread.getGame();
							Player player = (Player) e;
							if (!game.isParticipating(player) || game.getDeathManager().isEliminated(player)) {
								continue;
							}
						}
						entities.add(clazz.cast(e));
					}
				}
			}
		} else {
			for (Entity e : center.getWorld().getEntities()) {
				Location entityLoc = e.getLocation();
				if(NumberUtil.Subtract(Math.abs(entityLoc.getY()), Math.abs(center.getY())) <= VerticalDis) {
					if(NumberUtil.Subtract(Math.abs(entityLoc.getX()), Math.abs(center.getX())) <= HorizontalDis
					|| NumberUtil.Subtract(Math.abs(entityLoc.getZ()), Math.abs(center.getZ())) <= HorizontalDis) {
						if (clazz.isAssignableFrom(e.getClass())) {
							if(!exceptions.contains(e)) {
								if (e instanceof Player && AbilityWarThread.isGameTaskRunning()) {
									AbstractGame game = AbilityWarThread.getGame();
									Player player = (Player) e;
									if (!game.isParticipating(player) || game.getDeathManager().isEliminated(player)) {
										continue;
									}
								}
								entities.add(clazz.cast(e));
							}
						}
					}
				}
			}
		}

		return entities;
	}

	public static <E> List<E> getNearbyEntities(Class<E> clazz, Location center, int HorizontalDis, int VerticalDis) {
		return getNearbyEntities(clazz, center, HorizontalDis, VerticalDis, Arrays.asList());
	}
	
	public static List<Damageable> getNearbyDamageableEntities(Player p, int HorizontalDis, int VerticalDis) {
		return getNearbyEntities(Damageable.class, p.getLocation(), HorizontalDis, VerticalDis, Arrays.asList(p));
	}

	public static List<Damageable> getNearbyDamageableEntities(Location l, int HorizontalDis, int VerticalDis) {
		return getNearbyEntities(Damageable.class, l, HorizontalDis, VerticalDis);
	}

	public static List<Damageable> getNearbyDamageableEntities(Entity e, int HorizontalDis, int VerticalDis) {
		return getNearbyEntities(Damageable.class, e.getLocation(), HorizontalDis, VerticalDis, Arrays.asList(e));
	}
	
	public static List<Player> getNearbyPlayers(Player p, int HorizontalDis, int VerticalDis) {
		return getNearbyEntities(Player.class, p.getLocation(), HorizontalDis, VerticalDis, Arrays.asList(p));
	}

	public static List<Player> getNearbyPlayers(Location l, int HorizontalDis, int VerticalDis) {
		return getNearbyEntities(Player.class, l, HorizontalDis, VerticalDis);
	}

}
