package daybreak.abilitywar.game.manager.object;

import daybreak.abilitywar.AbilityWar;
import daybreak.abilitywar.game.games.mode.AbstractGame;
import daybreak.abilitywar.utils.library.item.ItemLib;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class InfiniteDurability implements Listener, AbstractGame.Observer {

	private final HashMap<Material, Boolean> hasDurability;

	public InfiniteDurability() {
		Bukkit.getPluginManager().registerEvents(this, AbilityWar.getPlugin());
		HashMap<Material, Boolean> hasDurability = new HashMap<>();
		for (Material material : Material.values()) {
			hasDurability.put(material, hasDurability(material));
		}
		this.hasDurability = hasDurability;
	}

	private final List<String> materialTool = Arrays.asList("AXE", "HOE", "PICKAXE", "SPADE", "SWORD", "BOOTS", "LEGGINGS", "CHESTPLATE", "HELMET");
	private final List<String> materialItem = Arrays.asList("BOW", "SHEARS", "FISHING_ROD", "FLINT_AND_STEEL");

	private boolean hasDurability(Material material) {
		String materialName = material.toString();
		String[] split = materialName.split("_");
		if (split.length > 1) {
			return materialTool.contains(split[1]);
		} else {
			return materialItem.contains(materialName);
		}
	}


	@EventHandler
	private void onItemDurability(PlayerInteractEvent e) {
		if (e.getItem() != null) {
			if (hasDurability.get(e.getItem().getType())) {
				ItemLib.setDurability(e.getItem(), (short) 0);
			}
		}
	}

	@EventHandler
	private void onArmorDurability(EntityDamageEvent e) {
		if (e.getEntity() instanceof Player) {
			Player p = (Player) e.getEntity();
			ItemStack boots = p.getInventory().getBoots();
			if (boots != null && hasDurability.get(boots.getType())) {
				ItemLib.setDurability(boots, (short) 0);
				p.getInventory().setBoots(boots);
			}
			ItemStack leggings = p.getInventory().getLeggings();
			if (leggings != null && hasDurability.get(leggings.getType())) {
				ItemLib.setDurability(leggings, (short) 0);
				p.getInventory().setLeggings(leggings);
			}
			ItemStack chestplate = p.getInventory().getChestplate();
			if (chestplate != null && hasDurability.get(chestplate.getType())) {
				ItemLib.setDurability(chestplate, (short) 0);
				p.getInventory().setChestplate(chestplate);
			}
			ItemStack helmet = p.getInventory().getHelmet();
			if (helmet != null && hasDurability.get(helmet.getType())) {
				ItemLib.setDurability(helmet, (short) 0);
				p.getInventory().setHelmet(helmet);
			}
		}
	}

	@Override
	public void update(AbstractGame.GAME_UPDATE update) {
		if (update.equals(AbstractGame.GAME_UPDATE.END)) {
			HandlerList.unregisterAll(this);
		}
	}

}
