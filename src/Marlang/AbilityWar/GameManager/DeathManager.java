package Marlang.AbilityWar.GameManager;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.PlayerDeathEvent;

import Marlang.AbilityWar.GameManager.Module.Module;

/**
 * Death Manager
 * @author _Marlang ¸»¶û
 */
public class DeathManager extends Module implements Listener {
	
	public DeathManager() {
		RegisterListener(this);
	}
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onDeath(PlayerDeathEvent e) {
		Player Victim = e.getEntity();
		Player Killer = Victim.getKiller();
		if(Victim.getLastDamageCause() != null) {
			DamageCause Cause = Victim.getLastDamageCause().getCause();

			if(Killer != null) {
				e.setDeathMessage(ChatColor.translateAlternateColorCodes('&', "&a" + Killer.getName() + "&f´ÔÀÌ &c" + Victim.getName() + "&f´ÔÀ» Á×¿´½À´Ï´Ù."));
			} else {
				if(Cause.equals(DamageCause.CONTACT)) {
					e.setDeathMessage(ChatColor.translateAlternateColorCodes('&', "&c" + Victim.getName() + "&f´ÔÀÌ Âñ·Á Á×¾ú½À´Ï´Ù."));
				} else if(Cause.equals(DamageCause.FALL)) {
					e.setDeathMessage(ChatColor.translateAlternateColorCodes('&', "&c" + Victim.getName() + "&f´ÔÀÌ ¶³¾îÁ® Á×¾ú½À´Ï´Ù."));
				} else if(Cause.equals(DamageCause.FALLING_BLOCK)) {
					e.setDeathMessage(ChatColor.translateAlternateColorCodes('&', "&c" + Victim.getName() + "&f´ÔÀÌ ¶³¾îÁö´Â ºí·Ï¿¡ ¸Â¾Æ Á×¾ú½À´Ï´Ù."));
				} else if(Cause.equals(DamageCause.SUFFOCATION)) {
					e.setDeathMessage(ChatColor.translateAlternateColorCodes('&', "&c" + Victim.getName() + "&f´ÔÀÌ ³¢¿© Á×¾ú½À´Ï´Ù."));
				} else if(Cause.equals(DamageCause.DROWNING)) {
					e.setDeathMessage(ChatColor.translateAlternateColorCodes('&', "&c" + Victim.getName() + "&f´ÔÀÌ ¹°¿¡ ºüÁ® Á×¾ú½À´Ï´Ù."));
				} else if(Cause.equals(DamageCause.ENTITY_EXPLOSION)) {
					e.setDeathMessage(ChatColor.translateAlternateColorCodes('&', "&c" + Victim.getName() + "&f´ÔÀÌ Æø¹ßÇÏ¿´½À´Ï´Ù."));
				} else if(Cause.equals(DamageCause.LAVA)) {
					e.setDeathMessage(ChatColor.translateAlternateColorCodes('&', "&c" + Victim.getName() + "&f´ÔÀÌ ¿ë¾Ï¿¡ ºüÁ® Á×¾ú½À´Ï´Ù."));
				} else if(Cause.equals(DamageCause.FIRE) || Cause.equals(DamageCause.FIRE_TICK)) {
					e.setDeathMessage(ChatColor.translateAlternateColorCodes('&', "&c" + Victim.getName() + "&f´ÔÀÌ ³ë¸©³ë¸©ÇÏ°Ô ±¸¿öÁ³½À´Ï´Ù."));
				} else {
					e.setDeathMessage(ChatColor.translateAlternateColorCodes('&', "&c" + Victim.getName() + "&f´ÔÀÌ Á×¾ú½À´Ï´Ù."));
				}
			}
		} else {
			e.setDeathMessage(ChatColor.translateAlternateColorCodes('&', "&c" + Victim.getName() + "&f´ÔÀÌ Á×¾ú½À´Ï´Ù."));
		}
		
	}
	
}
