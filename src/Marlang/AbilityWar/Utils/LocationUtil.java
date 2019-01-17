package Marlang.AbilityWar.Utils;

import java.util.ArrayList;

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

	public static ArrayList<Location> getCircle(Location center, double radius, int amount) {
		World world = center.getWorld();
		double increment = (2 * Math.PI) / amount;
		ArrayList<Location> locations = new ArrayList<Location>();
		for (int i = 0; i < amount; i++) {
			double angle = i * increment;
			double x = center.getX() + (radius * Math.cos(angle));
			double z = center.getZ() + (radius * Math.sin(angle));
			Location l = new Location(world, x, center.getY(), z);
			locations.add(world.getHighestBlockAt(l).getLocation().add(0, 1, 0));
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

}
