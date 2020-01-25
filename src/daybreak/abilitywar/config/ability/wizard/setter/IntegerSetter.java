package daybreak.abilitywar.config.ability.wizard.setter;

import daybreak.abilitywar.config.ability.AbilitySettings.SettingObject;
import daybreak.abilitywar.utils.library.MaterialX;
import org.bukkit.ChatColor;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class IntegerSetter extends Setter {

	static final Setter instance = new IntegerSetter();

	private IntegerSetter() {
	}

	@Override
	public boolean onClick(SettingObject<?> object, ClickType clickType) {
		@SuppressWarnings("unchecked") SettingObject<Integer> settingObject = (SettingObject<Integer>) object;
		switch (clickType) {
			case RIGHT:
				if (!settingObject.setValue(settingObject.getValue() + 1)) return false;
				break;
			case SHIFT_RIGHT:
				if (!settingObject.setValue(settingObject.getValue() + 20)) return false;
				break;
			case LEFT:
				if (!settingObject.setValue(settingObject.getValue() - 1)) return false;
				break;
			case SHIFT_LEFT:
				if (!settingObject.setValue(settingObject.getValue() - 20)) return false;
				break;
		}
		return true;
	}

	@Override
	public ItemStack getItem(SettingObject<?> settingObject) {
		ItemStack wool = MaterialX.BLUE_WOOL.parseItem();
		ItemMeta woolMeta = wool.getItemMeta();
		woolMeta.setDisplayName(ChatColor.WHITE + settingObject.getKey());
		String[] comments = settingObject.getComments();
		List<String> lore = new ArrayList<>(comments.length + 6);
		lore.add(ChatColor.translateAlternateColorCodes('&', "&9값&f: " + settingObject.getValue()));
		for (String comment : comments) {
			lore.add(ChatColor.GRAY + comment);
		}
		lore.addAll(Arrays.asList(
				"",
				ChatColor.translateAlternateColorCodes('&', "&c우클릭         &6» &e+ 1"),
				ChatColor.translateAlternateColorCodes('&', "&cSHIFT + 우클릭 &6» &e+ 20"),
				ChatColor.translateAlternateColorCodes('&', "&c좌클릭         &6» &e- 1"),
				ChatColor.translateAlternateColorCodes('&', "&cSHIFT + 좌클릭 &6» &e- 20")
		));
		woolMeta.setLore(lore);
		wool.setItemMeta(woolMeta);
		return wool;
	}

}
