package daybreak.abilitywar.config;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
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

import daybreak.abilitywar.config.AbilityWarSettings.DeathSettings;
import daybreak.abilitywar.config.enums.ConfigNodes;
import daybreak.abilitywar.config.enums.OnDeath;
import daybreak.abilitywar.utils.Messager;
import daybreak.abilitywar.utils.database.FileManager;
import daybreak.abilitywar.utils.library.item.ItemLib;
import daybreak.abilitywar.utils.library.item.MaterialLib;
import daybreak.abilitywar.utils.library.item.ItemLib.ItemColor;

/**
 * 콘피그 설정 마법사
 * @author DayBreak 새벽
 */
public class SettingWizard implements Listener {
	
	private final Player p;
	
	public SettingWizard(Player p, Plugin Plugin) {
		this.p = p;
		Bukkit.getPluginManager().registerEvents(this, Plugin);
	}
	
	private Inventory KitGUI;
	private Inventory InvGUI;
	private Inventory GameGUI;
	private Inventory SpawnGUI;
	private Inventory DeathGUI;
	
	public void openKitGUI() {
		KitGUI = Bukkit.createInventory(p, 45, ChatColor.translateAlternateColorCodes('&', "&2&l게임 킷 설정"));
		
		ItemStack Confirm = ItemLib.WOOL.getItemStack(ItemColor.LIME);
		ItemMeta ConfirmMeta = Confirm.getItemMeta();
		ConfirmMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&a확인"));
		Confirm.setItemMeta(ConfirmMeta);
		
		ItemStack Reset = ItemLib.WOOL.getItemStack(ItemColor.RED);
		ItemMeta ResetMeta = Reset.getItemMeta();
		ResetMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&c초기화"));
		Reset.setItemMeta(ResetMeta);
		
		ItemStack Deco = MaterialLib.WHITE_STAINED_GLASS_PANE.getItem();
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
		
		for(ItemStack is : AbilityWarSettings.getDefaultKit()) {
			KitGUI.addItem(is);
		}
		
		p.openInventory(KitGUI);
	}

	public void openInvincibilityGUI() {
		InvGUI = Bukkit.createInventory(p, 27, ChatColor.translateAlternateColorCodes('&', "&2&l초반 무적 설정"));

		ItemStack Deco = MaterialLib.WHITE_STAINED_GLASS_PANE.getItem();
		ItemMeta DecoMeta = Deco.getItemMeta();
		DecoMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&f"));
		Deco.setItemMeta(DecoMeta);
		
		for(Integer i = 0; i < 27; i++) {
			if(i.equals(11)) {
				boolean InvincibilityEnable = AbilityWarSettings.getInvincibilityEnable();
				ItemColor color = InvincibilityEnable ? ItemColor.LIME : ItemColor.RED;
				ItemStack Inv = ItemLib.WOOL.getItemStack(color);
				ItemMeta InvMeta = Inv.getItemMeta();
				InvMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&b초반 무적"));
				InvMeta.setLore(Messager.asList(
						ChatColor.translateAlternateColorCodes('&', "&7상태 : " + (InvincibilityEnable ? "&a활성화" : "&c비활성화"))
						));
				Inv.setItemMeta(InvMeta);
				
				InvGUI.setItem(i, Inv);
			} else if(i.equals(15)) {
				ItemStack Inv = MaterialLib.CLOCK.getItem();
				ItemMeta InvMeta = Inv.getItemMeta();
				InvMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&b초반 무적 시간"));
				InvMeta.setLore(Messager.asList(
						ChatColor.translateAlternateColorCodes('&', "&7지속 시간 : &a" + AbilityWarSettings.getInvincibilityDuration() + "분"),
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
	
	public void openGameGUI() {
		GameGUI = Bukkit.createInventory(p, 45, ChatColor.translateAlternateColorCodes('&', "&2&l게임 진행 설정"));

		ItemStack Deco = MaterialLib.WHITE_STAINED_GLASS_PANE.getItem();
		ItemMeta DecoMeta = Deco.getItemMeta();
		DecoMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&f"));
		Deco.setItemMeta(DecoMeta);
		
		for(Integer i = 0; i < 45; i++) {
			if(i.equals(12)) {
				ItemStack Food = new ItemStack(Material.COOKED_BEEF, 1);
				ItemMeta FoodMeta = Food.getItemMeta();
				FoodMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&b배고픔 무제한"));
				FoodMeta.setLore(Messager.asList(
						ChatColor.translateAlternateColorCodes('&', "&7상태 : " + (AbilityWarSettings.getNoHunger() ? "&a활성화" : "&c비활성화"))
						));
				Food.setItemMeta(FoodMeta);
				
				GameGUI.setItem(i, Food);
			} else if(i.equals(14)) {
				ItemStack Lev = MaterialLib.EXPERIENCE_BOTTLE.getItem();
				ItemMeta LevMeta = Lev.getItemMeta();
				LevMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&b초반 지급 레벨"));
				LevMeta.setLore(Messager.asList(
						ChatColor.translateAlternateColorCodes('&', "&7초반 지급 레벨 : &a" + AbilityWarSettings.getStartLevel() + "레벨"),
						" ",
						ChatColor.translateAlternateColorCodes('&', "&c우클릭         &6» &e+ 1레벨"),
						ChatColor.translateAlternateColorCodes('&', "&cSHIFT + 우클릭 &6» &e+ 5레벨"),
						ChatColor.translateAlternateColorCodes('&', "&c좌클릭         &6» &e- 1레벨"),
						ChatColor.translateAlternateColorCodes('&', "&cSHIFT + 좌클릭 &6» &e- 5레벨"),
						ChatColor.translateAlternateColorCodes('&', "&c휠클릭         &6» &e+ 10000레벨"),
						ChatColor.translateAlternateColorCodes('&', "&cQ              &6» &e- 10000레벨")
						));
				Lev.setItemMeta(LevMeta);
				
				GameGUI.setItem(i, Lev);
			} else if(i.equals(20)) {
				ItemStack Dur = new ItemStack(Material.IRON_CHESTPLATE);
				ItemMeta DurMeta = Dur.getItemMeta();
				DurMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&b내구도 무한"));
				DurMeta.setLore(Messager.asList(
						ChatColor.translateAlternateColorCodes('&', "&7상태 : " + (AbilityWarSettings.getInfiniteDurability() ? "&a활성화" : "&c비활성화"))
						));
				Dur.setItemMeta(DurMeta);
				
				GameGUI.setItem(i, Dur);
			} else if(i.equals(22)) {
				ItemStack Firewall = new ItemStack(Material.BARRIER);
				ItemMeta FirewallMeta = Firewall.getItemMeta();
				FirewallMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&b방화벽"));
				FirewallMeta.setLore(Messager.asList(
						ChatColor.translateAlternateColorCodes('&', "&a활성화&f하면 게임이 시작되고 난 후 참여자 또는 관전자가 아닌 유저는 접속할 수 없습니다."),
						ChatColor.translateAlternateColorCodes('&', "&c관리자 권한&f을 가지고 있을 경우 이를 무시하고 접속할 수 있습니다."),
						"",
						ChatColor.translateAlternateColorCodes('&', "&7상태 : " + (AbilityWarSettings.getFirewall() ? "&a활성화" : "&c비활성화"))
						));
				Firewall.setItemMeta(FirewallMeta);
				
				GameGUI.setItem(i, Firewall);
			} else if(i.equals(24)) {
				ItemStack ClearWeather = MaterialLib.SNOWBALL.getItem();
				ItemMeta ClearWeatherMeta = ClearWeather.getItemMeta();
				ClearWeatherMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&b맑은 날씨 고정"));
				ClearWeatherMeta.setLore(Messager.asList(
						ChatColor.translateAlternateColorCodes('&', "&7상태 : " + (AbilityWarSettings.getClearWeather() ? "&a활성화" : "&c비활성화"))
						));
				ClearWeather.setItemMeta(ClearWeatherMeta);
				
				GameGUI.setItem(i, ClearWeather);
			} else if(i.equals(30)) {
				ItemStack VisualEffect = new ItemStack(Material.BLAZE_POWDER);
				ItemMeta VisualEffectMeta = VisualEffect.getItemMeta();
				VisualEffectMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&b시각 효과"));
				VisualEffectMeta.setLore(Messager.asList(
						ChatColor.translateAlternateColorCodes('&', "&a활성화&f하면 일부 능력을 사용할 때 파티클 효과가 보여집니다."),
						"",
						ChatColor.translateAlternateColorCodes('&', "&7상태 : " + (AbilityWarSettings.getVisualEffect() ? "&a활성화" : "&c비활성화"))
						));
				VisualEffect.setItemMeta(VisualEffectMeta);
				
				GameGUI.setItem(i, VisualEffect);
			} else if(i.equals(32)) {
				ItemStack AbilityDraw = new ItemStack(Material.DISPENSER);
				ItemMeta AbilityDrawMeta = AbilityDraw.getItemMeta();
				AbilityDrawMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&b능력 추첨"));
				AbilityDrawMeta.setLore(Messager.asList(
						ChatColor.translateAlternateColorCodes('&', "&a활성화&f하면 게임을 시작할 때 능력을 추첨합니다."),
						"",
						ChatColor.translateAlternateColorCodes('&', "&7상태 : " + (AbilityWarSettings.getDrawAbility() ? "&a활성화" : "&c비활성화"))
						));
				AbilityDraw.setItemMeta(AbilityDrawMeta);
				
				GameGUI.setItem(i, AbilityDraw);
			} else {
				GameGUI.setItem(i, Deco);
			}
		}
		
		p.openInventory(GameGUI);
	}

	public void openSpawnGUI() {
		SpawnGUI = Bukkit.createInventory(p, 27, ChatColor.translateAlternateColorCodes('&', "&2&l스폰 설정"));

		ItemStack Deco = MaterialLib.WHITE_STAINED_GLASS_PANE.getItem();
		ItemMeta DecoMeta = Deco.getItemMeta();
		DecoMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&f"));
		Deco.setItemMeta(DecoMeta);
		
		for(Integer i = 0; i < 27; i++) {
			if(i.equals(11)) {
				ItemStack Spawn = new ItemStack(Material.COOKED_BEEF, 1);
				ItemMeta SpawnMeta = Spawn.getItemMeta();
				SpawnMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&b스폰 이동"));
				SpawnMeta.setLore(Messager.asList(
						ChatColor.translateAlternateColorCodes('&', "&f게임이 시작되면 &b스폰&f으로 이동합니다."),
						ChatColor.translateAlternateColorCodes('&', "&7상태 : " + (AbilityWarSettings.getSpawnEnable() ? "&a활성화" : "&c비활성화"))
						));
				Spawn.setItemMeta(SpawnMeta);
				
				SpawnGUI.setItem(i, Spawn);
			} else if(i.equals(15)) {
				ItemStack Spawn = new ItemStack(Material.COMPASS, 1);
				ItemMeta SpawnMeta = Spawn.getItemMeta();
				SpawnMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&b스폰 설정"));
				
				Location SpawnLocation = AbilityWarSettings.getSpawnLocation();
				Double X = SpawnLocation.getX();
				Double Y = SpawnLocation.getY();
				Double Z = SpawnLocation.getZ();
				
				SpawnMeta.setLore(Messager.asList(
						ChatColor.translateAlternateColorCodes('&', "&f당신이 현재 서 있는 &a위치&f를 &b스폰&f으로 설정합니다."),
						ChatColor.translateAlternateColorCodes('&', "&6» &f스폰 위치를 변경하려면 클릭하세요."),
						"",
						ChatColor.translateAlternateColorCodes('&', "&3현재 스폰 위치"),
						ChatColor.translateAlternateColorCodes('&', "&b월드 &7: &f" + SpawnLocation.getWorld().getName()),
						ChatColor.translateAlternateColorCodes('&', "&bX &7: &f" + X),
						ChatColor.translateAlternateColorCodes('&', "&bY &7: &f" + Y),
						ChatColor.translateAlternateColorCodes('&', "&bZ &7: &f" + Z),
						ChatColor.translateAlternateColorCodes('&', "&6» &f이 위치로 이동하려면 SHIFT + 좌클릭하세요."))
						);
				Spawn.setItemMeta(SpawnMeta);
				
				SpawnGUI.setItem(i, Spawn);
			} else {
				SpawnGUI.setItem(i, Deco);
			}
		}
		
		p.openInventory(SpawnGUI);
	}
	
	public void openDeathGUI() {
		DeathGUI = Bukkit.createInventory(p, 27, ChatColor.translateAlternateColorCodes('&', "&2&l플레이어 사망 설정"));

		ItemStack Deco = MaterialLib.WHITE_STAINED_GLASS_PANE.getItem();
		ItemMeta DecoMeta = Deco.getItemMeta();
		DecoMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&f"));
		Deco.setItemMeta(DecoMeta);
		
		for(Integer i = 0; i < 27; i++) {
			if(i.equals(10)) {
				ItemStack Eliminate = new ItemStack(Material.DIAMOND_SWORD);
				ItemMeta EliminateMeta = Eliminate.getItemMeta();
				EliminateMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&b작업"));
				List<String> lore = Messager.asList(
						ChatColor.translateAlternateColorCodes('&', "&f게임 진행 중 플레이어가 사망했을 때 어떤 작업을 수행할지 설정합니다."),
						"");
				OnDeath operation = DeathSettings.getOperation();
				for(OnDeath od : OnDeath.values()) {
					lore.add(ChatColor.translateAlternateColorCodes('&', (operation.equals(od) ? "&a" : "&7") + od.name() + "&f: " + od.getDescription()));
				}
				EliminateMeta.setLore(lore);
				Eliminate.setItemMeta(EliminateMeta);
				
				DeathGUI.setItem(i, Eliminate);
			} else if(i.equals(12)) {
				ItemStack AbilityReveal = MaterialLib.ENDER_EYE.getItem();
				ItemMeta AbilityRevealMeta = AbilityReveal.getItemMeta();
				AbilityRevealMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&b능력 공개"));
				AbilityRevealMeta.setLore(Messager.asList(
						ChatColor.translateAlternateColorCodes('&', "&a활성화&f하면 게임이 시작되고 난 후 사망할 경우 플레이어의 능력을 공개합니다."),
						"",
						ChatColor.translateAlternateColorCodes('&', "&7상태 : " + (DeathSettings.getAbilityReveal() ? "&a활성화" : "&c비활성화"))
						));
				AbilityReveal.setItemMeta(AbilityRevealMeta);
				
				DeathGUI.setItem(i, AbilityReveal);
			} else if(i.equals(14)) {
				ItemStack AbilityRemoval = MaterialLib.BEDROCK.getItem();
				ItemMeta AbilityRemovalMeta = AbilityRemoval.getItemMeta();
				AbilityRemovalMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&b능력 삭제"));
				AbilityRemovalMeta.setLore(Messager.asList(
						ChatColor.translateAlternateColorCodes('&', "&a활성화&f하면 게임이 시작되고 난 후 사망할 경우 플레이어의 능력이 삭제됩니다."),
						"",
						ChatColor.translateAlternateColorCodes('&', "&7상태 : " + (DeathSettings.getAbilityRemoval() ? "&a활성화" : "&c비활성화"))
						));
				AbilityRemoval.setItemMeta(AbilityRemovalMeta);
				
				DeathGUI.setItem(i, AbilityRemoval);
			} else if(i.equals(16)) {
				ItemStack ItemDrop = new ItemStack(Material.CHEST);
				ItemMeta ItemDropMeta = ItemDrop.getItemMeta();
				ItemDropMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&b아이템 드롭"));
				ItemDropMeta.setLore(Messager.asList(
						ChatColor.translateAlternateColorCodes('&', "&c비활성화&f하면 게임이 시작되고 난 후 사망하였을 때 아이템을 드롭하지 않습니다."),
						"",
						ChatColor.translateAlternateColorCodes('&', "&7상태 : " + (DeathSettings.getItemDrop() ? "&a활성화" : "&c비활성화"))
						));
				ItemDrop.setItemMeta(ItemDropMeta);
				
				DeathGUI.setItem(i, ItemDrop);
			} else {
				DeathGUI.setItem(i, Deco);
			}
		}
		
		p.openInventory(DeathGUI);
	}
	
	@EventHandler
	private void onInventoryClose(InventoryCloseEvent e) {
		if(e.getInventory().equals(this.SpawnGUI) || e.getInventory().equals(this.KitGUI) || e.getInventory().equals(this.InvGUI)
		|| e.getInventory().equals(this.GameGUI) || e.getInventory().equals(this.DeathGUI)) {
			HandlerList.unregisterAll(this);
			AbilityWarSettings.Save();
		}
	}
	
	@EventHandler
	private void onInventoryClick(InventoryClickEvent e) {
		if(e.getInventory().equals(this.KitGUI)) {
			Player p = (Player) e.getWhoClicked();
			
			if(e.getCurrentItem() != null) {
				if(e.getSlot() >= 36) e.setCancelled(true);
				if(e.getCurrentItem().hasItemMeta() && e.getCurrentItem().getItemMeta().hasDisplayName()) {
					if(e.getCurrentItem().getItemMeta().getDisplayName().equals(ChatColor.translateAlternateColorCodes('&', "&a확인"))) {
						AbilityWarSettings.setNewProperty(ConfigNodes.Game_Kit, getItemUntil(KitGUI, 35));
						p.closeInventory();
						p.sendMessage(ChatColor.translateAlternateColorCodes('&', "&2게임 킷 &a설정을 마쳤습니다."));
					} else if(e.getCurrentItem().getItemMeta().getDisplayName().equals(ChatColor.translateAlternateColorCodes('&', "&c초기화"))) {
						AbilityWarSettings.setNewProperty(ConfigNodes.Game_Kit, FileManager.getItemStackList());
						p.closeInventory();
						p.sendMessage(ChatColor.translateAlternateColorCodes('&', "&2게임 킷 &a설정이 초기화되었습니다."));
					}
				}
			}
		} else if(e.getInventory().equals(this.InvGUI)) {
			e.setCancelled(true);
			
			if(e.getCurrentItem() != null) {
				if(e.getCurrentItem().hasItemMeta() && e.getCurrentItem().getItemMeta().hasDisplayName()) {
					if(e.getCurrentItem().getItemMeta().getDisplayName().equals(ChatColor.translateAlternateColorCodes('&', "&b초반 무적"))) {
						AbilityWarSettings.setNewProperty(ConfigNodes.Game_Invincibility_Enable, !AbilityWarSettings.getInvincibilityEnable());
						openInvincibilityGUI();
					} else if(e.getCurrentItem().getItemMeta().getDisplayName().equals(ChatColor.translateAlternateColorCodes('&', "&b초반 무적 시간"))) {
						Integer InvincibilityDuration = AbilityWarSettings.getInvincibilityDuration();
						if(e.getClick().equals(ClickType.RIGHT)) {
							AbilityWarSettings.setNewProperty(ConfigNodes.Game_Invincibility_Duration, InvincibilityDuration + 1);
							openInvincibilityGUI();
						} else if(e.getClick().equals(ClickType.SHIFT_RIGHT)) {
							AbilityWarSettings.setNewProperty(ConfigNodes.Game_Invincibility_Duration, InvincibilityDuration + 5);
							openInvincibilityGUI();
						} else if(e.getClick().equals(ClickType.LEFT)) {
							if(InvincibilityDuration >= 2) {
								AbilityWarSettings.setNewProperty(ConfigNodes.Game_Invincibility_Duration, InvincibilityDuration - 1);
								openInvincibilityGUI();
							} else {
								AbilityWarSettings.setNewProperty(ConfigNodes.Game_Invincibility_Duration, 1);
								openInvincibilityGUI();
							}
						} else if(e.getClick().equals(ClickType.SHIFT_LEFT)) {
							if(InvincibilityDuration >= 6) {
								AbilityWarSettings.setNewProperty(ConfigNodes.Game_Invincibility_Duration, InvincibilityDuration - 5);
								openInvincibilityGUI();
							} else {
								AbilityWarSettings.setNewProperty(ConfigNodes.Game_Invincibility_Duration, 1);
								openInvincibilityGUI();
							}
						}
					}
				}
			}
		} else if(e.getInventory().equals(this.GameGUI)) {
			e.setCancelled(true);
			
			if(e.getCurrentItem() != null) {
				if(e.getCurrentItem().hasItemMeta() && e.getCurrentItem().getItemMeta().hasDisplayName()) {
					if(e.getCurrentItem().getItemMeta().getDisplayName().equals(ChatColor.translateAlternateColorCodes('&', "&b배고픔 무제한"))) {
						AbilityWarSettings.setNewProperty(ConfigNodes.Game_NoHunger, !AbilityWarSettings.getNoHunger());
						openGameGUI();
					} else if(e.getCurrentItem().getItemMeta().getDisplayName().equals(ChatColor.translateAlternateColorCodes('&', "&b초반 지급 레벨"))) {
						Integer StartLevel = AbilityWarSettings.getStartLevel();
						
						if(e.getClick().equals(ClickType.RIGHT)) {
							AbilityWarSettings.setNewProperty(ConfigNodes.Game_StartLevel, StartLevel + 1);
							openGameGUI();
						} else if(e.getClick().equals(ClickType.SHIFT_RIGHT)) {
							AbilityWarSettings.setNewProperty(ConfigNodes.Game_StartLevel, StartLevel + 5);
							openGameGUI();
						} else if(e.getClick().equals(ClickType.LEFT)) {
							if(StartLevel >= 1) {
								AbilityWarSettings.setNewProperty(ConfigNodes.Game_StartLevel, StartLevel - 1);
								openGameGUI();
							} else {
								AbilityWarSettings.setNewProperty(ConfigNodes.Game_StartLevel, 0);
								openGameGUI();
							}
						} else if(e.getClick().equals(ClickType.SHIFT_LEFT)) {
							if(StartLevel >= 5) {
								AbilityWarSettings.setNewProperty(ConfigNodes.Game_StartLevel, StartLevel - 5);
								openGameGUI();
							} else {
								AbilityWarSettings.setNewProperty(ConfigNodes.Game_StartLevel, 0);
								openGameGUI();
							}
						} else if(e.getClick().equals(ClickType.MIDDLE)) {
							AbilityWarSettings.setNewProperty(ConfigNodes.Game_StartLevel, StartLevel + 10000);
							openGameGUI();
						} else if(e.getClick().equals(ClickType.DROP)) {
							if(StartLevel >= 10000) {
								AbilityWarSettings.setNewProperty(ConfigNodes.Game_StartLevel, StartLevel - 10000);
								openGameGUI();
							} else {
								AbilityWarSettings.setNewProperty(ConfigNodes.Game_StartLevel, 0);
								openGameGUI();
							}
						}
					} else if(e.getCurrentItem().getItemMeta().getDisplayName().equals(ChatColor.translateAlternateColorCodes('&', "&b내구도 무한"))) {
						AbilityWarSettings.setNewProperty(ConfigNodes.Game_InfiniteDurability, !AbilityWarSettings.getInfiniteDurability());
						openGameGUI();
					} else if(e.getCurrentItem().getItemMeta().getDisplayName().equals(ChatColor.translateAlternateColorCodes('&', "&b방화벽"))) {
						AbilityWarSettings.setNewProperty(ConfigNodes.Game_Firewall, !AbilityWarSettings.getFirewall());
						openGameGUI();
					} else if(e.getCurrentItem().getItemMeta().getDisplayName().equals(ChatColor.translateAlternateColorCodes('&', "&b맑은 날씨 고정"))) {
						AbilityWarSettings.setNewProperty(ConfigNodes.Game_ClearWeather, !AbilityWarSettings.getClearWeather());
						openGameGUI();
					} else if(e.getCurrentItem().getItemMeta().getDisplayName().equals(ChatColor.translateAlternateColorCodes('&', "&b시각 효과"))) {
						AbilityWarSettings.setNewProperty(ConfigNodes.Game_VisualEffect, !AbilityWarSettings.getVisualEffect());
						openGameGUI();
					} else if(e.getCurrentItem().getItemMeta().getDisplayName().equals(ChatColor.translateAlternateColorCodes('&', "&b능력 추첨"))) {
						AbilityWarSettings.setNewProperty(ConfigNodes.Game_DrawAbility, !AbilityWarSettings.getDrawAbility());
						openGameGUI();
					}
				}
			}
		} else if(e.getInventory().equals(this.SpawnGUI)) {
			e.setCancelled(true);
			
			Player p = (Player) e.getWhoClicked();
			
			if(e.getCurrentItem() != null) {
				if(e.getCurrentItem().hasItemMeta() && e.getCurrentItem().getItemMeta().hasDisplayName()) {
					if(e.getCurrentItem().getItemMeta().getDisplayName().equals(ChatColor.translateAlternateColorCodes('&', "&b스폰 이동"))) {
						AbilityWarSettings.setNewProperty(ConfigNodes.Game_Spawn_Enable, !AbilityWarSettings.getSpawnEnable());
						openSpawnGUI();
					} else if(e.getCurrentItem().getItemMeta().getDisplayName().equals(ChatColor.translateAlternateColorCodes('&', "&b스폰 설정"))) {
						if(!e.getClick().equals(ClickType.SHIFT_LEFT)) {
							AbilityWarSettings.setNewProperty(ConfigNodes.Game_Spawn_Location, p.getLocation());
							p.closeInventory();
							p.sendMessage(ChatColor.translateAlternateColorCodes('&', "&a게임 스폰이 변경되었습니다."));
						} else {
							p.teleport(AbilityWarSettings.getSpawnLocation());
							p.closeInventory();
							p.sendMessage(ChatColor.translateAlternateColorCodes('&', "&a스폰 &f위치로 이동되었습니다."));
						}
					}
				}
			}
		} else if(e.getInventory().equals(this.DeathGUI)) {
			e.setCancelled(true);
			
			if(e.getCurrentItem() != null) {
				if(e.getCurrentItem().getItemMeta().getDisplayName().equals(ChatColor.translateAlternateColorCodes('&', "&b작업"))) {
					DeathSettings.nextOperation();
					openDeathGUI();
				} else if(e.getCurrentItem().getItemMeta().getDisplayName().equals(ChatColor.translateAlternateColorCodes('&', "&b능력 공개"))) {
					AbilityWarSettings.setNewProperty(ConfigNodes.Game_Death_AbilityReveal, !DeathSettings.getAbilityReveal());
					openDeathGUI();
				} else if(e.getCurrentItem().getItemMeta().getDisplayName().equals(ChatColor.translateAlternateColorCodes('&', "&b능력 삭제"))) {
					AbilityWarSettings.setNewProperty(ConfigNodes.Game_Death_AbilityRemoval, !DeathSettings.getAbilityRemoval());
					openDeathGUI();
				} else if(e.getCurrentItem().getItemMeta().getDisplayName().equals(ChatColor.translateAlternateColorCodes('&', "&b아이템 드롭"))) {
					AbilityWarSettings.setNewProperty(ConfigNodes.Game_Death_ItemDrop, !DeathSettings.getItemDrop());
					openDeathGUI();
				}
			}
		}
	}
	
	public ArrayList<ItemStack> getItemUntil(Inventory inv, int Count) {
		ArrayList<ItemStack> List = new ArrayList<ItemStack>();
		
		for(int i = 0; i <= Count; i++) {
			if(inv.getItem(i) != null && !inv.getItem(i).getType().equals(Material.AIR)) {
				List.add(inv.getItem(i));
			}
		}
		
		return List;
	}
	
}
