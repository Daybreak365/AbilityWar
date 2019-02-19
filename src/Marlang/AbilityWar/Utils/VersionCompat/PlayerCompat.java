package Marlang.AbilityWar.Utils.VersionCompat;

import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;

public class PlayerCompat {
	
	public static void addPotionEffect(Player p, PotionEffectType potion, int duration, int amplifier, boolean force) {
		if(ServerVersion.getVersion() >= potion.getVersion()) {
			org.bukkit.potion.PotionEffectType effect = org.bukkit.potion.PotionEffectType.getByName(potion.toString());
			if(effect != null) {
				p.addPotionEffect(new PotionEffect(effect, duration, amplifier), force);
			}
		}
	}
	
	public static void removePotionEffect(Player p, PotionEffectType potion) {
		if(ServerVersion.getVersion() >= potion.getVersion()) {
			org.bukkit.potion.PotionEffectType effect = org.bukkit.potion.PotionEffectType.getByName(potion.toString());
			if(effect != null) {
				p.removePotionEffect(effect);
			}
		}
	}
	
	@SuppressWarnings("deprecation")
	public static ItemStack getItemInHand(Player p) {
		if(ServerVersion.getVersion() >= 9) {
			return p.getInventory().getItemInMainHand();
		} else {
			return p.getInventory().getItemInHand();
		}
	}
	
	@SuppressWarnings("deprecation")
	public static double getMaxHealth(Player p) {
		if(ServerVersion.getVersion() >= 9) {
			return p.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue();
		} else {
			return p.getMaxHealth();
		}
	}
	
}
