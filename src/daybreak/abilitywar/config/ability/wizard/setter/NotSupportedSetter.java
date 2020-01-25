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

public class NotSupportedSetter extends Setter {

	static final Setter instance = new NotSupportedSetter();

	@Override
	public boolean onClick(SettingObject<?> settingObject, ClickType clickType) {
		return false;
	}

	@Override
	public ItemStack getItem(SettingObject<?> settingObject) {
		ItemStack wool = MaterialX.RED_WOOL.parseItem();
		ItemMeta woolMeta = wool.getItemMeta();
		woolMeta.setDisplayName(ChatColor.WHITE + settingObject.getKey());
		String[] comments = settingObject.getComments();
		List<String> lore = new ArrayList<>(comments.length + 5);
		for (String comment : comments) {
			lore.add(ChatColor.GRAY + comment);
		}
		lore.addAll(Arrays.asList(
				"",
				ChatColor.translateAlternateColorCodes('&', "&c지원되지 않는 데이터 타입입니다."),
				ChatColor.translateAlternateColorCodes('&', "&c콘피그에서 직접 변경해주세요.")
		));
		woolMeta.setLore(lore);
		wool.setItemMeta(woolMeta);
		return wool;
	}

}
