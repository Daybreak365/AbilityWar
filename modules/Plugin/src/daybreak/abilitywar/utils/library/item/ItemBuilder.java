package daybreak.abilitywar.utils.library.item;

import com.google.common.base.Preconditions;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;
import java.util.List;

public class ItemBuilder {

	private Material type;
	private int amount = 1;
	private String displayName;
	private List<String> lore;

	public ItemBuilder type(Material type) {
		this.type = type;
		return this;
	}

	public ItemBuilder amount(int amount) {
		Preconditions.checkArgument(amount > 0, "amount must be greater than 0");
		this.amount = amount;
		return this;
	}

	public ItemBuilder displayName(String displayName) {
		this.displayName = displayName;
		return this;
	}

	public ItemBuilder lore(String... lore) {
		this.lore = Arrays.asList(lore);
		return this;
	}

	public ItemBuilder lore(List<String> lore) {
		this.lore = lore;
		return this;
	}

	public ItemStack build() {
		Preconditions.checkNotNull(type, "type is null");
		ItemStack stack = new ItemStack(type, amount);
		if (displayName != null || lore != null) {
			ItemMeta meta = stack.getItemMeta();
			if (displayName != null) meta.setDisplayName(displayName);
			if (lore != null) meta.setLore(lore);
			stack.setItemMeta(meta);
		}
		return stack;
	}

}
