package daybreak.abilitywar.config.wizard.setter;

import daybreak.abilitywar.config.interfaces.Configurable;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

public abstract class Setter {

	public static Setter getInstance(Configurable<?> configurable) {
		Object value = configurable.getValue();
		if (value instanceof Integer) {
			return IntegerSetter.instance;
		} else if (value instanceof Boolean) {
			return BooleanSetter.instance;
		} else if (value instanceof Double) {
			return DoubleSetter.instance;
		}
		return NotSupportedSetter.instance;
	}

	Setter() {}

	public abstract boolean onClick(Configurable<?> configurable, ClickType clickType);

	public abstract ItemStack getItem(Configurable<?> configurable);

}
