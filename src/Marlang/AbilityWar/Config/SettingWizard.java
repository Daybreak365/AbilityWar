package Marlang.AbilityWar.Config;

import java.util.ArrayList;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import Marlang.AbilityWar.AbilityWar;
import Marlang.AbilityWar.Config.Nodes.ConfigNodes;
import Marlang.AbilityWar.GameManager.Module.Module;
import Marlang.AbilityWar.Utils.FileManager;
import Marlang.AbilityWar.Utils.Messager;

/**
 * 콘피그 설정 마법사
 * @author _Marlang 말랑
 */
public class SettingWizard extends Module implements Listener {
	
	public SettingWizard() {
		RegisterListener(this);
	}
	
	static Inventory KitGUI;
	static Inventory InvGUI;
	static Inventory StartLevelGUI;
	static Inventory InfFoodGUI;
	static Inventory SpawnGUI;
	
	public static void openKitGUI(Player p) {
		KitGUI = Bukkit.createInventory(p, 45, ChatColor.translateAlternateColorCodes('&', "&2&l게임 킷 설정"));
		
		ItemStack Confirm = new ItemStack(Material.WOOL, 1, (short) 5);
		ItemMeta ConfirmMeta = Confirm.getItemMeta();
		ConfirmMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&a확인"));
		Confirm.setItemMeta(ConfirmMeta);
		
		ItemStack Reset = new ItemStack(Material.WOOL, 1, (short) 14);
		ItemMeta ResetMeta = Reset.getItemMeta();
		ResetMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&c초기화"));
		Reset.setItemMeta(ResetMeta);
		
		ItemStack Deco = new ItemStack(Material.STAINED_GLASS_PANE);
		ItemMeta DecoMeta = Deco.getItemMeta();
		DecoMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&f"));
		Deco.setItemMeta(DecoMeta);
		
		KitGUI.setItem(36, Deco);
		KitGUI.setItem(37, Deco);
		KitGUI.setItem(38, Deco);
		KitGUI.setItem(39, Reset);
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

	public static void openInvincibilityGUI(Player p) {
		InvGUI = Bukkit.createInventory(p, 27, ChatColor.translateAlternateColorCodes('&', "&2&l초반 무적 설정"));
		
		ItemStack Deco = new ItemStack(Material.STAINED_GLASS_PANE);
		ItemMeta DecoMeta = Deco.getItemMeta();
		DecoMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&f"));
		Deco.setItemMeta(DecoMeta);
		
		for(Integer i = 0; i < 27; i++) {
			if(i.equals(11)) {
				ItemStack Inv = new ItemStack(Material.WOOL, 1, (short) (AbilityWar.getSetting().getInvincibilityEnable() ? 5 : 14));
				ItemMeta InvMeta = Inv.getItemMeta();
				InvMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&b초반 무적"));
				InvMeta.setLore(Messager.getStringList(
						ChatColor.translateAlternateColorCodes('&', "&7상태 : " + (AbilityWar.getSetting().getInvincibilityEnable() ? "&a활성화" : "&c비활성화"))
						));
				Inv.setItemMeta(InvMeta);
				
				InvGUI.setItem(i, Inv);
			} else if(i.equals(15)) {
				ItemStack Inv = new ItemStack(Material.WATCH, 1);
				ItemMeta InvMeta = Inv.getItemMeta();
				InvMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&b초반 무적 시간"));
				InvMeta.setLore(Messager.getStringList(
						ChatColor.translateAlternateColorCodes('&', "&7지속 시간 : &a" + AbilityWar.getSetting().getInvincibilityDuration() + "분"),
						" ",
						ChatColor.translateAlternateColorCodes('&', "&c우클릭         &6» &e+ 1분"),
						ChatColor.translateAlternateColorCodes('&', "&cSHIFT + 우클릭 &6» &e+ 5분"),
						ChatColor.translateAlternateColorCodes('&', "&c좌클릭         &6» &e- 1분"),
						ChatColor.translateAlternateColorCodes('&', "&cSHIFT + 좌클릭 &6» &e- 5분")
						));
				Inv.setItemMeta(InvMeta);
				
				InvGUI.setItem(i, Inv);
			} else {
				InvGUI.setItem(i, Deco);
			}
		}
		
		p.openInventory(InvGUI);
	}

	public static void openStartLevelGUI(Player p) {
		StartLevelGUI = Bukkit.createInventory(p, 27, ChatColor.translateAlternateColorCodes('&', "&2&l초반 지급 레벨 설정"));
		
		ItemStack Deco = new ItemStack(Material.STAINED_GLASS_PANE);
		ItemMeta DecoMeta = Deco.getItemMeta();
		DecoMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&f"));
		Deco.setItemMeta(DecoMeta);
		
		for(Integer i = 0; i < 27; i++) {
			if(i.equals(13)) {
				ItemStack Lev = new ItemStack(Material.EXP_BOTTLE, 1);
				ItemMeta LevMeta = Lev.getItemMeta();
				LevMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&b초반 지급 레벨"));
				LevMeta.setLore(Messager.getStringList(
						ChatColor.translateAlternateColorCodes('&', "&7초반 지급 레벨 : &a" + AbilityWar.getSetting().getStartLevel() + "레벨"),
						" ",
						ChatColor.translateAlternateColorCodes('&', "&c우클릭         &6» &e+ 1레벨"),
						ChatColor.translateAlternateColorCodes('&', "&cSHIFT + 우클릭 &6» &e+ 5레벨"),
						ChatColor.translateAlternateColorCodes('&', "&c좌클릭         &6» &e- 1레벨"),
						ChatColor.translateAlternateColorCodes('&', "&cSHIFT + 좌클릭 &6» &e- 5레벨")
						));
				Lev.setItemMeta(LevMeta);
				
				StartLevelGUI.setItem(i, Lev);
			} else {
				StartLevelGUI.setItem(i, Deco);
			}
		}
		
		p.openInventory(StartLevelGUI);
	}

	public static void openInfiniteFoodGUI(Player p) {
		InfFoodGUI = Bukkit.createInventory(p, 27, ChatColor.translateAlternateColorCodes('&', "&2&l배고픔 무제한 설정"));
		
		ItemStack Deco = new ItemStack(Material.STAINED_GLASS_PANE);
		ItemMeta DecoMeta = Deco.getItemMeta();
		DecoMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&f"));
		Deco.setItemMeta(DecoMeta);
		
		for(Integer i = 0; i < 27; i++) {
			if(i.equals(13)) {
				ItemStack Food = new ItemStack(Material.COOKED_BEEF, 1);
				ItemMeta FoodMeta = Food.getItemMeta();
				FoodMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&b배고픔 무제한"));
				FoodMeta.setLore(Messager.getStringList(
						ChatColor.translateAlternateColorCodes('&', "&7상태 : " + (AbilityWar.getSetting().getNoHunger() ? "&a활성화" : "&c비활성화"))
						));
				Food.setItemMeta(FoodMeta);
				
				InfFoodGUI.setItem(i, Food);
			} else {
				InfFoodGUI.setItem(i, Deco);
			}
		}
		
		p.openInventory(InfFoodGUI);
	}

	public static void openSpawnGUI(Player p) {
		SpawnGUI = Bukkit.createInventory(p, 27, ChatColor.translateAlternateColorCodes('&', "&2&l스폰 설정"));
		
		ItemStack Deco = new ItemStack(Material.STAINED_GLASS_PANE);
		ItemMeta DecoMeta = Deco.getItemMeta();
		DecoMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&f"));
		Deco.setItemMeta(DecoMeta);
		
		for(Integer i = 0; i < 27; i++) {
			if(i.equals(11)) {
				ItemStack Spawn = new ItemStack(Material.COOKED_BEEF, 1);
				ItemMeta SpawnMeta = Spawn.getItemMeta();
				SpawnMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&b스폰 이동"));
				SpawnMeta.setLore(Messager.getStringList(
						ChatColor.translateAlternateColorCodes('&', "&f게임이 시작되면 &b스폰&f으로 이동합니다."),
						ChatColor.translateAlternateColorCodes('&', "&7상태 : " + (AbilityWar.getSetting().getSpawnEnable() ? "&a활성화" : "&c비활성화"))
						));
				Spawn.setItemMeta(SpawnMeta);
				
				SpawnGUI.setItem(i, Spawn);
			} else if(i.equals(15)) {
				ItemStack Spawn = new ItemStack(Material.COMPASS, 1);
				ItemMeta SpawnMeta = Spawn.getItemMeta();
				SpawnMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&b스폰 설정"));
				SpawnMeta.setLore(Messager.getStringList(
						ChatColor.translateAlternateColorCodes('&', "&f당신이 현재 서 있는 &a위치&f를 &b스폰&f으로 설정합니다."))
						);
				Spawn.setItemMeta(SpawnMeta);
				
				SpawnGUI.setItem(i, Spawn);
			} else {
				SpawnGUI.setItem(i, Deco);
			}
		}
		
		p.openInventory(SpawnGUI);
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
					} else if(e.getCurrentItem().getItemMeta().getDisplayName().equals(ChatColor.translateAlternateColorCodes('&', "&c초기화"))) {
						e.setCancelled(true);
						AbilityWar.getSetting().setNewProperty(ConfigNodes.Game_Kit, FileManager.getItemStackList());
						p.closeInventory();
						Messager.sendMessage(p, ChatColor.translateAlternateColorCodes('&', "&2게임 킷 &a설정이 초기화되었습니다."));
					} else if(e.getCurrentItem().getItemMeta().getDisplayName().equals(ChatColor.translateAlternateColorCodes('&', "&f"))) {
						e.setCancelled(true);
					}
				}
			}
		} else if(e.getInventory().equals(InvGUI)) {
			e.setCancelled(true);
			
			Player p = (Player) e.getWhoClicked();
			
			if(e.getCurrentItem() != null) {
				if(e.getCurrentItem().hasItemMeta() && e.getCurrentItem().getItemMeta().hasDisplayName()) {
					if(e.getCurrentItem().getItemMeta().getDisplayName().equals(ChatColor.translateAlternateColorCodes('&', "&b초반 무적"))) {
						AbilityWar.getSetting().setNewProperty(ConfigNodes.Game_Invincibility_Enable, !AbilityWar.getSetting().getInvincibilityEnable());
						openInvincibilityGUI(p);
					} else if(e.getCurrentItem().getItemMeta().getDisplayName().equals(ChatColor.translateAlternateColorCodes('&', "&b초반 무적 시간"))) {
						if(e.getClick().equals(ClickType.RIGHT)) {
							AbilityWar.getSetting().setNewProperty(ConfigNodes.Game_Invincibility_Duration, AbilityWar.getSetting().getInvincibilityDuration() + 1);
							openInvincibilityGUI(p);
						} else if(e.getClick().equals(ClickType.SHIFT_RIGHT)) {
							AbilityWar.getSetting().setNewProperty(ConfigNodes.Game_Invincibility_Duration, AbilityWar.getSetting().getInvincibilityDuration() + 5);
							openInvincibilityGUI(p);
						} else if(e.getClick().equals(ClickType.LEFT)) {
							if(AbilityWar.getSetting().getInvincibilityDuration() >= 2) {
								AbilityWar.getSetting().setNewProperty(ConfigNodes.Game_Invincibility_Duration, AbilityWar.getSetting().getInvincibilityDuration() - 1);
								openInvincibilityGUI(p);
							} else {
								AbilityWar.getSetting().setNewProperty(ConfigNodes.Game_Invincibility_Duration, 1);
								openInvincibilityGUI(p);
							}
						} else if(e.getClick().equals(ClickType.SHIFT_LEFT)) {
							if(AbilityWar.getSetting().getInvincibilityDuration() >= 6) {
								AbilityWar.getSetting().setNewProperty(ConfigNodes.Game_Invincibility_Duration, AbilityWar.getSetting().getInvincibilityDuration() - 5);
								openInvincibilityGUI(p);
							} else {
								AbilityWar.getSetting().setNewProperty(ConfigNodes.Game_Invincibility_Duration, 1);
								openInvincibilityGUI(p);
							}
						}
					}
				}
			}
		} else if(e.getInventory().equals(StartLevelGUI)) {
			e.setCancelled(true);
			
			Player p = (Player) e.getWhoClicked();
			
			if(e.getCurrentItem() != null) {
				if(e.getCurrentItem().hasItemMeta() && e.getCurrentItem().getItemMeta().hasDisplayName()) {
					if(e.getCurrentItem().getItemMeta().getDisplayName().equals(ChatColor.translateAlternateColorCodes('&', "&b초반 지급 레벨"))) {
						if(e.getClick().equals(ClickType.RIGHT)) {
							AbilityWar.getSetting().setNewProperty(ConfigNodes.Game_StartLevel, AbilityWar.getSetting().getStartLevel() + 1);
							openStartLevelGUI(p);
						} else if(e.getClick().equals(ClickType.SHIFT_RIGHT)) {
							AbilityWar.getSetting().setNewProperty(ConfigNodes.Game_StartLevel, AbilityWar.getSetting().getStartLevel() + 5);
							openStartLevelGUI(p);
						} else if(e.getClick().equals(ClickType.LEFT)) {
							if(AbilityWar.getSetting().getStartLevel() >= 1) {
								AbilityWar.getSetting().setNewProperty(ConfigNodes.Game_StartLevel, AbilityWar.getSetting().getStartLevel() - 1);
								openStartLevelGUI(p);
							} else {
								AbilityWar.getSetting().setNewProperty(ConfigNodes.Game_StartLevel, 0);
								openStartLevelGUI(p);
							}
						} else if(e.getClick().equals(ClickType.SHIFT_LEFT)) {
							if(AbilityWar.getSetting().getStartLevel() >= 5) {
								AbilityWar.getSetting().setNewProperty(ConfigNodes.Game_StartLevel, AbilityWar.getSetting().getStartLevel() - 5);
								openStartLevelGUI(p);
							} else {
								AbilityWar.getSetting().setNewProperty(ConfigNodes.Game_StartLevel, 0);
								openStartLevelGUI(p);
							}
						}
					}
				}
			}
		} else if(e.getInventory().equals(InfFoodGUI)) {
			e.setCancelled(true);
			
			Player p = (Player) e.getWhoClicked();
			
			if(e.getCurrentItem() != null) {
				if(e.getCurrentItem().hasItemMeta() && e.getCurrentItem().getItemMeta().hasDisplayName()) {
					if(e.getCurrentItem().getItemMeta().getDisplayName().equals(ChatColor.translateAlternateColorCodes('&', "&b배고픔 무제한"))) {
						AbilityWar.getSetting().setNewProperty(ConfigNodes.Game_NoHunger, !AbilityWar.getSetting().getNoHunger());
						openInfiniteFoodGUI(p);
					}
				}
			}
		} else if(e.getInventory().equals(SpawnGUI)) {
			e.setCancelled(true);
			
			Player p = (Player) e.getWhoClicked();
			
			if(e.getCurrentItem() != null) {
				if(e.getCurrentItem().hasItemMeta() && e.getCurrentItem().getItemMeta().hasDisplayName()) {
					if(e.getCurrentItem().getItemMeta().getDisplayName().equals(ChatColor.translateAlternateColorCodes('&', "&b스폰 이동"))) {
						AbilityWar.getSetting().setNewProperty(ConfigNodes.Game_Spawn_Enable, !AbilityWar.getSetting().getSpawnEnable());
						openSpawnGUI(p);
					} else if(e.getCurrentItem().getItemMeta().getDisplayName().equals(ChatColor.translateAlternateColorCodes('&', "&b스폰 설정"))) {
						AbilityWar.getSetting().setNewProperty(ConfigNodes.Game_Spawn_Location, p.getLocation());
						p.closeInventory();
						Messager.sendMessage(p, ChatColor.translateAlternateColorCodes('&', "&a게임 스폰이 변경되었습니다."));
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
