package daybreak.abilitywar.utils.base.minecraft.item.builder;

import com.google.common.base.Preconditions;
import daybreak.abilitywar.utils.base.minecraft.item.Skulls;
import java.util.Arrays;
import java.util.List;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

public class CustomSkullBuilder implements Builder {

	@NotNull
	private final String url;
	private int amount = 1;
	private String displayName;
	private List<String> lore;

	public CustomSkullBuilder(@NotNull final String url) {
		this.url = Preconditions.checkNotNull(url);
	}

	@Override
	public Builder amount(int amount) {
		Preconditions.checkArgument(amount > 0, "amount must be greater than 0");
		this.amount = amount;
		return this;
	}

	@Override
	public CustomSkullBuilder displayName(final String displayName) {
		this.displayName = displayName;
		return this;
	}

	@Override
	public CustomSkullBuilder lore(final String... lore) {
		this.lore = Arrays.asList(lore);
		return this;
	}

	@Override
	public CustomSkullBuilder lore(final List<String> lore) {
		this.lore = lore;
		return this;
	}

	@Override
	public CustomSkullBuilder emptyLore() {
		this.lore = null;
		return this;
	}

	@Override
	public ItemStack build() {
		final ItemStack stack = Skulls.createCustomSkull(url);
		stack.setAmount(amount);
		if (displayName != null || lore != null) {
			final ItemMeta meta = stack.getItemMeta();
			if (displayName != null) meta.setDisplayName(displayName);
			if (lore != null) meta.setLore(lore);
			stack.setItemMeta(meta);
		}
		return stack;
	}

}
