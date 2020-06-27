package daybreak.abilitywar.game.script.setter;

import daybreak.abilitywar.game.script.ScriptWizard;
import daybreak.abilitywar.utils.base.Messager;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class LocationSetter extends Setter<Location> {

	public LocationSetter(String Key, Location Default, ScriptWizard Wizard) {
		super(Key, Default, Wizard);

		registerEvent(PlayerInteractEvent.class);
	}

	@Override
	public void execute(Listener listener, Event event) {
		if (event instanceof PlayerInteractEvent) {
			PlayerInteractEvent e = (PlayerInteractEvent) event;
			if (e.getPlayer().equals(getWizard().getPlayer())) {
				Action action = e.getAction();
				if (action.equals(Action.LEFT_CLICK_BLOCK)) {
					if (Setting) {
						Setting = false;
						e.setCancelled(true);

						this.setValue(e.getClickedBlock().getLocation());
					}
				} else if (action.equals(Action.RIGHT_CLICK_BLOCK)) {
					if (Setting) {
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
		Player p = getWizard().getPlayer();
		p.sendMessage("§f지정할 §6위치§f를 클릭해주세요. §e좌클릭§f은 클릭한 블록의 위치를 저장하고,");
		p.sendMessage("§e우클릭§f은 클릭한 블록 위의 위치를 저장합니다.");
	}

	@Override
	public ItemStack getItem() {
		ItemStack loc = new ItemStack(Material.COMPASS);
		ItemMeta locMeta = loc.getItemMeta();
		locMeta.setDisplayName(ChatColor.AQUA + this.getKey());
		if (this.getValue() != null) {
			Location l = this.getValue();
			String world = l.getWorld().getName();
			double X = l.getX();
			double Y = l.getY();
			double Z = l.getZ();
			locMeta.setLore(Messager.asList(
					"§a월드§f: " + world,
					"§bX§f: " + X,
					"§bY§f: " + Y,
					"§bZ§f: " + Z
			));
		} else {
			locMeta.setLore(Messager.asList(
					"§f지정된 위치가 없습니다."
			));
		}

		loc.setItemMeta(locMeta);

		return loc;
	}

}
