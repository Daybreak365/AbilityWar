package daybreak.abilitywar.config.serializable;

import com.google.common.base.Preconditions;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.bukkit.Material;
import org.bukkit.configuration.MemorySection;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class KitPreset {

	@Nullable
	private ItemStack helmet, chestplate, leggings, boots;
	@NotNull
	private List<ItemStack> items;

	public KitPreset(final KitPreset other) {
		this.helmet = other.helmet;
		this.chestplate = other.chestplate;
		this.leggings = other.leggings;
		this.boots = other.boots;
		this.items = other.items.isEmpty() ? Collections.emptyList() : new ArrayList<>(other.items);
	}

	@SuppressWarnings("unchecked")
	public KitPreset(final Map<?, ?> map) throws ClassCastException {
		if (map.containsKey("helmet")) {
			this.helmet = (ItemStack) map.get("helmet");
		}
		if (map.containsKey("chestplate")) {
			this.chestplate = (ItemStack) map.get("chestplate");
		}
		if (map.containsKey("leggings")) {
			this.leggings = (ItemStack) map.get("leggings");
		}
		if (map.containsKey("boots")) {
			this.boots = (ItemStack) map.get("boots");
		}
		if (map.containsKey("items")) {
			this.items = (List<ItemStack>) map.get("items");
		} else {
			this.items = Collections.emptyList();
		}
	}

	@SuppressWarnings("unchecked")
	public KitPreset(final MemorySection section) throws ClassCastException, NullPointerException {
		if (section.isSet("helmet")) {
			this.helmet = section.getItemStack("helmet");
		}
		if (section.isSet("chestplate")) {
			this.chestplate = section.getItemStack("chestplate");
		}
		if (section.isSet("leggings")) {
			this.leggings = section.getItemStack("leggings");
		}
		if (section.isSet("boots")) {
			this.boots = section.getItemStack("boots");
		}
		if (section.isSet("items") && section.isList("items")) {
			this.items = (List<ItemStack>) Preconditions.checkNotNull(section.getList("items"));
		} else {
			this.items = Collections.emptyList();
		}
	}

	public KitPreset(final List<ItemStack> items) throws ClassCastException, NullPointerException {
		this.items = items != null ? items : Collections.emptyList();
	}

	public KitPreset() {
		this.items = Collections.emptyList();
	}

	@Nullable
	public ItemStack getHelmet() {
		return helmet;
	}

	public void setHelmet(@Nullable ItemStack helmet) {
		if (helmet != null && helmet.getType() == Material.AIR) helmet = null;
		this.helmet = helmet;
	}

	@Nullable
	public ItemStack getChestplate() {
		return chestplate;
	}

	public void setChestplate(@Nullable ItemStack chestplate) {
		if (chestplate != null && chestplate.getType() == Material.AIR) chestplate = null;
		this.chestplate = chestplate;
	}

	@Nullable
	public ItemStack getLeggings() {
		return leggings;
	}

	public void setLeggings(@Nullable ItemStack leggings) {
		if (leggings != null && leggings.getType() == Material.AIR) leggings = null;
		this.leggings = leggings;
	}

	@Nullable
	public ItemStack getBoots() {
		return boots;
	}

	public void setBoots(@Nullable ItemStack boots) {
		if (boots != null && boots.getType() == Material.AIR) boots = null;
		this.boots = boots;
	}

	@NotNull
	public List<ItemStack> getItems() {
		return items;
	}

	public void setItems(@NotNull List<ItemStack> items) {
		this.items = items;
	}

	public Map<String, Object> toMap() {
		final Map<String, Object> map = new HashMap<>();
		if (helmet != null) map.put("helmet", helmet);
		if (chestplate != null) map.put("chestplate", chestplate);
		if (leggings != null) map.put("leggings", leggings);
		if (boots != null) map.put("boots", boots);
		if (!items.isEmpty()) map.put("items", items);
		return map;
	}

}
