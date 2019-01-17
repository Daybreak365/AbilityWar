package Marlang.AbilityWar.GameManager;

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
import Marlang.AbilityWar.Ability.AbilityBase;
import Marlang.AbilityWar.Ability.AbilityList;
import Marlang.AbilityWar.Utils.AbilityWarThread;
import Marlang.AbilityWar.Utils.Messager;

public class AbilityGUI implements Listener {

	Player p;
	
	Player target;
	
	public AbilityGUI(Player p, Player target, AbilityWar Plugin) {
		this.p = p;
		this.target = target;
		Bukkit.getPluginManager().registerEvents(this, Plugin);
	}
	
	Integer PlayerPage = 1;
	
	Inventory AbilitySelectGUI;
	
	public void openAbilitySelectGUI(Integer page) {
		try {
			if ((AbilityList.values().size() - 1) / 36 + 1 < page)
				page = 1;
			if(page < 1) page = 1;
			AbilitySelectGUI = Bukkit.createInventory(null, 54, ChatColor.translateAlternateColorCodes('&', "&cAbilityWar &e능력 목록"));
			PlayerPage = page;
			int Count = 0;
			Integer MaxPage = ((AbilityList.values().size() - 1) / 36) + 1;
			
			for (String name : AbilityList.values()) {
				ItemStack is = new ItemStack(Material.IRON_BLOCK);
				ItemMeta im = is.getItemMeta();
				im.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&b" + name));
				im.setLore(Messager.getStringList(
						ChatColor.translateAlternateColorCodes('&', "&2» &f이 능력을 부여하려면 클릭하세요.")
						));
				is.setItemMeta(im);
				
				if (Count / 36 == page - 1) {
					AbilitySelectGUI.setItem(Count % 36, is);
				}
				Count++;
			}
			
			if(page > 1) {
				ItemStack previousPage = new ItemStack(Material.ARROW, 1);
				ItemMeta previousMeta = previousPage.getItemMeta();
				previousMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&b이전 페이지"));
				previousPage.setItemMeta(previousMeta);
				AbilitySelectGUI.setItem(48, previousPage);
			}
			
			if(page != MaxPage) {
				ItemStack nextPage = new ItemStack(Material.ARROW, 1);
				ItemMeta nextMeta = nextPage.getItemMeta();
				nextMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&b다음 페이지"));
				nextPage.setItemMeta(nextMeta);
				AbilitySelectGUI.setItem(50, nextPage);
			}

			ItemStack Page = new ItemStack(Material.PAPER, 1);
			ItemMeta PageMeta = Page.getItemMeta();
			PageMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&',
					"&6페이지 &e" + page + " &6/ &e" + MaxPage));
			Page.setItemMeta(PageMeta);
			AbilitySelectGUI.setItem(49, Page);
			
			p.openInventory(AbilitySelectGUI);
		} catch(Exception e) {}
	}
	
	@EventHandler
	public void onInventoryClick(InventoryClickEvent e) {
		if(e.getInventory().equals(AbilitySelectGUI)) {
			Player p = (Player) e.getWhoClicked();
			e.setCancelled(true);
			if(e.getCurrentItem() != null && e.getCurrentItem().hasItemMeta() && e.getCurrentItem().getItemMeta().hasDisplayName()) {
				if(e.getCurrentItem().getItemMeta().getDisplayName().equals(ChatColor.translateAlternateColorCodes('&', "&b이전 페이지"))) {
					openAbilitySelectGUI(PlayerPage - 1);
				} else if(e.getCurrentItem().getItemMeta().getDisplayName().equals(ChatColor.translateAlternateColorCodes('&', "&b다음 페이지"))) {
					openAbilitySelectGUI(PlayerPage + 1);
				}
			}
			
			if(e.getCurrentItem().getType().equals(Material.IRON_BLOCK)) {
				if(e.getCurrentItem() != null && e.getCurrentItem().hasItemMeta() && e.getCurrentItem().getItemMeta().hasDisplayName()) {
					String AbilityName = ChatColor.stripColor(e.getCurrentItem().getItemMeta().getDisplayName());
					
					Class<? extends AbilityBase> l = AbilityList.getByString(AbilityName);
					try {
						if(target.isOnline()) {
							if(l != null) {
								AbilityBase Ability = l.newInstance();
								Ability.setPlayer(target);
								
								if(AbilityWarThread.getGame().getInvincibility().isTimerRunning()) {
									Ability.setRestricted(true);
								} else {
									if(AbilityWarThread.getGame().isGameStarted()) {
										Ability.setRestricted(false);
									} else {
										Ability.setRestricted(true);
									}
								}
								
								AbilityWarThread.getGame().removeAbility(target);
								AbilityWarThread.getGame().addAbility(Ability);
							} else {
								throw new Exception();
							}
						} else {
							throw new Exception("해당 플레이어가 접속을 종료하였습니다.");
						}
					} catch(Exception ex) {
						if(ex.getMessage() != null && !ex.getMessage().isEmpty()) {
							Messager.sendErrorMessage(p, ex.getMessage());
						} else {
							Messager.sendErrorMessage(p, "설정 도중 오류가 발생하였습니다.");
						}
					}
					
					p.closeInventory();
					
					Messager.broadcastMessage(ChatColor.translateAlternateColorCodes('&', "&e" + p.getName() + "&a님이 &f" + target.getName() + "&a님에게 능력을 임의로 부여하였습니다."));
				}
			}
		}
	}
}
