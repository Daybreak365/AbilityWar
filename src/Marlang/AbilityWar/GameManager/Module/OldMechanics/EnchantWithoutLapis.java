package Marlang.AbilityWar.GameManager.Module.OldMechanics;

import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.enchantment.EnchantItemEvent;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.EnchantingInventory;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.Dye;

import Marlang.AbilityWar.Config.AbilityWarSettings;
import Marlang.AbilityWar.GameManager.Module.Module;
import Marlang.AbilityWar.Utils.Thread.AbilityWarThread;

public class EnchantWithoutLapis extends Module implements Listener {

	public EnchantWithoutLapis() {
		RegisterListener(this);
	}

	@EventHandler
	public void onEnchant(EnchantItemEvent e) {
		if (isEnabled()) {
			if (AbilityWarThread.isGameTaskRunning()) {
				EnchantingInventory i = (EnchantingInventory) e.getInventory();
				i.setSecondary(getLapis());
			}
		}
	}

	@EventHandler
	public void onInventoryClose(InventoryCloseEvent e) {
		if (isEnabled()) {
			if (AbilityWarThread.isGameTaskRunning()) {
				Inventory i = e.getInventory();

				if (i != null && i.getType().equals(InventoryType.ENCHANTING)) {
					((EnchantingInventory) i).setSecondary(new ItemStack(Material.AIR));
				}
			}
		}
	}

	@EventHandler
	public void onInventoryOpen(InventoryOpenEvent e) {
		if (isEnabled()) {
			if (AbilityWarThread.isGameTaskRunning()) {
				Inventory i = e.getInventory();

				if (i != null && i.getType().equals(InventoryType.ENCHANTING)) {
					((EnchantingInventory) i).setSecondary(getLapis());
				}
			}
		}
	}

	@EventHandler
	public void onInventoryClick(InventoryClickEvent e) {
		if(isEnabled()) {
			if(AbilityWarThread.isGameTaskRunning()) {
				if(e.getCurrentItem() != null) {
					if(e.getCurrentItem().getType().equals(Material.INK_SACK) && e.getRawSlot() == 1) {
						e.setCancelled(true);
					} else if(e.getCursor() != null && e.getCursor().getType().equals(Material.INK_SACK) && e.getClick().equals(ClickType.DOUBLE_CLICK)) {
						e.setCancelled(true);
					}
				}
			}
		}
	}

	private boolean isEnabled() {
		return AbilityWarSettings.getOldEnchant();
	}

	private ItemStack getLapis() {
		Dye dye = new Dye();
		dye.setColor(DyeColor.BLUE);
		return dye.toItemStack(64);
	}

}
