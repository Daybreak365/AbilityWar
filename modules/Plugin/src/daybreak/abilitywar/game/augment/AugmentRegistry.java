package daybreak.abilitywar.game.augment;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class AugmentRegistry {

	private final Map<String, Augment> augments = new LinkedHashMap<>();

	public void register(@NotNull Augment augment) {
		if (augments.containsKey(augment.getKey())) {
			throw new IllegalArgumentException("Duplicated augment key: " + augment.getKey());
		}
		augments.put(augment.getKey(), augment);
	}

	@Nullable
	public Augment getByKey(@NotNull String key) {
		return augments.get(key);
	}

	@NotNull
	public Collection<Augment> values() {
		return Collections.unmodifiableCollection(augments.values());
	}

	@NotNull
	public List<Augment> values(@NotNull AugmentRarity rarity) {
		final List<Augment> result = new ArrayList<>();
		for (Augment augment : augments.values()) {
			if (augment.getRarity() == rarity) result.add(augment);
		}
		return result;
	}
}
