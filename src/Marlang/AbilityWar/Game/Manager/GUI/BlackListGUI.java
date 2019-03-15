package Marlang.AbilityWar.Game.Manager.GUI;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.apache.commons.lang.ArrayUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;

import Marlang.AbilityWar.Ability.AbilityBase;
import Marlang.AbilityWar.Ability.AbilityList;
import Marlang.AbilityWar.Ability.AbilityManifest;
import Marlang.AbilityWar.Ability.AbilityManifest.Rank;
import Marlang.AbilityWar.Config.AbilityWarSettings;
import Marlang.AbilityWar.Utils.Messager;
import Marlang.AbilityWar.Utils.Library.SoundLib;
import Marlang.AbilityWar.Utils.Library.Item.ItemLib;
import Marlang.AbilityWar.Utils.Library.Item.ItemLib.ItemColor;
import Marlang.AbilityWar.Utils.Library.Item.MaterialLib;
import Marlang.AbilityWar.Utils.VersionCompat.ServerVersion;

/**
 * ?ä•?†• Í∏àÏ? GUI
 */
public class BlackListGUI implements Listener {
	
	private Player p;
	
	public BlackListGUI(Player p, Plugin Plugin) {
		this.p = p;
		Bukkit.getPluginManager().registerEvents(this, Plugin);
	}
	
	private Integer PlayerPage = 1;
	
	private Inventory BlackListGUI;
	
	public ArrayList<String> getBlackList() {
		ArrayList<String> list = new ArrayList<String>();
		
		for(String name : AbilityList.nameValues()) {
			if(!list.contains(name)) {
				list.add(name);
			}
		}
		
		for(String name : AbilityWarSettings.getBlackList()) {
			if(!list.contains(name)) {
				list.add(name);
			}
		}
		
		list.sort(new Comparator<String>() {
			
			public int compare(String obj1, String obj2) {
				return obj1.compareToIgnoreCase(obj2);
			}
			
		});
		
		return list;
	}
	
	public void openBlackListGUI(Integer page) {
		Integer MaxPage = ((AbilityList.nameValues().size() - 1) / 36) + 1;
		if (MaxPage < page) page = 1;
		if(page < 1) page = 1;
		BlackListGUI = Bukkit.createInventory(null, 54, ChatColor.translateAlternateColorCodes('&', "&c&l?úñ &8&l?ä•?†• Î∏îÎûôÎ¶¨Ïä§?ä∏ &c&l?úñ"));
		PlayerPage = page;
		int Count = 0;
		
		List<String> blackList = AbilityWarSettings.getBlackList();
		
		for(String name : getBlackList()) {
			ItemStack is;
			
			if(blackList.contains(name)) {
				is = ItemLib.WOOL.getItemStack(ItemColor.RED);
				ItemMeta im = is.getItemMeta();
				im.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&b" + name));
				im.setLore(Messager.getStringList(
						ChatColor.translateAlternateColorCodes('&', "&7?ù¥ ?ä•?†•?? ?ä•?†•?ùÑ Ï∂îÏ≤®?ï† ?ïå ?òà?ô∏?ê©?ãà?ã§."),
						ChatColor.translateAlternateColorCodes('&', "&b¬ª &f?òà?ô∏ Ï≤òÎ¶¨Î•? ?ï¥?†ú?ïò?†§Î©? ?Å¥Î¶??ïò?Ñ∏?öî.")
						));
				is.setItemMeta(im);
			} else {
				is = ItemLib.WOOL.getItemStack(ItemColor.LIME);
				ItemMeta im = is.getItemMeta();
				im.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&b" + name));
				im.setLore(Messager.getStringList(
						ChatColor.translateAlternateColorCodes('&', "&7?ù¥ ?ä•?†•?? ?ä•?†•?ùÑ Ï∂îÏ≤®?ï† ?ïå ?òà?ô∏?êòÏß? ?ïä?äµ?ãà?ã§."),
						ChatColor.translateAlternateColorCodes('&', "&b¬ª &f?òà?ô∏ Ï≤òÎ¶¨Î•? ?ïò?†§Î©? ?Å¥Î¶??ïò?Ñ∏?öî.")
						));
				is.setItemMeta(im);
			}
			
			if (Count / 36 == page - 1) {
				BlackListGUI.setItem(Count % 36, is);
			}
			Count++;
		}
		
		Integer RankCount = 37;
		Rank[] forEach = Rank.values();
		ArrayUtils.reverse(forEach);
		for(Rank r : forEach) {
			ItemStack RankItem;
			switch(r) {
				case D:
					RankItem = new ItemStack(Material.STONE);
					break;
				case C:
					RankItem = new ItemStack(Material.IRON_BLOCK);
					break;
				case B:
					RankItem = new ItemStack(Material.GOLD_BLOCK);
					break;
				case A:
					RankItem = new ItemStack(Material.DIAMOND_BLOCK);
					break;
				case S:
					RankItem = new ItemStack(Material.EMERALD_BLOCK);
					break;
				case GOD:
					if(ServerVersion.getVersion() >= 8) {
						RankItem = new ItemStack(Material.SEA_LANTERN);
					} else {
						RankItem = new ItemStack(Material.PACKED_ICE);
					}
					break;
				default:
					RankItem = new ItemStack(Material.BEACON);
					break;
			}
			ItemMeta RankMeta = RankItem.getItemMeta();
			String RankName = r.getRankName();
			RankMeta.setDisplayName(RankName);
			RankMeta.setLore(Messager.getStringList(
					ChatColor.translateAlternateColorCodes('&', "&fÎ™®Îì† " + RankName + " &f?ä•?†•?ùÑ ?òà?ô∏ Ï≤òÎ¶¨ ?ïò?†§Î©? Ï¢åÌÅ¥Î¶?,"),
					ChatColor.translateAlternateColorCodes('&', "&fÎ™®Îì† " + RankName + " &f?ä•?†•?ùÑ ?òà?ô∏ Ï≤òÎ¶¨ ?ï¥?†ú?ïò?†§Î©? ?ö∞?Å¥Î¶??ùÑ ?ï¥Ï£ºÏÑ∏?öî.")
					));
			RankItem.setItemMeta(RankMeta);
			BlackListGUI.setItem(RankCount, RankItem);
			RankCount++;
		}
		
		if(page > 1) {
			ItemStack previousPage = new ItemStack(Material.ARROW, 1);
			ItemMeta previousMeta = previousPage.getItemMeta();
			previousMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&b?ù¥?†Ñ ?éò?ù¥Ïß?"));
			previousPage.setItemMeta(previousMeta);
			BlackListGUI.setItem(48, previousPage);
		}
		
		if(page != MaxPage) {
			ItemStack nextPage = new ItemStack(Material.ARROW, 1);
			ItemMeta nextMeta = nextPage.getItemMeta();
			nextMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&b?ã§?ùå ?éò?ù¥Ïß?"));
			nextPage.setItemMeta(nextMeta);
			BlackListGUI.setItem(50, nextPage);
		}

		ItemStack Page = new ItemStack(Material.PAPER, 1);
		ItemMeta PageMeta = Page.getItemMeta();
		PageMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&',
				"&6?éò?ù¥Ïß? &e" + page + " &6/ &e" + MaxPage));
		Page.setItemMeta(PageMeta);
		BlackListGUI.setItem(49, Page);
		
		p.openInventory(BlackListGUI);
	}

	@EventHandler
	public void onInventoryClose(InventoryCloseEvent e) {
		if(e.getInventory().equals(this.BlackListGUI)) {
			HandlerList.unregisterAll(this);
			AbilityWarSettings.Save();
		}
	}
	
	@EventHandler
	public void onInventoryClick(InventoryClickEvent e) {
		if(e.getInventory().equals(BlackListGUI)) {
			e.setCancelled(true);
			if(e.getCurrentItem() != null && e.getCurrentItem().hasItemMeta() && e.getCurrentItem().getItemMeta().hasDisplayName()) {
				if(e.getCurrentItem().getItemMeta().getDisplayName().equals(ChatColor.translateAlternateColorCodes('&', "&b?ù¥?†Ñ ?éò?ù¥Ïß?"))) {
					openBlackListGUI(PlayerPage - 1);
				} else if(e.getCurrentItem().getItemMeta().getDisplayName().equals(ChatColor.translateAlternateColorCodes('&', "&b?ã§?ùå ?éò?ù¥Ïß?"))) {
					openBlackListGUI(PlayerPage + 1);
				} else {
					String ItemName = e.getCurrentItem().getItemMeta().getDisplayName();
					
					if(ItemLib.WOOL.compareType(e.getCurrentItem().getType())) {
						String StripItemName = ChatColor.stripColor(ItemName);
						if(StripItemName.equals(StripItemName))
						if(MaterialLib.RED_WOOL.compareMaterial(e.getCurrentItem())) {
							AbilityWarSettings.removeBlackList(StripItemName);
							SoundLib.ENTITY_EXPERIENCE_ORB_PICKUP.playSound(p);
							openBlackListGUI(PlayerPage);
						} else if(MaterialLib.LIME_WOOL.compareMaterial(e.getCurrentItem())) {
							AbilityWarSettings.addBlackList(StripItemName);
							SoundLib.BLOCK_ANVIL_LAND.playSound(p);
							openBlackListGUI(PlayerPage);
						}
					} else {
						for(Rank r : Rank.values()) {
							if(ItemName.equals(r.getRankName())) {
								if(e.getClick().equals(ClickType.LEFT)) {
									addBlacklistAll(r);
									SoundLib.BLOCK_ANVIL_LAND.playSound(p);
									openBlackListGUI(PlayerPage);
								} else if(e.getClick().equals(ClickType.RIGHT)) {
									removeBlacklistAll(r);
									SoundLib.ENTITY_EXPERIENCE_ORB_PICKUP.playSound(p);
									openBlackListGUI(PlayerPage);
								}
							}
						}
					}
				}
			}
		}
	}
	
	private List<String> getAbilities(Rank r) {
		List<String> list = new ArrayList<String>();
		
		for(String name : AbilityList.nameValues()) {
			Class<? extends AbilityBase> clazz = AbilityList.getByString(name);
			AbilityManifest manifest = clazz.getAnnotation(AbilityManifest.class);
			if(manifest != null) {
				if(manifest.Rank().equals(r)) {
					list.add(name);
				}
			}
		}
		
		return list;
	}
	
	private void addBlacklistAll(Rank r) {
		for(String name : getAbilities(r)) {
			AbilityWarSettings.addBlackList(name);
		}
	}
	
	private void removeBlacklistAll(Rank r) {
		for(String name : getAbilities(r)) {
			AbilityWarSettings.removeBlackList(name);
		}
	}
	
}
