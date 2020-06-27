package daybreak.abilitywar.config.ability.wizard.setter;

import daybreak.abilitywar.config.ability.AbilitySettings.SettingObject;
import daybreak.abilitywar.utils.library.MaterialX;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.ChatColor;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class BooleanSetter extends Setter {

	static final Setter instance = new BooleanSetter();

	private BooleanSetter() {
	}

	@Override
	public boolean onClick(SettingObject<?> object, ClickType clickType) {
		@SuppressWarnings("unchecked") SettingObject<Boolean> settingObject = (SettingObject<Boolean>) object;
		settingObject.setValue(!settingObject.getValue());
		return true;
	}

	@Override
	public ItemStack getItem(SettingObject<?> settingObject) {
		ItemStack wool = MaterialX.LIME_WOOL.parseItem();
		ItemMeta woolMeta = wool.getItemMeta();
		woolMeta.setDisplayName(ChatColor.WHITE + settingObject.getKey());
		String[] comments = settingObject.getComments();
		List<String> lore = new ArrayList<>(comments.length + 1);
		lore.add("§9값§f: " + settingObject.getValue());
		for (String comment : comments) {
			lore.add(ChatColor.GRAY + comment);
		}
		woolMeta.setLore(lore);
		wool.setItemMeta(woolMeta);
		return wool;
	}

}
