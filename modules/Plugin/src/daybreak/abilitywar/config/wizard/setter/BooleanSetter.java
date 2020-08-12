package daybreak.abilitywar.config.wizard.setter;

import daybreak.abilitywar.config.interfaces.Configurable;
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
	public boolean onClick(Configurable<?> configurable, ClickType clickType) {
		@SuppressWarnings("unchecked") Configurable<Boolean> settingObject = (Configurable<Boolean>) configurable;
		settingObject.setValue(!settingObject.getValue());
		return true;
	}

	@Override
	public ItemStack getItem(Configurable<?> configurable) {
		ItemStack wool = MaterialX.LIME_WOOL.createItem();
		ItemMeta woolMeta = wool.getItemMeta();
		woolMeta.setDisplayName(ChatColor.WHITE + configurable.getKey());
		String[] comments = configurable.getComments();
		List<String> lore = new ArrayList<>(comments.length + 1);
		lore.add("§9값§f: " + configurable.getValue());
		for (String comment : comments) {
			lore.add(ChatColor.GRAY + comment);
		}
		woolMeta.setLore(lore);
		wool.setItemMeta(woolMeta);
		return wool;
	}

}
