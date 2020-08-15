package daybreak.abilitywar.utils.library.item;

import com.google.common.base.Preconditions;
import daybreak.abilitywar.utils.base.minecraft.item.Skulls;
import java.util.Arrays;
import java.util.List;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

public class CustomSkullBuilder {

	@NotNull
	private final String url;
	private String displayName;
	private List<String> lore;

	public CustomSkullBuilder(@NotNull final String url) {
		this.url = Preconditions.checkNotNull(url);
	}

	public CustomSkullBuilder displayName(String displayName) {
		this.displayName = displayName;
		return this;
	}

	public CustomSkullBuilder lore(String... lore) {
		this.lore = Arrays.asList(lore);
		return this;
	}

	public CustomSkullBuilder lore(List<String> lore) {
		this.lore = lore;
		return this;
	}

	public CustomSkullBuilder lore() {
		this.lore = null;
		return this;
	}

	public ItemStack build() {
		final ItemStack stack = Skulls.createCustomSkull(url);
		if (displayName != null || lore != null) {
			final ItemMeta meta = stack.getItemMeta();
			if (displayName != null) meta.setDisplayName(displayName);
			if (lore != null) meta.setLore(lore);
			stack.setItemMeta(meta);
		}
		return stack;
	}

}
