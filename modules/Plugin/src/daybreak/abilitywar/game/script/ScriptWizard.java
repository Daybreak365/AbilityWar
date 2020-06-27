package daybreak.abilitywar.game.script;

import daybreak.abilitywar.game.script.manager.ScriptManager;
import daybreak.abilitywar.game.script.manager.ScriptManager.RequiredData;
import daybreak.abilitywar.game.script.manager.ScriptManager.ScriptRegistration;
import daybreak.abilitywar.game.script.setter.Setter;
import daybreak.abilitywar.game.script.setter.special.LoopCountSetter;
import daybreak.abilitywar.game.script.setter.special.LoopSetter;
import daybreak.abilitywar.game.script.setter.special.MessageSetter;
import daybreak.abilitywar.game.script.setter.special.TimeSetter;
import daybreak.abilitywar.utils.base.logging.Logger;
import daybreak.abilitywar.utils.library.MaterialX;
import daybreak.abilitywar.utils.library.item.ItemBuilder;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
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

/**
 * Script Wizard
 *
 * @author Daybreak 새벽
 */
public class ScriptWizard implements Listener {

	private static final Logger logger = Logger.getLogger(ScriptWizard.class);

	private static final ItemStack DECO = new ItemBuilder()
			.type(MaterialX.WHITE_STAINED_GLASS_PANE)
			.displayName(ChatColor.WHITE.toString())
			.build();

	private static final ItemStack PREVIOUS_PAGE = new ItemBuilder()
			.type(Material.ARROW)
			.displayName(ChatColor.AQUA + "이전 페이지")
			.build();

	private static final ItemStack NEXT_PAGE = new ItemBuilder()
			.type(Material.ARROW)
			.displayName(ChatColor.AQUA + "다음 페이지")
			.build();

	private final Player player;
	private final Class<? extends AbstractScript> scriptClass;
	private final String scriptName;

	public ScriptWizard(Player player, Plugin plugin, Class<? extends AbstractScript> scriptClass, String scriptName) throws IllegalArgumentException {
		this.player = player;
		this.scriptClass = scriptClass;
		this.scriptName = scriptName;

		ScriptRegistration registration = ScriptManager.getRegistration(scriptClass);
		for (RequiredData<?> data : registration.getRequiredDatas()) {
			if (data.getSetterClass() == null) {
				Setter<?> setter = Setter.newInstance(data.getClazz(), data.getKey(), data.getDefault(), this);
				setters.add(setter);
			} else {
				Constructor<? extends Setter<?>> setterConstructor;
				try {
					setterConstructor = data.getSetterClass().getConstructor(String.class, data.getClazz(), ScriptWizard.class);
					Setter<?> setter = setterConstructor.newInstance(data.getKey(), data.getDefault(), this);
					setters.add(setter);
				} catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException | SecurityException ex) {
					throw new IllegalArgumentException();
				}
			}
		}

		Bukkit.getPluginManager().registerEvents(this, plugin);
	}

	private int playerPage = 1;

	private Inventory gui;

	// Default Setters
	public final TimeSetter timeSetter = new TimeSetter(this);
	public final LoopSetter loopSetter = new LoopSetter(this);
	public final LoopCountSetter loopCountSetter = new LoopCountSetter(this);
	public final MessageSetter preRunMessageSetter = new MessageSetter("실행 예고 메시지", "§e%Time%§f초 후에 §e%ScriptName% §f스크립트가 실행됩니다.", this);
	public final MessageSetter runMessageSetter = new MessageSetter("실행 메시지", "§e%ScriptName% §f스크립트가 실행되었습니다.", this);
	// Default Setters

	private final List<Setter<?>> setters = new ArrayList<>();

	public void openGUI(int page) {
		playerPage = page;
		gui = Bukkit.createInventory(null, 45, "§c" + scriptName + " §0스크립트 편집");

		int maxPage = ((setters.size() - 1) / 18) + 1;
		if (maxPage < page) page = 1;
		if (page < 1) page = 1;

		int count = 0;
		for (Setter<?> setter : setters) {
			if (count / 18 == page - 1) {
				gui.setItem((count % 36) + 18, setter.getItem());
			}
			count++;
		}

		gui.setItem(2, timeSetter.getItem());
		gui.setItem(3, loopSetter.getItem());
		gui.setItem(4, loopCountSetter.getItem());
		gui.setItem(5, preRunMessageSetter.getItem());
		gui.setItem(6, runMessageSetter.getItem());

		for (int i = 0; i < 45; i++) {
			if (i == 0 || i == 1 || i == 7 || i == 8 || (i >= 9 && i <= 17) || i >= 36) gui.setItem(i, DECO);
		}

		if (page > 1) gui.setItem(39, PREVIOUS_PAGE);
		if (page != maxPage) gui.setItem(41, NEXT_PAGE);

		ItemStack pageStack = new ItemStack(Material.PAPER, 1);
		ItemMeta pageMeta = pageStack.getItemMeta();
		pageMeta.setDisplayName("§6페이지 §e" + page + " §6/ §e" + maxPage);
		pageStack.setItemMeta(pageMeta);
		gui.setItem(40, pageStack);

		ItemStack confirmStack = new ItemStack(Material.PAPER, 1);
		ItemMeta confirmMeta = confirmStack.getItemMeta();
		confirmMeta.setDisplayName("§a저장");
		confirmMeta.setLore(Arrays.asList(
				"§f저장 " + (canSave() ? "§a가능" : "§c불가능")
		));
		confirmStack.setItemMeta(confirmMeta);
		gui.setItem(44, confirmStack);

		player.openInventory(gui);
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
		if (e.getInventory().equals(gui)) {
			e.setCancelled(true);

			ItemStack clicked = e.getCurrentItem();
			if (clicked != null && !clicked.getType().equals(Material.AIR) && clicked.hasItemMeta() && clicked.getItemMeta().hasDisplayName()) {
				String displayName = clicked.getItemMeta().getDisplayName();

				if (displayName.equals(ChatColor.AQUA + "이전 페이지")) {
					openGUI(playerPage - 1);
				} else if (displayName.equals(ChatColor.AQUA + "다음 페이지")) {
					openGUI(playerPage + 1);
				} else if (displayName.equals("§a저장")) {
					if (canSave()) {
						Exception exception = null;

						try {
							List<Class<?>> classList = new ArrayList<>();
							List<Object> valueList = new ArrayList<>();

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

							Constructor<?> constructor = scriptClass.getConstructor(classList.toArray(new Class<?>[0]));
							AbstractScript script = (AbstractScript) constructor.newInstance(valueList.toArray(new Object[0]));
							ScriptManager.Save(script);
							ScriptManager.addScript(script);
						} catch (Exception ex) {
							exception = ex;
						}

						player.closeInventory();

						if (exception == null) {
							player.sendMessage("§c" + scriptName + " §f스크립트를 저장하였습니다.");
						} else {
							player.sendMessage("§c스크립트를 저장하던 도중 오류가 발생하였습니다.");

							if (exception.getMessage() != null && !exception.getMessage().isEmpty()) {
								logger.error(exception instanceof NoSuchMethodException ? exception.getMessage() + " 메소드가 존재하지 않습니다." : exception.getMessage());
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
		player.closeInventory();
	}

	private boolean safeClose = false;

	@EventHandler
	private void onInventoryClose(InventoryCloseEvent e) {
		if (e.getInventory().equals(this.gui)) {
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
		return player;
	}

}