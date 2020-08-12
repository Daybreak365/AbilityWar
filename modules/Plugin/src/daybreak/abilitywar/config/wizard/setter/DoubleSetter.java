package daybreak.abilitywar.config.wizard.setter;

import daybreak.abilitywar.config.interfaces.Configurable;
import daybreak.abilitywar.utils.library.MaterialX;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.bukkit.ChatColor;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class DoubleSetter extends Setter {

	static final Setter instance = new DoubleSetter();

	private DoubleSetter() {
	}

	@Override
	public boolean onClick(Configurable<?> configurable, ClickType clickType) {
		@SuppressWarnings("unchecked") Configurable<Double> settingObject = (Configurable<Double>) configurable;
		switch (clickType) {
			case RIGHT:
				if (!settingObject.setValue(round(settingObject.getValue() + 1d))) return false;
				break;
			case SHIFT_RIGHT:
				if (!settingObject.setValue(round(settingObject.getValue() + .1d))) return false;
				break;
			case LEFT:
				if (!settingObject.setValue(round(settingObject.getValue() - 1d))) return false;
				break;
			case SHIFT_LEFT:
				if (!settingObject.setValue(round(settingObject.getValue() - .1d))) return false;
				break;
		}
		return true;
	}

	private double round(final double a) {
		return Math.round(a * 10.0) / 10.0;
	}

	@Override
	public ItemStack getItem(Configurable<?> configurable) {
		final ItemStack stack = MaterialX.BLUE_WOOL.createItem();
		final ItemMeta meta = stack.getItemMeta();
		meta.setDisplayName(ChatColor.WHITE + configurable.getKey());
		final String[] comments = configurable.getComments();
		final List<String> lore = new ArrayList<>(comments.length + 6);
		lore.add("§9값§f: " + configurable.getValue());
		for (String comment : comments) {
			lore.add(ChatColor.GRAY + comment);
		}
		lore.addAll(Arrays.asList(
				"",
				"§c우클릭         §6» §e+ 1",
				"§cSHIFT + 우클릭 §6» §e+ 0.1",
				"§c좌클릭         §6» §e- 1",
				"§cSHIFT + 좌클릭 §6» §e- 0.1"
		));
		meta.setLore(lore);
		stack.setItemMeta(meta);
		return stack;
	}

}
