package DayBreak.AbilityWar.Game.Script.Objects.Setter;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.event.Event;
import org.bukkit.event.EventException;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import DayBreak.AbilityWar.Game.Script.ScriptWizard;
import DayBreak.AbilityWar.Utils.Messager;

public class LocationSetter extends Setter<Location> {

	public LocationSetter(String Key, Location Default, ScriptWizard Wizard) {
		super(Key, Default, Wizard);

		registerEvent(PlayerInteractEvent.class);
	}

	@Override
	public void execute(Listener listener, Event event) throws EventException {
		if(event instanceof PlayerInteractEvent) {
			PlayerInteractEvent e = (PlayerInteractEvent) event;
			if(e.getPlayer().equals(getWizard().getPlayer())) {
				Action action = e.getAction();
				if(action.equals(Action.LEFT_CLICK_BLOCK)) {
					if(Setting) {
						Setting = false;
						e.setCancelled(true);
						
						this.setValue(e.getClickedBlock().getLocation());
					}
				} else if(action.equals(Action.RIGHT_CLICK_BLOCK)) {
					if(Setting) {
						Setting = false;
						e.setCancelled(true);
						
						this.setValue(e.getClickedBlock().getLocation().add(0, 1, 0));
					}
				}
			}
		}
	}
	
	private boolean Setting = false;
	
	@Override
	public void onClick(ClickType click) {
		Setting = true;
		getWizard().safeClose();
		Messager.sendMessage(getWizard().getPlayer(), ChatColor.translateAlternateColorCodes('&', "&f지정할 &6위치&f를 클릭해주세요. &e좌클릭&f은 클릭한 블록의 위치를 저장하고,"));
		Messager.sendMessage(getWizard().getPlayer(), ChatColor.translateAlternateColorCodes('&', "&e우클릭&f은 클릭한 블록 위의 위치를 저장합니다."));
	}

	@Override
	public ItemStack getItem() {
		ItemStack loc = new ItemStack(Material.COMPASS);
		ItemMeta locMeta = loc.getItemMeta();
		locMeta.setDisplayName(ChatColor.AQUA + this.getKey());
		if(this.getValue() != null) {
			Location l = this.getValue();
			String world = l.getWorld().getName();
			Double X = l.getX();
			Double Y = l.getY();
			Double Z = l.getZ();
			locMeta.setLore(Messager.getStringList(
					ChatColor.translateAlternateColorCodes('&', "&a월드&f: " + world),
					ChatColor.translateAlternateColorCodes('&', "&bX&f: " + X),
					ChatColor.translateAlternateColorCodes('&', "&bY&f: " + Y),
					ChatColor.translateAlternateColorCodes('&', "&bZ&f: " + Z)
					));
		} else {
			locMeta.setLore(Messager.getStringList(
					ChatColor.translateAlternateColorCodes('&', "&f지정된 위치가 없습니다.")
					));
		}
		
		loc.setItemMeta(locMeta);
		
		return loc;
	}

}
