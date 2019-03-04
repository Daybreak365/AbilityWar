package Marlang.AbilityWar.Utils.Math;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Damageable;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import Marlang.AbilityWar.GameManager.Game.AbstractGame;
import Marlang.AbilityWar.GameManager.Object.Participant;
import Marlang.AbilityWar.Utils.Thread.AbilityWarThread;
import Marlang.AbilityWar.Utils.VersionCompat.ServerVersion;

/**
 * Location Util
 * 
 * @author _Marlang 말랑
 */
public class LocationUtil {

	public static boolean isInCircle(Location center, Location location, double radius) {
		if(center.getWorld().equals(location.getWorld())) {
			double distance = center.distance(location);
			return (distance <= radius);
		} else {
			return false;
		}
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

	/**
	 * 기준에서 가장 가까운 플레이어를 받아옵니다.
	 * 
	 * @param Base 기준이 되는 플레이어.
	 */
	public static Player getNearestPlayer(Player Base) {
		List<Player> Players;

		if (AbilityWarThread.isGameTaskRunning()) {
			AbstractGame game = AbilityWarThread.getGame();
			
			Players = new ArrayList<Player>();
			for(Participant participant : game.getParticipants()) {
				Player p = participant.getPlayer();
				if(!game.getDeathManager().isEliminated(p)) {
					Players.add(p);
				}
			}
		} else {
			Players = new ArrayList<Player>(Bukkit.getOnlinePlayers());
		}

		Location l = Base.getLocation();
		Double Distance = Double.MAX_VALUE;
		Player player = null;
		for (Player p : Players) {
			if (!p.equals(Base)) {
				Double d = l.distance(p.getLocation());
				if (d < Distance) {
					Distance = d;
					player = p;
				}
			}
		}

		return player;
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

	public static ArrayList<Location> getRandomLocations(Location center, double radius, int amount) {
		Random r = new Random();

		ArrayList<Location> locations = new ArrayList<Location>();
		for (int i = 0; i < amount; i++) {
			double Angle = r.nextDouble() * 360;
			double x = center.getX() + (r.nextDouble() * radius * Math.cos(Math.toRadians(Angle)));
			double z = center.getZ() + (r.nextDouble() * radius * Math.sin(Math.toRadians(Angle)));
			double y = center.getWorld().getHighestBlockYAt((int) x, (int) z) + 1;

			Location l = new Location(center.getWorld(), x, y, z);

			locations.add(l);
		}
		return locations;
	}

	public static ArrayList<Damageable> getNearbyDamageableEntities(Player p, Integer HorizontalDis, Integer VerticalDis) {
		ArrayList<Damageable> Entities = new ArrayList<Damageable>();
		for (Entity e : p.getNearbyEntities(HorizontalDis, VerticalDis, HorizontalDis)) {
			if (e instanceof Damageable) {
				if (e instanceof Player) {
					if (AbilityWarThread.isGameTaskRunning()) {
						AbstractGame game = AbilityWarThread.getGame();
						Player player = (Player) e;
						if (!game.isParticipating(player) || game.getDeathManager().isEliminated(player)) {
							continue;
						}
					}
				}
				Entities.add((Damageable) e);
			}
		}

		return Entities;
	}

	public static ArrayList<Damageable> getNearbyDamageableEntities(Location l, Integer HorizontalDis, Integer VerticalDis) {
		if(ServerVersion.getVersion() >= 9) {
			ArrayList<Damageable> Entities = new ArrayList<Damageable>();
			for (Entity e : l.getWorld().getNearbyEntities(l, HorizontalDis, VerticalDis, HorizontalDis)) {
				if (e instanceof Damageable) {
					if (e instanceof Player) {
						if (AbilityWarThread.isGameTaskRunning()) {
							AbstractGame game = AbilityWarThread.getGame();
							Player player = (Player) e;
							if (!game.isParticipating(player) || game.getDeathManager().isEliminated(player)) {
								continue;
							}
						}
					}
					Entities.add((Damageable) e);
				}
			}

			return Entities;
		} else {
			ArrayList<Damageable> Entities = new ArrayList<Damageable>();
			for (Entity e : l.getWorld().getEntities()) {
				Location targetLocation = e.getLocation();
				if(NumberUtil.Subtract(Math.abs(targetLocation.getY()), Math.abs(l.getY())) <= VerticalDis) {
					if(NumberUtil.Subtract(Math.abs(targetLocation.getX()), Math.abs(l.getX())) <= HorizontalDis
					|| NumberUtil.Subtract(Math.abs(targetLocation.getZ()), Math.abs(l.getZ())) <= HorizontalDis) {
						if (e instanceof Damageable) {
							if (e instanceof Player) {
								if (AbilityWarThread.isGameTaskRunning()) {
									AbstractGame game = AbilityWarThread.getGame();
									Player player = (Player) e;
									if (!game.isParticipating(player) || game.getDeathManager().isEliminated(player)) {
										continue;
									}
								}
							}
							Entities.add((Damageable) e);
						}
					}
				}
			}
			
			return Entities;
		}
	}

	public static ArrayList<Damageable> getNearbyDamageableEntities(Entity p, Integer HorizontalDis, Integer VerticalDis) {
		ArrayList<Damageable> Entities = new ArrayList<Damageable>();
		for (Entity e : p.getNearbyEntities(HorizontalDis, VerticalDis, HorizontalDis)) {
			if (e instanceof Damageable) {
				if (e instanceof Player) {
					if (AbilityWarThread.isGameTaskRunning()) {
						AbstractGame game = AbilityWarThread.getGame();
						Player player = (Player) e;
						if (!game.isParticipating(player) || game.getDeathManager().isEliminated(player)) {
							continue;
						}
					}
				}
				Entities.add((Damageable) e);
			}
		}

		return Entities;
	}
	
	/**
	 * 가까이에 있는 플레이어 목록을 받아옵니다.
	 * @param p 기준이 되는 플레이어입니다. 가까이에 있는 플레이어 목록에서 제외됩니다.
	 * @param HorizontalDis 수평 거리입니다.
	 * @param VerticalDis 수직 거리입니다.
	 * @return List<Player> 플레이어 목록
	 */
	public static List<Player> getNearbyPlayers(Player p, Integer HorizontalDis, Integer VerticalDis) {
		ArrayList<Player> Players = new ArrayList<Player>();
		for (Entity e : p.getNearbyEntities(HorizontalDis, VerticalDis, HorizontalDis)) {
			if (e instanceof Player) {
				Player player = (Player) e;

				if (AbilityWarThread.isGameTaskRunning()) {
					AbstractGame game = AbilityWarThread.getGame();
					if (game.isParticipating(player) && !game.getDeathManager().isEliminated(player)) {
						Players.add(player);
					}
				} else {
					Players.add(player);
				}
			}
		}

		return Players;
	}

	public static ArrayList<Player> getNearbyPlayers(Location l, Integer HorizontalDis, Integer VerticalDis) {
		if(ServerVersion.getVersion() >= 9) {
			ArrayList<Player> Players = new ArrayList<Player>();
			for (Entity e : l.getWorld().getNearbyEntities(l, HorizontalDis, VerticalDis, HorizontalDis)) {
				if (e instanceof Player) {
					Player player = (Player) e;

					if (AbilityWarThread.isGameTaskRunning()) {
						AbstractGame game = AbilityWarThread.getGame();
						if (game.isParticipating(player) && !game.getDeathManager().isEliminated(player)) {
							Players.add(player);
						}
					} else {
						Players.add(player);
					}
				}
			}

			return Players;
		} else {
			ArrayList<Player> Players = new ArrayList<Player>();
			for (Entity e : l.getWorld().getEntities()) {
				Location targetLocation = e.getLocation();
				if(NumberUtil.Subtract(Math.abs(targetLocation.getY()), Math.abs(l.getY())) <= VerticalDis) {
					if(NumberUtil.Subtract(Math.abs(targetLocation.getX()), Math.abs(l.getX())) <= HorizontalDis
					|| NumberUtil.Subtract(Math.abs(targetLocation.getZ()), Math.abs(l.getZ())) <= HorizontalDis) {
						if (e instanceof Player) {
							Player player = (Player) e;

							if (AbilityWarThread.isGameTaskRunning()) {
								AbstractGame game = AbilityWarThread.getGame();
								if (game.isParticipating(player) && !game.getDeathManager().isEliminated(player)) {
									Players.add(player);
								}
							} else {
								Players.add(player);
							}
						}
					}
				}
			}
			
			return Players;
		}
	}

}
