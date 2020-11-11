package daybreak.abilitywar.config.wizard.setter;

import daybreak.abilitywar.config.interfaces.Configurable;
import daybreak.abilitywar.utils.library.MaterialX;
import org.bukkit.ChatColor;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class FloatSetter extends Setter {

	static final Setter instance = new FloatSetter();

	private FloatSetter() {
	}

	@Override
	public boolean onClick(Configurable<?> configurable, ClickType clickType) {
		@SuppressWarnings("unchecked") Configurable<Float> settingObject = (Configurable<Float>) configurable;
		switch (clickType) {
			case RIGHT:
				if (!settingObject.setValue(round(settingObject.getValue() + 1f))) return false;
				break;
			case SHIFT_RIGHT:
				if (!settingObject.setValue(round(settingObject.getValue() + .1f))) return false;
				break;
			case LEFT:
				if (!settingObject.setValue(round(settingObject.getValue() - 1f))) return false;
				break;
			case SHIFT_LEFT:
				if (!settingObject.setValue(round(settingObject.getValue() - .1f))) return false;
				break;
		}
		return true;
	}

	private float round(final float a) {
		return Math.round(a * 10f) / 10f;
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
