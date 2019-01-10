package Marlang.AbilityWar.Utils;

import java.util.ArrayList;

import org.bukkit.entity.Damageable;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

/**
 * Distance Util
 * @author _Marlang ¸»¶û
 */
public class DistanceUtil {
	
	public static ArrayList<Damageable> getNearbyDamageableEntities(Player p, Integer Xdis, Integer Ydis) {
		ArrayList<Damageable> Entities = new ArrayList<Damageable>();
		for(Entity e : p.getNearbyEntities(Xdis, Ydis, Xdis)) {
			if(e instanceof Damageable) {
				if(e instanceof Player) {
					Player player = (Player) e;
					if(AbilityWarThread.getGame().getSpectators().contains(player)) {
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
		for(Entity e : p.getNearbyEntities(Xdis, Ydis, Xdis)) {
			if(e instanceof Damageable) {
				if(e instanceof Player) {
					Player player = (Player) e;
					if(AbilityWarThread.getGame().getSpectators().contains(player)) {
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
		for(Entity e : p.getNearbyEntities(Xdis, Ydis, Xdis)) {
			if(e instanceof Player) {
				Player player = (Player) e;
				if(AbilityWarThread.getGame().getSpectators().contains(player)) {
					continue;
				} else {
					Players.add(player);
				}
			}
		}
		
		return Players;
	}
	
}
