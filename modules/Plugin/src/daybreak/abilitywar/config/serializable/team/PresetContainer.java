package daybreak.abilitywar.config.serializable.team;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import org.bukkit.configuration.serialization.ConfigurationSerializable;

public class PresetContainer implements ConfigurationSerializable {

	private final Map<String, TeamPreset> structures;

	public PresetContainer() {
		this.structures = new HashMap<>();
	}

	public PresetContainer(Map<String, Object> args) {
		Map<String, TeamPreset> structures = new HashMap<>();
		for (Entry<String, Object> entry : args.entrySet()) {
			Object value = entry.getValue();
			if (value instanceof TeamPreset) {
				structures.put(entry.getKey(), (TeamPreset) value);
			}
		}
		this.structures = structures;
	}

	public Set<String> getKeys() {
		return structures.keySet();
	}

	public Collection<TeamPreset> getPresets() {
		return structures.values();
	}

	public TeamPreset getPreset(String name) {
		return structures.get(name);
	}

	public boolean addPreset(TeamPreset structure) {
		if (structures.get(structure.getName()) == null) {
			structures.put(structure.getName(), structure);
			return true;
		} else return false;
	}

	public void removePreset(String name) {
		structures.remove(name);
	}

	public boolean hasPreset(String name) {
		return structures.containsKey(name);
	}

	@Override
	public Map<String, Object> serialize() {
		return new HashMap<>(structures);
	}

}
