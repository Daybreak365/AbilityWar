package daybreak.abilitywar.game.augment;

import com.google.common.collect.ImmutableList;
import daybreak.abilitywar.game.AbstractGame.Participant;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * 참가자에게 보여 줄 증강 선택지를 생성합니다.
 */
public class AugmentSelection {

	private final AugmentRegistry registry;
	private final Random random;
	private final Map<AugmentRarity, Integer> rarityWeights = new EnumMap<>(AugmentRarity.class);

	public AugmentSelection(@NotNull AugmentRegistry registry, @NotNull Random random) {
		this.registry = registry;
		this.random = random;
		for (AugmentRarity rarity : AugmentRarity.values()) {
			rarityWeights.put(rarity, rarity.getDefaultWeight());
		}
	}

	public void setRarityWeight(@NotNull AugmentRarity rarity, int weight) {
		rarityWeights.put(rarity, Math.max(0, weight));
	}

	@NotNull
	public List<Augment> roll(@NotNull AugmentContext context, @NotNull Participant participant, int amount) {
		final List<Augment> result = new ArrayList<>();
		for (int i = 0; i < amount; i++) {
			final AugmentRarity rarity = selectRarity();
			final List<Augment> pool = new ArrayList<>();
			for (Augment augment : registry.values(rarity)) {
				if (!result.contains(augment) && augment.isAvailable(context, participant)) pool.add(augment);
			}
			if (pool.isEmpty()) break;
			result.add(pool.get(random.nextInt(pool.size())));
		}
		return ImmutableList.copyOf(result);
	}

	private AugmentRarity selectRarity() {
		int totalWeight = 0;
		for (int weight : rarityWeights.values()) totalWeight += weight;
		if (totalWeight <= 0) return AugmentRarity.SILVER;
		int value = random.nextInt(totalWeight);
		for (Map.Entry<AugmentRarity, Integer> entry : rarityWeights.entrySet()) {
			value -= entry.getValue();
			if (value < 0) return entry.getKey();
		}
		return AugmentRarity.SILVER;
	}
}
