package DayBreak.AbilityWar.Game.Script.Objects.Setter.Special;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.event.Event;
import org.bukkit.event.EventException;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import DayBreak.AbilityWar.Game.Script.ScriptWizard;
import DayBreak.AbilityWar.Game.Script.Objects.Setter.Setter;
import DayBreak.AbilityWar.Utils.Messager;

public class LoopSetter extends Setter<Boolean> {
	
	public LoopSetter(ScriptWizard Wizard) {
		super("반복 실행", false, Wizard);
	}

	@Override
	public void execute(Listener listener, Event event) throws EventException {}

	@Override
	public void onClick(ClickType click) {
		this.setValue(!this.getValue());
	}
	
	@Override
	public ItemStack getItem() {
		ItemStack loop = new ItemStack(Material.BOOK);
		ItemMeta loopMeta = loop.getItemMeta();
		loopMeta.setDisplayName(ChatColor.AQUA + this.getKey());
		if(this.getValue()) {
			loopMeta.setLore(Messager.getStringList(
					ChatColor.translateAlternateColorCodes('&', "&a반복 실행"),
					ChatColor.translateAlternateColorCodes('&', "&7한번 실행")
					));
		} else {
			loopMeta.setLore(Messager.getStringList(
					ChatColor.translateAlternateColorCodes('&', "&7반복 실행"),
					ChatColor.translateAlternateColorCodes('&', "&a한번 실행")
					));
		}
		
		loop.setItemMeta(loopMeta);
		
		return loop;
	}
	
}