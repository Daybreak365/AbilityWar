package Marlang.AbilityWar.GameManager.Manager;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

public class InfiniteDurability implements Listener {

	/**
	 * 내구도 Listener
	 */
	@EventHandler
	public void onItemDurability(PlayerInteractEvent e) {
		if(e.getItem() != null) {
			if(hasDurability(e.getItem().getType())) {
				e.getItem().setDurability((short) 0);
			}
		}
	}

	/**
	 * 내구도 Listener
	 */
	@EventHandler
	public void onArmorDurability(EntityDamageEvent e) {
		if(e.getEntity() instanceof Player) {
			Player p = (Player) e.getEntity();
			
			ItemStack Boots = p.getInventory().getBoots();
			if(Boots != null && hasDurability(Boots.getType())) {
				Boots.setDurability((short) 0);
				p.getInventory().setBoots(Boots);
			}
			
			ItemStack Leggings = p.getInventory().getLeggings();
			if(Leggings != null && hasDurability(Leggings.getType())) {
				Leggings.setDurability((short) 0);
				p.getInventory().setLeggings(Leggings);
			}
			
			ItemStack Chestplate = p.getInventory().getChestplate();
			if(Chestplate != null && hasDurability(Chestplate.getType())) {
				Chestplate.setDurability((short) 0);
				p.getInventory().setChestplate(Chestplate);
			}
			
			ItemStack Helmet = p.getInventory().getHelmet();
			if(Helmet != null && hasDurability(Helmet.getType())) {
				Helmet.setDurability((short) 0);
				p.getInventory().setHelmet(Helmet);
			}
		}
	}
	
	private boolean hasDurability(Material m) {
		String materialName = m.toString();
		
		String[] split = materialName.split("_");
		if(split.length > 1) {
			String[] Item = {"AXE", "HOE", "PICKAXE", "SPADE", "SWORD", "BOOTS", "LEGGINGS", "CHESTPLATE", "HELMET"};
			for(String compare : Item) {
				if(split[1].equalsIgnoreCase(compare)) {
					return true;
				}
			}
		} else {
			String[] Item = {"BOW", "SHEARS", "FISHING_ROD", "FLINT_AND_STEEL"};
			for(String compare : Item) {
				if(materialName.equalsIgnoreCase(compare)) {
					return true;
				}
			}
		}
		
		return false;
	}
	
}
