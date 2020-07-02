package daybreak.abilitywar.game.script.setter.special;

import daybreak.abilitywar.game.script.ScriptWizard;
import daybreak.abilitywar.game.script.setter.Setter;
import daybreak.abilitywar.utils.base.TimeUtil;
import daybreak.abilitywar.utils.library.MaterialX;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.ChatColor;
import org.bukkit.event.Event;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

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

		List<String> Lore = new ArrayList<>();

		if (getWizard().loopSetter.getValue()) {
			Lore.add("§f게임 시작 후 §e" + TimeUtil.parseTimeAsString(this.getValue()) + "§f마다 실행됩니다.");
		} else {
			Lore.add("§f게임 시작 §e" + TimeUtil.parseTimeAsString(this.getValue()) + " §f후에 실행됩니다.");
		}

		Lore.add("");
		Lore.add("§c우클릭         §6» §e+ 1초");
		Lore.add("§cSHIFT + 우클릭 §6» §e+ 1분");
		Lore.add("§c좌클릭         §6» §e- 1초");
		Lore.add("§cSHIFT + 좌클릭 §6» §e- 1분");

		watchMeta.setLore(Lore);

		watch.setItemMeta(watchMeta);

		return watch;
	}

}