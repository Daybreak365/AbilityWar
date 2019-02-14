package Marlang.AbilityWar.Utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Damageable;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import Marlang.AbilityWar.GameManager.Game.AbstractGame;

/**
 * Location Util
 * @author _Marlang 말랑
 */
public class LocationUtil {
	
	public static boolean isInCircle(Location location, Location center, Double radius) {
		Double X = location.getX();
		Double Z = location.getZ();
		
		Double Center_X = center.getX();
		Double Center_Z = center.getZ();
		
		return ((X - Center_X) * (X - Center_X)) + ((Z - Center_Z) * (Z - Center_Z)) < (radius * radius);
	}
	
	public static ArrayList<Block> getBlocks(Location location, Integer radius, boolean hollow, boolean top) {
		ArrayList<Block> Blocks = new ArrayList<Block>();
		
		Integer X = location.getBlockX();
		Integer Y = location.getBlockY();
		Integer	Z = location.getBlockZ();
		
		for (Integer x = X - radius; x <= X + radius; x++) {
			for (Integer y = Y - radius; y <= Y + radius; y++) {
				for (Integer z = Z - radius; z <= Z + radius; z++) {
					Location l = new Location(location.getWorld(), x, y, z);
					double distance = location.distanceSquared(l);
					if (distance < (radius * radius) && !(hollow && distance < ((radius - 1) * (radius - 1)))) {
						if(top) {
							Location highest = l.getWorld().getHighestBlockAt(l).getLocation();
							Block highestBlock = highest.getBlock();
							if(highestBlock.getType().equals(Material.AIR)) {
								Block lowBlock = highest.clone().subtract(0, 1, 0).getBlock();
								if(!lowBlock.getType().equals(Material.AIR)) {
									if(!Blocks.contains(lowBlock)) {
										Blocks.add(lowBlock);
									}
								}
							} else {
								if(!Blocks.contains(highestBlock)) {
									Blocks.add(highestBlock);
								}
							}
						} else {
							Block block = l.getBlock();
							if(!block.getType().equals(Material.AIR)) {
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
	 * 기준에서 가장 가까운 플레이어를 받아옵니다.
	 * @param Base 기준이 되는 플레이어.
	 */
	public static Player getNearestPlayer(Player Base) {
		List<Player> Players;
		
		if(AbilityWarThread.isGameTaskRunning()) {
			AbstractGame game = AbilityWarThread.getGame();
			Players = game.getParticipants().stream().filter(p -> !game.getDeathManager().isEliminated(p)).collect(Collectors.toList());
		} else {
			Players = new ArrayList<Player>(Bukkit.getOnlinePlayers());
		}
		
		Location l = Base.getLocation();
		Double Distance = Double.MAX_VALUE;
		Player player = null;
		for(Player p : Players) {
			if(!p.equals(Base)) {
				Double d = l.distance(p.getLocation());
				if(d < Distance) {
					Distance = d;
					player = p;
				}
			}
		}
		
		return player;
	}

	public static ArrayList<Location> getCircle(Location center, int radius, int amount, boolean HighestY) {
		return getCircle(center, Double.valueOf(radius), amount, HighestY);
	}

	public static ArrayList<Location> getCircle(Location center, double radius, int amount, boolean HighestY) {
		World world = center.getWorld();
		double increment = (2 * Math.PI) / amount;
		ArrayList<Location> locations = new ArrayList<Location>();
		
		for (int i = 0; i < amount; i++) {
			double angle = i * increment;
			double x = center.getX() + (radius * Math.cos(angle));
			double z = center.getZ() + (radius * Math.sin(angle));
			
			if(HighestY) {
				locations.add(new Location(world, x, world.getHighestBlockYAt((int) x, (int) z) + 1, z));
			} else {
				locations.add(new Location(world, x, center.getY(), z));
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

	public static ArrayList<Damageable> getNearbyDamageableEntities(Player p, Integer Xdis, Integer Ydis) {
		ArrayList<Damageable> Entities = new ArrayList<Damageable>();
		for (Entity e : p.getNearbyEntities(Xdis, Ydis, Xdis)) {
			if (e instanceof Damageable) {
				if (e instanceof Player) {
					if(AbilityWarThread.isGameTaskRunning()) {
						AbstractGame game = AbilityWarThread.getGame();
						Player player = (Player) e;
						if(!game.getParticipants().contains(player) || game.getDeathManager().isEliminated(player)) {
							continue;
						}
					}
				}
				Entities.add((Damageable) e);
			}
		}

		return Entities;
	}

	public static ArrayList<Damageable> getNearbyDamageableEntities(Location l, Integer Xdis, Integer Ydis) {
		ArrayList<Damageable> Entities = new ArrayList<Damageable>();
		for (Entity e : l.getWorld().getNearbyEntities(l, Xdis, Ydis, Xdis)) {
			if (e instanceof Damageable) {
				if (e instanceof Player) {
					if(AbilityWarThread.isGameTaskRunning()) {
						AbstractGame game = AbilityWarThread.getGame();
						Player player = (Player) e;
						if(!game.getParticipants().contains(player) || game.getDeathManager().isEliminated(player)) {
							continue;
						}
					}
				}
				Entities.add((Damageable) e);
			}
		}

		return Entities;
	}

	public static ArrayList<Damageable> getNearbyDamageableEntities(Entity p, Integer Xdis, Integer Ydis) {
		ArrayList<Damageable> Entities = new ArrayList<Damageable>();
		for (Entity e : p.getNearbyEntities(Xdis, Ydis, Xdis)) {
			if (e instanceof Damageable) {
				if (e instanceof Player) {
					if(AbilityWarThread.isGameTaskRunning()) {
						AbstractGame game = AbilityWarThread.getGame();
						Player player = (Player) e;
						if(!game.getParticipants().contains(player) || game.getDeathManager().isEliminated(player)) {
							continue;
						}
					}
				}
				Entities.add((Damageable) e);
			}
		}

		return Entities;
	}

	public static ArrayList<Player> getNearbyPlayers(Player p, Integer Xdis, Integer Ydis) {
		ArrayList<Player> Players = new ArrayList<Player>();
		for (Entity e : p.getNearbyEntities(Xdis, Ydis, Xdis)) {
			if (e instanceof Player) {
				Player player = (Player) e;

				if(AbilityWarThread.isGameTaskRunning()) {
					AbstractGame game = AbilityWarThread.getGame();
					if(game.getParticipants().contains(player) && !game.getDeathManager().isEliminated(player)) {
						Players.add(player);
					}
				} else {
					Players.add(player);
				}
			}
		}

		return Players;
	}

	public static ArrayList<Player> getNearbyPlayers(Location l, Integer Xdis, Integer Ydis) {
		ArrayList<Player> Players = new ArrayList<Player>();
		for (Entity e : l.getWorld().getNearbyEntities(l, Xdis, Ydis, Xdis)) {
			if (e instanceof Player) {
				Player player = (Player) e;
				
				if(AbilityWarThread.isGameTaskRunning()) {
					AbstractGame game = AbilityWarThread.getGame();
					if(game.getParticipants().contains(player) && !game.getDeathManager().isEliminated(player)) {
						Players.add(player);
					}
				} else {
					Players.add(player);
				}
			}
		}

		return Players;
	}

}
