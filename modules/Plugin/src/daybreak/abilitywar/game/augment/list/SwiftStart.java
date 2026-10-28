package daybreak.abilitywar.game.augment.list;

import daybreak.abilitywar.game.AbstractGame.Participant;
import daybreak.abilitywar.game.augment.AbstractAugment;
import daybreak.abilitywar.game.augment.AugmentManifest;
import daybreak.abilitywar.game.augment.AugmentContext;
import daybreak.abilitywar.game.augment.AugmentRarity;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.jetbrains.annotations.NotNull;

@AugmentManifest(key = "swift_start", name = "민첩한 출발", rarity = AugmentRarity.SILVER, description = "이동 속도가 소폭 증가합니다.")
public class SwiftStart extends AbstractAugment {
	@Override protected void onApply(@NotNull AugmentContext context, @NotNull Participant participant) {
		final AttributeInstance attribute = participant.getPlayer().getAttribute(Attribute.GENERIC_MOVEMENT_SPEED);
		if (attribute != null) attribute.setBaseValue(attribute.getBaseValue() * 1.08);
	}
}
