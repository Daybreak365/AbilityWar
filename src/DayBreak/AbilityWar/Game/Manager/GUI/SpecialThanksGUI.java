package DayBreak.AbilityWar.Game.Manager.GUI;

import java.util.ArrayList;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.plugin.Plugin;

import DayBreak.AbilityWar.Utils.Messager;
import DayBreak.AbilityWar.Utils.Data.MojangAPI;
import DayBreak.AbilityWar.Utils.Library.Item.ItemLib;

/**
 * 기여자 목록 GUI
 */
public class SpecialThanksGUI implements Listener {
	
	private static final SpecialThank[] SpecialThanks = {
				new SpecialThank("f6cef0829b7e48c1a973532389b6e3e1", "다량의 능력 아이디어를 제공해주셨습니다."),
				new SpecialThank("ecb53e2ffdf34089ae3486cff3fc5f34", "능력 아이디어 제공 및 코드 개선에 도움을 주셨습니다."),
				new SpecialThank("48b5f9e0ba544368ab4dc6f2be56d9f4", "테스팅에 도움을 주셨습니다."),
				new SpecialThank("101ceb32a2bc4dbd9d32291c86b66eca", "테스팅에 도움을 주셨습니다."),
				new SpecialThank("507fc49666fb43489200251f48bf4719", "몇몇 업데이트에 기여하셨습니다.")
			};
	
	public static class SpecialThank {
		
		private String name;
		private String[] role;
		
		public SpecialThank(String UUID, String... role) {
			try {
				this.name = MojangAPI.getNickname(UUID);
			} catch (Exception e) {
				this.name = "Error";
			}
			this.role = role;
		}
		
		public String getName() {
			return name;
		}
		
		public ArrayList<String> getRole() {
			ArrayList<String> list = new ArrayList<String>();
			for(String s : role) {
				list.add(ChatColor.translateAlternateColorCodes('&', "&f" + s));
			}
			
			return list;
		}
		
	}
	
	private final Player p;
	
	public SpecialThanksGUI(Player p, Plugin plugin) {
		this.p = p;
		Bukkit.getPluginManager().registerEvents(this, plugin);
	}
	
	private int PlayerPage = 1;
	
	private Inventory STGUI;
	
	public void openGUI(Integer page) {
		Integer MaxPage = ((SpecialThanks.length - 1) / 18) + 1;
		if (MaxPage < page) page = 1;
		if(page < 1) page = 1;
		STGUI = Bukkit.createInventory(null, 27, ChatColor.translateAlternateColorCodes('&', "&c&l✿ &0&lSpecial Thanks &c&l✿"));
		PlayerPage = page;
		int Count = 0;
		
		for (SpecialThank st : SpecialThanks) {
			ItemStack is = ItemLib.getHead(st.getName());
			SkullMeta im = (SkullMeta) is.getItemMeta();
			if(!st.getName().equals("Error")) {
				im.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&e" + st.getName()));
				
				im.setLore(st.getRole());
			} else {
				im.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&c오류"));

				im.setLore(Messager.getStringList(ChatColor.translateAlternateColorCodes('&', "&bMojang API&f에 연결할 수 없습니다.")));
			}
			
			is.setItemMeta(im);
			
			if (Count / 18 == page - 1) {
				STGUI.setItem(Count % 18, is);
			}
			Count++;
		}
		
		if(page > 1) {
			ItemStack previousPage = new ItemStack(Material.ARROW, 1);
			ItemMeta previousMeta = previousPage.getItemMeta();
			previousMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&b이전 페이지"));
			previousPage.setItemMeta(previousMeta);
			STGUI.setItem(21, previousPage);
		}
		
		if(page != MaxPage) {
			ItemStack nextPage = new ItemStack(Material.ARROW, 1);
			ItemMeta nextMeta = nextPage.getItemMeta();
			nextMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&b다음 페이지"));
			nextPage.setItemMeta(nextMeta);
			STGUI.setItem(23, nextPage);
		}

		ItemStack Page = new ItemStack(Material.PAPER, 1);
		ItemMeta PageMeta = Page.getItemMeta();
		PageMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&',
				"&6페이지 &e" + page + " &6/ &e" + MaxPage));
		Page.setItemMeta(PageMeta);
		STGUI.setItem(22, Page);
		
		p.openInventory(STGUI);
	}
	
	@EventHandler
	public void onInventoryClose(InventoryCloseEvent e) {
		if(e.getInventory().equals(this.STGUI)) {
			HandlerList.unregisterAll(this);
		}
	}
	
	@EventHandler
	public void onInventoryClick(InventoryClickEvent e) {
		if(e.getInventory().equals(STGUI)) {
			e.setCancelled(true);
			if(e.getCurrentItem() != null && e.getCurrentItem().hasItemMeta() && e.getCurrentItem().getItemMeta().hasDisplayName()) {
				if(e.getCurrentItem().getItemMeta().getDisplayName().equals(ChatColor.translateAlternateColorCodes('&', "&b이전 페이지"))) {
					openGUI(PlayerPage - 1);
				} else if(e.getCurrentItem().getItemMeta().getDisplayName().equals(ChatColor.translateAlternateColorCodes('&', "&b다음 페이지"))) {
					openGUI(PlayerPage + 1);
				}
			}
		}
	}
	
}