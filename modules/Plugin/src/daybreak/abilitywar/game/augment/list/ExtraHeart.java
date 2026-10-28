package daybreak.abilitywar.game.augment.list;

import daybreak.abilitywar.game.AbstractGame.Participant;
import daybreak.abilitywar.game.augment.AbstractAugment;
import daybreak.abilitywar.game.augment.AugmentManifest;
import daybreak.abilitywar.game.augment.AugmentContext;
import daybreak.abilitywar.game.augment.AugmentRarity;
import org.jetbrains.annotations.NotNull;

@AugmentManifest(key = "extra_heart", name = "튼튼한 심장", rarity = AugmentRarity.SILVER, description = "최대 체력이 2 증가합니다.")
public class ExtraHeart extends AbstractAugment {
	@Override protected void onApply(@NotNull AugmentContext context, @NotNull Participant participant) { context.addMaxHealth(participant, 2.0); }
}
