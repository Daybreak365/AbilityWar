package daybreak.abilitywar.game.script.setter;

import daybreak.abilitywar.game.script.ScriptWizard;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.event.Event;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

/**
 * Enum Setter
 *
 * @author Daybreak 새벽
 */
public class EnumSetter extends Setter<Object> {

	private final int maxNumber;
	private final Class<?> enumClass;

	public EnumSetter(String Key, Object Default, ScriptWizard Wizard) throws IllegalArgumentException {
		super(Key, Default, Wizard);
		if (Default.getClass().isEnum()) {
			this.enumClass = Default.getClass();
		} else {
			if (Default.getClass().getSuperclass().isEnum()) {
				this.enumClass = Default.getClass().getSuperclass();
			} else {
				throw new IllegalArgumentException();
			}
		}
		this.maxNumber = enumClass.getEnumConstants().length - 1;
	}

	@Override
	public void execute(Listener listener, Event event) {
	}

	@Override
	public void onClick(ClickType click) {
		if (click.equals(ClickType.LEFT) || click.equals(ClickType.SHIFT_LEFT)) {
			if (number > 0) {
				number--;
			} else {
				number = maxNumber;
			}
		} else if (click.equals(ClickType.RIGHT) || click.equals(ClickType.SHIFT_RIGHT)) {
			if (number < maxNumber) {
				number++;
			} else {
				number = 0;
			}
		}

		setValue(enumClass.getEnumConstants()[number]);
	}

	private int number = 0;

	@Override
	public ItemStack getItem() {
		ItemStack enumItem = new ItemStack(Material.ENCHANTED_BOOK);
		ItemMeta enumItemMeta = enumItem.getItemMeta();
		enumItemMeta.setDisplayName(ChatColor.AQUA + this.getKey());

		List<String> enumItemLore = new ArrayList<>();

		Object[] enumConstants = enumClass.getEnumConstants();

		for (int i = (number - 2); i <= (number + 2); i++) {
			if (i >= 0 && i < enumConstants.length) {
				Object enumValue = enumConstants[i];
				enumItemLore.add((i == number ? ChatColor.GREEN : ChatColor.GRAY) + enumValue.toString());
			}
		}

		enumItemLore.add("§f전체 상수§7: §a" + (maxNumber + 1) + "§f개, 현재 상수§7: §a" + (number + 1) + "§f번째");
		enumItemLore.add("§f이전 상수로 변경하려면 §c좌클릭§f하세요.");
		enumItemLore.add("§f다음 상수로 변경하려면 §c우클릭§f하세요.");

		enumItemMeta.setLore(enumItemLore);

		enumItem.setItemMeta(enumItemMeta);

		return enumItem;
	}

}