package daybreak.abilitywar.game.script.setter.special;

import daybreak.abilitywar.game.script.ScriptWizard;
import daybreak.abilitywar.game.script.setter.Setter;
import daybreak.abilitywar.utils.base.Messager;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.event.Event;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class LoopSetter extends Setter<Boolean> {

	public LoopSetter(ScriptWizard Wizard) {
		super("반복 실행", false, Wizard);
	}

	@Override
	public void execute(Listener listener, Event event) {
	}

	@Override
	public void onClick(ClickType click) {
		this.setValue(!this.getValue());
	}

	@Override
	public ItemStack getItem() {
		ItemStack loop = new ItemStack(Material.BOOK);
		ItemMeta loopMeta = loop.getItemMeta();
		loopMeta.setDisplayName(ChatColor.AQUA + this.getKey());
		if (this.getValue()) {
			loopMeta.setLore(Messager.asList(
					"§a반복 실행",
					"§7한번 실행"
			));
		} else {
			loopMeta.setLore(Messager.asList(
					"§7반복 실행",
					"§a한번 실행"
			));
		}

		loop.setItemMeta(loopMeta);

		return loop;
	}

}