package Marlang.AbilityWar.Utils;

import java.util.ArrayList;
import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Damageable;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import Marlang.AbilityWar.GameManager.Game;

/**
 * Location Util
 * 
 * @author _Marlang ¸»¶û
 */
public class LocationUtil {

	public static boolean isInCircle(Location location, Location center, Double radius) {
		Double X = location.getX();
		Double Z = location.getZ();
		
		Double Center_X = center.getX();
		Double Center_Z = center.getZ();
		
		return ((X - Center_X) * (X - Center_X)) + ((Z - Center_Z) * (Z - Center_Z)) < (radius * radius);
	}
	
	public static Player getNearestPlayer(Player Base) {
		Location l = Base.getLocation();
		Player closestPlayer = null;
		double closestDistance = 0.0;

		for (Player p : Bukkit.getOnlinePlayers()) {
			if (Game.getSpectators().contains(p.getName())) {
				continue;
			}

			if (p.equals(Base))
				continue;

			double distance = p.getLocation().distanceSquared(l);
			if (closestPlayer == null || distance < closestDistance) {
				closestDistance = distance;
				closestPlayer = p;
			}
		}

		return closestPlayer;
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
					Player player = (Player) e;
					if (Game.getSpectators().contains(player.getName())) {
						continue;
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
					Player player = (Player) e;
					if (Game.getSpectators().contains(player.getName())) {
						continue;
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
					Player player = (Player) e;
					if (Game.getSpectators().contains(player.getName())) {
						continue;
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
				if (Game.getSpectators().contains(player.getName())) {
					continue;
				} else {
					Players.add(player);
				}
			}
		}

		return Players;
	}

	public static ArrayList<Player> getNearbyPlayers(Location l, Integer Xdis, Integer Ydis) {
		ArrayList<Player> Entities = new ArrayList<Player>();
		for (Entity e : l.getWorld().getNearbyEntities(l, Xdis, Ydis, Xdis)) {
			if (e instanceof Player) {
				Player player = (Player) e;
				if (Game.getSpectators().contains(player.getName())) {
					continue;
				} else {
					Entities.add(player);
				}
			}
		}

		return Entities;
	}

}
