package Marlang.AbilityWar.Config;

import java.util.ArrayList;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import Marlang.AbilityWar.AbilityWar;
import Marlang.AbilityWar.Config.Nodes.ConfigNodes;
import Marlang.AbilityWar.Utils.Messager;

/**
 * 콘피그 설정 마법사
 * @author _Marlang 말랑
 */
public class SettingWizard implements Listener {
	
	static Inventory KitGUI;
	
	public static void openKitGUI(Player p) {
		KitGUI = Bukkit.createInventory(p, 45, ChatColor.translateAlternateColorCodes('&', "&2게임 킷 설정"));
		
		ItemStack Confirm = new ItemStack(Material.WOOL, 1, (short) 5);
		ItemMeta ConfirmMeta = Confirm.getItemMeta();
		ConfirmMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&a확인"));
		Confirm.setItemMeta(ConfirmMeta);
		
		ItemStack Deco = new ItemStack(Material.STAINED_GLASS_PANE);
		ItemMeta DecoMeta = Deco.getItemMeta();
		DecoMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&f"));
		Deco.setItemMeta(DecoMeta);
		
		KitGUI.setItem(36, Deco);
		KitGUI.setItem(37, Deco);
		KitGUI.setItem(38, Deco);
		KitGUI.setItem(39, Deco);
		KitGUI.setItem(40, Confirm);
		KitGUI.setItem(41, Deco);
		KitGUI.setItem(42, Deco);
		KitGUI.setItem(43, Deco);
		KitGUI.setItem(44, Deco);
		
		for(ItemStack is : AbilityWar.getSetting().getDefaultKit()) {
			KitGUI.addItem(is);
		}
		
		p.openInventory(KitGUI);
	}
	
	@EventHandler
	public void onInventoryClick(InventoryClickEvent e) {
		if(e.getInventory().equals(KitGUI)) {
			Player p = (Player) e.getWhoClicked();
			
			if(e.getCurrentItem() != null) {
				if(e.getCurrentItem().hasItemMeta() && e.getCurrentItem().getItemMeta().hasDisplayName()) {
					if(e.getCurrentItem().getItemMeta().getDisplayName().equals(ChatColor.translateAlternateColorCodes('&', "&a확인"))) {
						e.setCancelled(true);
						AbilityWar.getSetting().setNewProperty(ConfigNodes.Game_Kit, getItemUntil(KitGUI, 35));
						p.closeInventory();
						Messager.sendMessage(p, ChatColor.translateAlternateColorCodes('&', "&2게임 킷 &a설정을 마쳤습니다."));
					} else if(e.getCurrentItem().getItemMeta().getDisplayName().equals(ChatColor.translateAlternateColorCodes('&', "&f"))) {
						e.setCancelled(true);
					}
				}
			}
		}
	}
	
	public ArrayList<ItemStack> getItemUntil(Inventory inv, Integer Count) {
		ArrayList<ItemStack> List = new ArrayList<ItemStack>();
		
		for(int i = 0; i <= Count; i++) {
			if(inv.getItem(i) != null && !inv.getItem(i).getType().equals(Material.AIR)) {
				List.add(inv.getItem(i));
			}
		}
		
		return List;
	}
	
}
