package daybreak.abilitywar.game.script;

import daybreak.abilitywar.game.script.ScriptManager.RequiredData;
import daybreak.abilitywar.game.script.ScriptManager.ScriptRegisteration;
import daybreak.abilitywar.game.script.objects.AbstractScript;
import daybreak.abilitywar.game.script.objects.setter.Setter;
import daybreak.abilitywar.game.script.objects.setter.special.LoopCountSetter;
import daybreak.abilitywar.game.script.objects.setter.special.LoopSetter;
import daybreak.abilitywar.game.script.objects.setter.special.MessageSetter;
import daybreak.abilitywar.game.script.objects.setter.special.TimeSetter;
import daybreak.abilitywar.utils.Messager;
import daybreak.abilitywar.utils.library.MaterialLib;
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

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * Script Wizard
 *
 * @author Daybreak 새벽
 */
public class ScriptWizard implements Listener {

	private final Player p;
	private final Class<? extends AbstractScript> scriptClass;
	private final String scriptName;

	public ScriptWizard(Player p, Plugin Plugin, Class<? extends AbstractScript> scriptClass, String scriptName) throws IllegalArgumentException, ScriptException {
		this.p = p;
		this.scriptClass = scriptClass;
		this.scriptName = scriptName;

		ScriptRegisteration reg = ScriptManager.getRegisteration(scriptClass);
		for (RequiredData<?> data : reg.getRequiredDatas()) {
			if (data.getSetterClass() == null) {
				Setter<?> setter = Setter.newInstance(data.getClazz(), data.getKey(), data.getDefault(), this);
				setters.add(setter);
			} else {
				Constructor<? extends Setter<?>> setterConstructor;
				try {
					setterConstructor = data.getSetterClass().getConstructor(String.class, data.getClazz(), ScriptWizard.class);
					Setter<?> setter = setterConstructor.newInstance(data.getKey(), data.getDefault(), this);
					setters.add(setter);
				} catch (InstantiationException | IllegalAccessException | InvocationTargetException
						| NoSuchMethodException | SecurityException ex) {
					throw new IllegalArgumentException();
				}
			}
		}

		Bukkit.getPluginManager().registerEvents(this, Plugin);
	}

	private int playerPage = 1;

	private Inventory scriptGUI;

	// Default Setters
	public TimeSetter timeSetter = new TimeSetter(this);
	public LoopSetter loopSetter = new LoopSetter(this);
	public LoopCountSetter loopCountSetter = new LoopCountSetter(this);
	public MessageSetter preRunMessageSetter = new MessageSetter("실행 예고 메시지", "&e%Time%&f초 후에 &e%ScriptName% &f스크립트가 실행됩니다.", this);
	public MessageSetter runMessageSetter = new MessageSetter("실행 메시지", "&e%ScriptName% &f스크립트가 실행되었습니다.", this);
	// Default Setters

	private ArrayList<Setter<?>> setters = new ArrayList<Setter<?>>();

	public void openScriptWizard(int page) {
		playerPage = page;
		scriptGUI = Bukkit.createInventory(null, 45, ChatColor.translateAlternateColorCodes('&', "&c" + scriptName + " &0스크립트 편집"));

		int maxPage = ((setters.size() - 1) / 18) + 1;
		if (maxPage < page) page = 1;
		if (page < 1) page = 1;

		int count = 0;
		for (Setter<?> setter : setters) {
			if (count / 18 == page - 1) {
				scriptGUI.setItem((count % 36) + 18, setter.getItem());
			}
			count++;
		}

		scriptGUI.setItem(2, timeSetter.getItem());
		scriptGUI.setItem(3, loopSetter.getItem());
		scriptGUI.setItem(4, loopCountSetter.getItem());
		scriptGUI.setItem(5, preRunMessageSetter.getItem());
		scriptGUI.setItem(6, runMessageSetter.getItem());

		ItemStack Deco = MaterialLib.WHITE_STAINED_GLASS_PANE.getItem();
		ItemMeta DecoMeta = Deco.getItemMeta();
		DecoMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&f"));
		Deco.setItemMeta(DecoMeta);

		for (Integer i = 0; i < 45; i++) {
			if (i.equals(0) || i.equals(1) || i.equals(7) || i.equals(8)
					|| (i >= 9 && i <= 17) || (i >= 36 && i <= 44)) {
				scriptGUI.setItem(i, Deco);
			}
		}

		if (page > 1) {
			ItemStack previousPage = new ItemStack(Material.ARROW, 1);
			ItemMeta previousMeta = previousPage.getItemMeta();
			previousMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&b이전 페이지"));
			previousPage.setItemMeta(previousMeta);
			scriptGUI.setItem(39, previousPage);
		}

		if (page != maxPage) {
			ItemStack nextPage = new ItemStack(Material.ARROW, 1);
			ItemMeta nextMeta = nextPage.getItemMeta();
			nextMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&b다음 페이지"));
			nextPage.setItemMeta(nextMeta);
			scriptGUI.setItem(41, nextPage);
		}

		ItemStack pageStack = new ItemStack(Material.PAPER, 1);
		ItemMeta pageMeta = pageStack.getItemMeta();
		pageMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&',
				"&6페이지 &e" + page + " &6/ &e" + maxPage));
		pageStack.setItemMeta(pageMeta);
		scriptGUI.setItem(40, pageStack);

		ItemStack confirmStack = new ItemStack(Material.PAPER, 1);
		ItemMeta confirmMeta = confirmStack.getItemMeta();
		confirmMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&a저장"));
		confirmMeta.setLore(Arrays.asList(
				ChatColor.translateAlternateColorCodes('&', "&f저장 " + (canSave() ? "&a가능" : "&c불가능"))
		));
		confirmStack.setItemMeta(confirmMeta);
		scriptGUI.setItem(44, confirmStack);

		p.openInventory(scriptGUI);
	}

	private boolean canSave() {
		for (Setter<?> setter : setters) {
			if (setter.getValue() == null) {
				return false;
			}
		}

		return true;
	}

	@EventHandler
	private void onInventoryClick(InventoryClickEvent e) {
		if (e.getInventory().equals(scriptGUI)) {
			e.setCancelled(true);

			ItemStack clicked = e.getCurrentItem();
			if (clicked != null && !clicked.getType().equals(Material.AIR)
					&& clicked.hasItemMeta() && clicked.getItemMeta().hasDisplayName()) {
				String displayName = clicked.getItemMeta().getDisplayName();

				if (displayName.equals(ChatColor.translateAlternateColorCodes('&', "&b이전 페이지"))) {
					openScriptWizard(playerPage - 1);
				} else if (displayName.equals(ChatColor.translateAlternateColorCodes('&', "&b다음 페이지"))) {
					openScriptWizard(playerPage + 1);
				} else if (displayName.equals(ChatColor.translateAlternateColorCodes('&', "&a저장"))) {
					if (canSave()) {
						Exception exception = null;

						try {
							ArrayList<Class<?>> classList = new ArrayList<Class<?>>();
							ArrayList<Object> valueList = new ArrayList<Object>();

							//스크립트 이름
							classList.add(String.class);
							valueList.add(scriptName);

							//시간
							classList.add(int.class);
							valueList.add(timeSetter.getValue());

							//반복 횟수
							classList.add(int.class);
							valueList.add(loopSetter.getValue() ? (loopCountSetter.getValue()) : 0);

							//실행 예고 메시지
							classList.add(String.class);
							valueList.add(preRunMessageSetter.getValue());

							//실행 메시지
							classList.add(String.class);
							valueList.add(runMessageSetter.getValue());

							//커스텀 데이터
							for (Setter<?> setter : setters) {
								classList.add(setter.getClazz());
								valueList.add(setter.getValue());
							}

							Constructor<?> constructor = scriptClass.getConstructor(classList.toArray(new Class<?>[classList.size()]));
							AbstractScript script = (AbstractScript) constructor.newInstance(valueList.toArray(new Object[valueList.size()]));
							ScriptManager.Save(script);
							ScriptManager.AddScript(script);
						} catch (Exception ex) {
							exception = ex;
						}

						p.closeInventory();

						if (exception == null) {
							p.sendMessage(ChatColor.translateAlternateColorCodes('&', "&c" + scriptName + " &f스크립트를 저장하였습니다."));
						} else {
							p.sendMessage(ChatColor.translateAlternateColorCodes('&', "&c스크립트를 저장하던 도중 오류가 발생하였습니다."));

							if (exception.getMessage() != null && !exception.getMessage().isEmpty()) {
								if (exception instanceof NoSuchMethodException) {
									Messager.sendConsoleErrorMessage(exception.getMessage() + " 메소드가 존재하지 않습니다.");
								} else {
									Messager.sendConsoleErrorMessage(exception.getMessage());
								}
							}
						}
					}
				} else {
					String stripName = ChatColor.stripColor(displayName);
					for (Setter<?> setter : setters) {
						if (stripName.equalsIgnoreCase(setter.getKey())) {
							setter.onClick(e.getClick());
						}
					}

					if (stripName.equalsIgnoreCase(timeSetter.getKey())) timeSetter.onClick(e.getClick());
					if (stripName.equalsIgnoreCase(loopSetter.getKey())) loopSetter.onClick(e.getClick());
					if (stripName.equalsIgnoreCase(loopCountSetter.getKey())) loopCountSetter.onClick(e.getClick());
					if (stripName.equalsIgnoreCase(preRunMessageSetter.getKey()))
						preRunMessageSetter.onClick(e.getClick());
					if (stripName.equalsIgnoreCase(runMessageSetter.getKey())) runMessageSetter.onClick(e.getClick());
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
	private void onInventoryClose(InventoryCloseEvent e) {
		if (e.getInventory().equals(this.scriptGUI)) {
			if (safeClose) {
				safeClose = false;
			} else {
				HandlerList.unregisterAll(this);
			}
		}
	}

	public Integer getPlayerPage() {
		return playerPage;
	}

	public Player getPlayer() {
		return p;
	}

}