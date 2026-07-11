package daybreak.abilitywar.game.augment.list;

import daybreak.abilitywar.game.AbstractGame.Participant;
import daybreak.abilitywar.game.augment.AbstractAugment;
import daybreak.abilitywar.game.augment.AugmentContext;
import daybreak.abilitywar.game.augment.AugmentRarity;
import org.jetbrains.annotations.NotNull;

public class ExtraHeart extends AbstractAugment {
	public ExtraHeart() { super("extra_heart", "튼튼한 심장", AugmentRarity.SILVER, "최대 체력이 2 증가합니다."); }
	@Override protected void onApply(@NotNull AugmentContext context, @NotNull Participant participant) { context.addMaxHealth(participant, 2.0); }
}
