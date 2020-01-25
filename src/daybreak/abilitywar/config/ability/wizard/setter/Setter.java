package daybreak.abilitywar.config.ability.wizard.setter;

import daybreak.abilitywar.config.ability.AbilitySettings.SettingObject;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

public abstract class Setter {

	public static Setter getInstance(SettingObject<?> settingObject) {
		Object value = settingObject.getValue();
		if (value instanceof Integer) {
			return IntegerSetter.instance;
		} else if (value instanceof Boolean) {
			return BooleanSetter.instance;
		}
		return NotSupportedSetter.instance;
	}

	Setter() {
	}

	public abstract boolean onClick(SettingObject<?> settingObject, ClickType clickType);

	public abstract ItemStack getItem(SettingObject<?> settingObject);

}
