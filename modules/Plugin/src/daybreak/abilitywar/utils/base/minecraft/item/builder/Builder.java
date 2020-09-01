package daybreak.abilitywar.utils.base.minecraft.item.builder;

import java.util.List;
import org.bukkit.inventory.ItemStack;

public interface Builder {

	Builder amount(final int amount);
	Builder displayName(final String displayName);
	Builder lore(final String... lore);
	Builder lore(final List<String> lore);
	Builder emptyLore();
	ItemStack build();

}
