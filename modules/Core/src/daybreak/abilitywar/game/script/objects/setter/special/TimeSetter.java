package daybreak.abilitywar.game.script.objects.setter.special;

import daybreak.abilitywar.game.script.ScriptWizard;
import daybreak.abilitywar.game.script.objects.setter.Setter;
import daybreak.abilitywar.utils.library.MaterialX;
import daybreak.abilitywar.utils.math.NumberUtil;
import org.bukkit.ChatColor;
import org.bukkit.event.Event;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class TimeSetter extends Setter<Integer> {

	public TimeSetter(ScriptWizard Wizard) {
		super("시간", 30, Wizard);
	}

	@Override
	public void execute(Listener listener, Event event) {
	}

	@Override
	public void onClick(ClickType click) {
		if (click.equals(ClickType.RIGHT)) {
			this.setValue(this.getValue() + 1);
		} else if (click.equals(ClickType.SHIFT_RIGHT)) {
			this.setValue(this.getValue() + 60);
		} else if (click.equals(ClickType.LEFT)) {
			if (this.getValue() >= 2) {
				this.setValue(this.getValue() - 1);
			} else {
				this.setValue(1);
			}
		} else if (click.equals(ClickType.SHIFT_LEFT)) {
			if (this.getValue() >= 61) {
				this.setValue(this.getValue() - 60);
			} else {
				this.setValue(1);
			}
		}
	}

	@Override
	public ItemStack getItem() {
		ItemStack watch = MaterialX.CLOCK.parseItem();
		ItemMeta watchMeta = watch.getItemMeta();
		watchMeta.setDisplayName(ChatColor.AQUA + this.getKey());

		List<String> Lore = new ArrayList<String>();

		if (getWizard().loopSetter.getValue()) {
			Lore.add(ChatColor.translateAlternateColorCodes('&', "&f게임 시작 후 &e" + NumberUtil.parseTimeString(this.getValue()) + "&f마다 실행됩니다."));
		} else {
			Lore.add(ChatColor.translateAlternateColorCodes('&', "&f게임 시작 &e" + NumberUtil.parseTimeString(this.getValue()) + " &f후에 실행됩니다."));
		}

		Lore.add("");
		Lore.add(ChatColor.translateAlternateColorCodes('&', "&c우클릭         &6» &e+ 1초"));
		Lore.add(ChatColor.translateAlternateColorCodes('&', "&cSHIFT + 우클릭 &6» &e+ 1분"));
		Lore.add(ChatColor.translateAlternateColorCodes('&', "&c좌클릭         &6» &e- 1초"));
		Lore.add(ChatColor.translateAlternateColorCodes('&', "&cSHIFT + 좌클릭 &6» &e- 1분"));

		watchMeta.setLore(Lore);

		watch.setItemMeta(watchMeta);

		return watch;
	}

}