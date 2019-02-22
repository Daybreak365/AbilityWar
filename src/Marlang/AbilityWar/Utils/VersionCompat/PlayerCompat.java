package Marlang.AbilityWar.Utils.VersionCompat;

import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class PlayerCompat {
	
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
