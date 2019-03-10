package Marlang.AbilityWar.GameManager.Script;

import java.lang.reflect.Constructor;
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
import org.bukkit.plugin.Plugin;

import Marlang.AbilityWar.GameManager.Script.Script.RequiredData;
import Marlang.AbilityWar.GameManager.Script.Script.ScriptRegisteration;
import Marlang.AbilityWar.GameManager.Script.Objects.AbstractScript;
import Marlang.AbilityWar.GameManager.Script.Objects.Setter.Setter;
import Marlang.AbilityWar.GameManager.Script.Objects.Setter.StringSetter;
import Marlang.AbilityWar.GameManager.Script.Objects.Setter.Special.LoopCountSetter;
import Marlang.AbilityWar.GameManager.Script.Objects.Setter.Special.LoopSetter;
import Marlang.AbilityWar.GameManager.Script.Objects.Setter.Special.TimeSetter;
import Marlang.AbilityWar.Utils.Messager;
import Marlang.AbilityWar.Utils.Library.Item.MaterialLib;

/**
 * Script Wizard
 * @author _Marlang 말랑
 */
public class ScriptWizard implements Listener {
	
	private final Player p;
	private final Class<? extends AbstractScript> scriptClass;
	private final String scriptName;
	
	public ScriptWizard(Player p, Plugin Plugin, Class<? extends AbstractScript> scriptClass, String scriptName) throws IllegalArgumentException, ScriptException {
		this.p = p;
		this.scriptClass = scriptClass;
		this.scriptName = scriptName;
		
		ScriptRegisteration reg = Script.getRegisteration(scriptClass);
		for(RequiredData<?> data : reg.getRequiredDatas()) {
			if(data.getSetterClass() == null) {
				Setter<?> setter = Setter.newInstance(data.getClazz(), data.getKey(), data.getDefault(), this);
				Setters.add(setter);
			} else {
				Constructor<? extends Setter<?>> setterConstructor;
				try {
					setterConstructor = data.getSetterClass().getConstructor(String.class, data.getClazz(), ScriptWizard.class);
					Setter<?> setter = setterConstructor.newInstance(data.getKey(), data.getDefault(), this);
					Setters.add(setter);
				} catch (Exception ex) {
					ex.printStackTrace();
					throw new IllegalArgumentException();
				}
			}
		}
		
		Bukkit.getPluginManager().registerEvents(this, Plugin);
	}
	
	private Integer PlayerPage = 1;
	
	private Inventory ScriptGUI;
	
	// Default Setters
	public TimeSetter Time = new TimeSetter(this);
	public LoopSetter Loop = new LoopSetter(this);
	public LoopCountSetter LoopCount = new LoopCountSetter(this);
	public StringSetter PreRunMessage = new StringSetter("실행 예고 메시지", "&e%Time%&f초 후에 &e%ScriptName% &f스크립트가 실행됩니다.", this);
	public StringSetter RunMessage = new StringSetter("실행 메시지", "&e%ScriptName% &f스크립트가 실행되었습니다.", this);
	// Default Setters
	
	private ArrayList<Setter<?>> Setters = new ArrayList<Setter<?>>();
	
	public void openScriptWizard(Integer page) {
		PlayerPage = page;
		ScriptGUI = Bukkit.createInventory(null, 45, ChatColor.translateAlternateColorCodes('&', "&c" + scriptName + " &0스크립트 편집"));
		
		Integer MaxPage = ((Setters.size() - 1) / 18) + 1;
		if (MaxPage < page) page = 1;
		if(page < 1) page = 1;
		
		int Count = 0;
		for (Setter<?> setter : Setters) {
			if (Count / 18 == page - 1) {
				ScriptGUI.setItem((Count % 36) + 18, setter.getItem());
			}
			Count++;
		}

		ScriptGUI.setItem(2, Time.getItem());
		ScriptGUI.setItem(3, Loop.getItem());
		ScriptGUI.setItem(4, LoopCount.getItem());
		ScriptGUI.setItem(5, PreRunMessage.getItem());
		ScriptGUI.setItem(6, RunMessage.getItem());
		
		ItemStack Deco = MaterialLib.WHITE_STAINED_GLASS_PANE.getItem();
		ItemMeta DecoMeta = Deco.getItemMeta();
		DecoMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&f"));
		Deco.setItemMeta(DecoMeta);
		
		for(Integer i = 0; i < 45; i++) {
			if(i.equals(0) || i.equals(1) || i.equals(7) || i.equals(8)
			|| (i >= 9 && i <= 17) || (i >= 36 && i <= 44)) {
				ScriptGUI.setItem(i, Deco);
			}
		}
		
		if(page > 1) {
			ItemStack previousPage = new ItemStack(Material.ARROW, 1);
			ItemMeta previousMeta = previousPage.getItemMeta();
			previousMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&b이전 페이지"));
			previousPage.setItemMeta(previousMeta);
			ScriptGUI.setItem(39, previousPage);
		}
		
		if(page != MaxPage) {
			ItemStack nextPage = new ItemStack(Material.ARROW, 1);
			ItemMeta nextMeta = nextPage.getItemMeta();
			nextMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&b다음 페이지"));
			nextPage.setItemMeta(nextMeta);
			ScriptGUI.setItem(41, nextPage);
		}

		ItemStack Page = new ItemStack(Material.PAPER, 1);
		ItemMeta PageMeta = Page.getItemMeta();
		PageMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&',
				"&6페이지 &e" + page + " &6/ &e" + MaxPage));
		Page.setItemMeta(PageMeta);
		ScriptGUI.setItem(40, Page);
		
		ItemStack Confirm = new ItemStack(Material.PAPER, 1);
		ItemMeta ConfirmMeta = Confirm.getItemMeta();
		ConfirmMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&a저장"));
		ConfirmMeta.setLore(Messager.getStringList(
				ChatColor.translateAlternateColorCodes('&', "&f저장 " + (canSave() ? "&a가능" : "&c불가능"))
				));
		Confirm.setItemMeta(ConfirmMeta);
		ScriptGUI.setItem(44, Confirm);
		
		p.openInventory(ScriptGUI);
	}
	
	private boolean canSave() {
		for(Setter<?> setter : Setters) {
			if(setter.getValue() == null) {
				return false;
			}
		}
		
		return true;
	}
	
	@EventHandler
	public void onInventoryClick(InventoryClickEvent e) {
		if(e.getInventory().equals(ScriptGUI)) {
			e.setCancelled(true);
			
			ItemStack Clicked = e.getCurrentItem();
			if(Clicked != null && !Clicked.getType().equals(Material.AIR)
			&& Clicked.hasItemMeta() && Clicked.getItemMeta().hasDisplayName()) {
				String displayName = Clicked.getItemMeta().getDisplayName();
				
				if(displayName.equals(ChatColor.translateAlternateColorCodes('&', "&b이전 페이지"))) {
					openScriptWizard(PlayerPage - 1);
				} else if(displayName.equals(ChatColor.translateAlternateColorCodes('&', "&b다음 페이지"))) {
					openScriptWizard(PlayerPage + 1);
				} else if(displayName.equals(ChatColor.translateAlternateColorCodes('&', "&a저장"))) {
					if(canSave()) {
						Exception exception = null;
						
						try {
							ArrayList<Class<?>> classList = new ArrayList<Class<?>>();
							ArrayList<Object> valueList = new ArrayList<Object>();
							
							//스크립트 이름
							classList.add(String.class);
							valueList.add(scriptName);
							
							//시간
							classList.add(int.class);
							valueList.add(Time.getValue());
							
							//반복 여부
							classList.add(boolean.class);
							valueList.add(Loop.getValue());

							//반복 횟수
							classList.add(int.class);
							valueList.add(LoopCount.getValue());
							
							//실행 예고 메시지
							classList.add(String.class);
							valueList.add(PreRunMessage.getValue());

							//실행 메시지
							classList.add(String.class);
							valueList.add(RunMessage.getValue());
							
							//커스텀 데이터
							for(Setter<?> setter : Setters) {
								classList.add(setter.getValue().getClass());
								valueList.add(setter.getValue());
							}
							
							Constructor<?> constructor = scriptClass.getConstructor(classList.toArray(new Class<?>[classList.size()]));
							AbstractScript script = (AbstractScript) constructor.newInstance(valueList.toArray(new Object[valueList.size()]));
							Script.Save(script);
							Script.AddScript(script);
						} catch(Exception ex) {
							exception = ex;
						}
						
						p.closeInventory();
						
						if(exception == null) {
							Messager.sendMessage(p, ChatColor.translateAlternateColorCodes('&', "&c" + scriptName + " &f스크립트를 저장하였습니다."));
						} else {
							Messager.sendMessage(p, ChatColor.translateAlternateColorCodes('&', "&c스크립트를 저장하던 도중 오류가 발생하였습니다."));

							if(exception.getMessage() != null && !exception.getMessage().isEmpty()) {
								if(exception instanceof NoSuchMethodException) {
									Messager.sendErrorMessage(exception.getMessage() + " 메소드가 존재하지 않습니다.");
								} else {
									Messager.sendErrorMessage(exception.getMessage());
								}
							}
						}
					}
				} else {
					String stripName = ChatColor.stripColor(displayName);
					for(Setter<?> setter : Setters) {
						if(stripName.equalsIgnoreCase(setter.getKey())) {
							setter.onClick(e.getClick());
						}
					}

					if(stripName.equalsIgnoreCase(Time.getKey())) Time.onClick(e.getClick());
					if(stripName.equalsIgnoreCase(Loop.getKey())) Loop.onClick(e.getClick());
					if(stripName.equalsIgnoreCase(LoopCount.getKey())) LoopCount.onClick(e.getClick());
					if(stripName.equalsIgnoreCase(PreRunMessage.getKey())) PreRunMessage.onClick(e.getClick());
					if(stripName.equalsIgnoreCase(RunMessage.getKey())) RunMessage.onClick(e.getClick());
				}
			}
		}
	}
	
	/**
	 * Listener가 Unregister되지 않게 인벤토리를 닫습니다.
	 */
	public void safeClose() {
		safeClose = true;
		p.closeInventory();
	}
	
	private boolean safeClose = false;
	
	@EventHandler
	public void onInventoryClose(InventoryCloseEvent e) {
		if(e.getInventory().equals(this.ScriptGUI)) {
			if(safeClose) {
				safeClose = false;
			} else {
				HandlerList.unregisterAll(this);
			}
		}
	}
	
	public Integer getPlayerPage() {
		return PlayerPage;
	}
	
	public Player getPlayer() {
		return p;
	}
	
}
