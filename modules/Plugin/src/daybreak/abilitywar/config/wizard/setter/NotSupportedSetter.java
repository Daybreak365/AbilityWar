package daybreak.abilitywar.config.wizard.setter;

import daybreak.abilitywar.config.interfaces.Configurable;
import daybreak.abilitywar.utils.library.MaterialX;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.ChatColor;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class NotSupportedSetter extends Setter {

	static final Setter instance = new NotSupportedSetter();

	@Override
	public boolean onClick(Configurable<?> configurable, ClickType clickType) {
		return false;
	}

	@Override
	public ItemStack getItem(Configurable<?> configurable) {
		ItemStack wool = MaterialX.RED_WOOL.createItem();
		ItemMeta woolMeta = wool.getItemMeta();
		woolMeta.setDisplayName(ChatColor.WHITE + configurable.getKey());
		String[] comments = configurable.getComments();
		List<String> lore = new ArrayList<>(comments.length + 5);
		for (String comment : comments) {
			lore.add(ChatColor.GRAY + comment);
		}
		lore.add("");
		if (configurable.getValue() == null) {
			lore.add("§c지원되지 않는 데이터 타입입니다.");
		} else {
			lore.add("§c지원되지 않는 데이터 타입입니다: " + configurable.getValue().getClass().getSimpleName());
		}
		lore.add("§c콘피그에서 직접 변경해주세요.");

		woolMeta.setLore(lore);
		wool.setItemMeta(woolMeta);
		return wool;
	}

}
