package DayBreak.AbilityWar.Game.Manager;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import DayBreak.AbilityWar.Utils.Library.Item.ItemLib;

public class InfiniteDurability implements Listener {

	/**
	 * 내구도 Listener
	 */
	@EventHandler
	public void onItemDurability(PlayerInteractEvent e) {
		if(e.getItem() != null) {
			if(hasDurability(e.getItem().getType())) {
				ItemLib.setDurability(e.getItem(), (short) 0);
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
				ItemLib.setDurability(Boots, (short) 0);
				p.getInventory().setBoots(Boots);
			}
			
			ItemStack Leggings = p.getInventory().getLeggings();
			if(Leggings != null && hasDurability(Leggings.getType())) {
				ItemLib.setDurability(Leggings, (short) 0);
				p.getInventory().setLeggings(Leggings);
			}
			
			ItemStack Chestplate = p.getInventory().getChestplate();
			if(Chestplate != null && hasDurability(Chestplate.getType())) {
				ItemLib.setDurability(Chestplate, (short) 0);
				p.getInventory().setChestplate(Chestplate);
			}
			
			ItemStack Helmet = p.getInventory().getHelmet();
			if(Helmet != null && hasDurability(Helmet.getType())) {
				ItemLib.setDurability(Helmet, (short) 0);
				p.getInventory().setHelmet(Helmet);
			}
		}
	}
	
	private final String[] MaterialBased = {"AXE", "HOE", "PICKAXE", "SPADE", "SWORD", "BOOTS", "LEGGINGS", "CHESTPLATE", "HELMET"};
	private final String[] Item = {"BOW", "SHEARS", "FISHING_ROD", "FLINT_AND_STEEL"};
	
	private boolean hasDurability(Material m) {
		String materialName = m.toString();
		
		String[] split = materialName.split("_");
		if(split.length > 1) {
			for(String compare : MaterialBased) {
				if(split[1].equalsIgnoreCase(compare)) {
					return true;
				}
			}
		} else {
			for(String compare : Item) {
				if(materialName.equalsIgnoreCase(compare)) {
					return true;
				}
			}
		}
		
		return false;
	}
	
}
