package daybreak.abilitywar.config.serializable;

import daybreak.abilitywar.config.Configuration.Settings;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.inventory.ItemStack;

public class AbilityKit implements ConfigurationSerializable {

	private final Map<String, List<ItemStack>> kits;

	public AbilityKit(Map<String, Object> args) {
		Map<String, List<ItemStack>> kits = new HashMap<>();
		for (Entry<String, Object> entry : args.entrySet()) {
			Object value = entry.getValue();
			if (value instanceof List) {
				kits.put(entry.getKey(), (List<ItemStack>) value);
			}
		}
		this.kits = kits;
	}

	public AbilityKit() {
		this.kits = new HashMap<>();
	}

	@Override
	public Map<String, Object> serialize() {
		return new HashMap<>(kits);
	}

	public List<ItemStack> getKits(String abilityName) {
		return kits.getOrDefault(abilityName, Settings.getDefaultKit());
	}

	public void setKits(String abilityName, List<ItemStack> kit) {
		kits.put(abilityName, kit);
	}

}
