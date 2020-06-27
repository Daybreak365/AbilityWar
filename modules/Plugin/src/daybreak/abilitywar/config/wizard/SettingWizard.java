package daybreak.abilitywar.config.wizard;

import daybreak.abilitywar.config.Configuration;
import daybreak.abilitywar.utils.base.logging.Logger;
import daybreak.abilitywar.utils.library.MaterialX;
import daybreak.abilitywar.utils.library.item.ItemBuilder;
import java.io.IOException;
import java.util.logging.Level;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

public abstract class SettingWizard {

	static final Logger logger = Logger.getLogger(SettingWizard.class.getName());

	protected static final ItemStack DECO = new ItemBuilder()
			.type(MaterialX.WHITE_STAINED_GLASS_PANE)
			.displayName(ChatColor.WHITE.toString())
			.build();

	private Inventory gui = null;
	private final int inventorySize;
	private final String inventoryName;
	final Player player;

	SettingWizard(Player player, int inventorySize, String inventoryName, Plugin plugin) {
		this.inventorySize = inventorySize;
		this.inventoryName = inventoryName;
		this.player = player;
		new Listener() {
			{
				Bukkit.getPluginManager().registerEvents(this, plugin);
			}

			@EventHandler
			private void onInventoryClose(InventoryCloseEvent e) {
				if (e.getInventory().equals(gui)) {
					HandlerList.unregisterAll(this);
					try {
						Configuration.update();
					} catch (IOException | InvalidConfigurationException e1) {
						logger.log(Level.SEVERE, "콘피그를 업데이트하는 도중 오류가 발생하였습니다.");
					}
				}
			}

			@EventHandler
			private void onInventoryClick(InventoryClickEvent e) {
				if (e.getInventory().equals(gui)) {
					onClick(e, gui);
				}
			}

		};
	}

	public void Show() {
		this.gui = Bukkit.createInventory(null, inventorySize, inventoryName);
		openGUI(gui);
	}

	abstract void openGUI(Inventory gui);

	abstract void onClick(InventoryClickEvent e, Inventory gui);

}
