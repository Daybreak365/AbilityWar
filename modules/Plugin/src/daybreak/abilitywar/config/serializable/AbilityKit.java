package daybreak.abilitywar.config.serializable;

import daybreak.abilitywar.config.kitpreset.KitConfiguration.KitSettings;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.jetbrains.annotations.NotNull;

public class AbilityKit implements ConfigurationSerializable {

	private final Map<String, KitPreset> kits;

	public AbilityKit(final Map<String, Object> args) {
		Map<String, KitPreset> kits = new HashMap<>();
		for (Entry<String, Object> entry : args.entrySet()) {
			Object value = entry.getValue();
			if (value instanceof Map<?, ?>) {
				kits.put(entry.getKey(), new KitPreset((Map<?, ?>) value));
			}
		}
		this.kits = kits;
	}

	public AbilityKit() {
		this.kits = new HashMap<>();
	}

	@NotNull
	@Override
	public Map<String, Object> serialize() {
		final Map<String, Object> map = new HashMap<>();
		for (Entry<String, KitPreset> entry : kits.entrySet()) {
			map.put(entry.getKey(), entry.getValue().toMap());
		}
		return map;
	}

	@NotNull
	public KitPreset getKits(final String ability) {
		return kits.getOrDefault(ability, new KitPreset(KitSettings.getKit()));
	}

	public boolean hasKits(final String ability) {
		return kits.containsKey(ability);
	}

	public void setKits(final String ability, final KitPreset kit) {
		kits.put(ability, kit);
	}

	public void removeKits(String ability) {
		kits.remove(ability);
	}

}
