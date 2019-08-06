package DayBreak.AbilityWar.Utils.Math;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Damageable;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import DayBreak.AbilityWar.Game.Games.Mode.AbstractGame;
import DayBreak.AbilityWar.Game.Games.Mode.AbstractGame.Participant;
import DayBreak.AbilityWar.Game.Games.Mode.TeamGame;
import DayBreak.AbilityWar.Utils.Thread.AbilityWarThread;

/**
 * Location Util
 * @author DayBreak 새벽
 */
public class LocationUtil {

	private LocationUtil() {}
	
	public static boolean isInCircle(final Location center, final Location location, final double radius, final boolean flatsurface) {
		if(flatsurface) {center.setY(0); location.setY(0);}
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

		return Blocks.stream().distinct().collect(Collectors.toList());
	}
	
	/**
	 * 범위 안에서 같은 Y 좌표에 있는 블록들을 List로 반환합니다.
	 * @param center 중심
	 * @param radius 반지름
	 */
	public static List<Block> getBlocksAtSameY(Location center, Integer radius, boolean hollow, boolean top) {
		List<Block> blocks = new ArrayList<Block>();

		Integer X = center.getBlockX();
		Integer Y = center.getBlockY();
		Integer Z = center.getBlockZ();

		for (Integer x = X - radius; x <= X + radius; x++) {
			for (Integer z = Z - radius; z <= Z + radius; z++) {
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
	
	public static <E> E getNearestEntity(Class<E> clazz, Location center, Entity exception) {
		double distance = Double.MAX_VALUE;
		E entity = null;
		
		for(Entity e : center.getWorld().getEntities()) {
			if(clazz.isAssignableFrom(e.getClass())) {
				if(!e.equals(exception)) {
					if (AbilityWarThread.isGameTaskRunning() && e instanceof Player) {
						AbstractGame game = AbilityWarThread.getGame();
						Player p = (Player) e;
						if(game.isParticipating(p)) {
							if(game.getDeathManager().isEliminated(p)) continue;
							Participant part = game.getParticipant(p);

							if (game instanceof TeamGame && exception instanceof Player) {
								TeamGame tgame = (TeamGame) game;
								Player ex = (Player) exception;
								if(game.isParticipating(ex)) {
									Participant expart = game.getParticipant(ex);
									if(tgame.hasTeam(part) && tgame.hasTeam(expart) && (tgame.getTeam(part).equals(tgame.getTeam(expart)))) continue;
								}
							}
						} else {
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

	public static Player getNearestPlayer(Player p) {
		return getNearestEntity(Player.class, p.getLocation(), p);
	}
	
	public static <E> List<E> getNearbyEntities(Class<E> clazz, Location center, int HorizontalDis, int VerticalDis, Entity exception) {
		List<E> entities = new ArrayList<E>();
		
		//if(ServerVersion.getVersion() >= 9) {
			for (Entity e : center.getWorld().getNearbyEntities(center, HorizontalDis, VerticalDis, HorizontalDis)) {
				if (clazz.isAssignableFrom(e.getClass())) {
					if(!e.equals(exception)) {
						if (AbilityWarThread.isGameTaskRunning() && e instanceof Player) {
							AbstractGame game = AbilityWarThread.getGame();
							Player p = (Player) e;
							if(game.isParticipating(p)) {
								if(game.getDeathManager().isEliminated(p)) continue;
								Participant part = game.getParticipant(p);

								if (game instanceof TeamGame && exception instanceof Player) {
									TeamGame tgame = (TeamGame) game;
									Player ex = (Player) exception;
									if(game.isParticipating(ex)) {
										Participant expart = game.getParticipant(ex);
										if(tgame.hasTeam(part) && tgame.hasTeam(expart) && (tgame.getTeam(part).equals(tgame.getTeam(expart)))) continue;
									}
								}
							} else {
								continue;
							}
						}
						entities.add(clazz.cast(e));
					}
				}
			}
		/*
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
		*/

		return entities;
	}

	public static <E> List<E> getNearbyEntities(Class<E> clazz, Location center, int HorizontalDis, int VerticalDis) {
		return getNearbyEntities(clazz, center, HorizontalDis, VerticalDis, null);
	}
	
	public static List<Damageable> getNearbyDamageableEntities(Player p, int HorizontalDis, int VerticalDis) {
		return getNearbyEntities(Damageable.class, p.getLocation(), HorizontalDis, VerticalDis, p);
	}

	public static List<Damageable> getNearbyDamageableEntities(Location l, int HorizontalDis, int VerticalDis) {
		return getNearbyEntities(Damageable.class, l, HorizontalDis, VerticalDis);
	}
	
	public static List<Player> getNearbyPlayers(Player p, int HorizontalDis, int VerticalDis) {
		return getNearbyEntities(Player.class, p.getLocation(), HorizontalDis, VerticalDis, p);
	}

	public static List<Player> getNearbyPlayers(Location l, int HorizontalDis, int VerticalDis) {
		return getNearbyEntities(Player.class, l, HorizontalDis, VerticalDis);
	}

}
