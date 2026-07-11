package daybreak.abilitywar.game.augment.list;

import daybreak.abilitywar.game.AbstractGame.Participant;
import daybreak.abilitywar.game.augment.AbstractAugment;
import daybreak.abilitywar.game.augment.AugmentContext;
import daybreak.abilitywar.game.augment.AugmentRarity;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.jetbrains.annotations.NotNull;

public class RaidBoss extends AbstractAugment {
	public RaidBoss() { super("raid_boss", "레이드 보스", AugmentRarity.PRISMATIC, "최대 체력이 8 증가하지만 이동 속도가 감소합니다."); }
	@Override protected void onApply(@NotNull AugmentContext context, @NotNull Participant participant) {
		context.addMaxHealth(participant, 8.0);
		final AttributeInstance attribute = participant.getPlayer().getAttribute(Attribute.GENERIC_MOVEMENT_SPEED);
		if (attribute != null) attribute.setBaseValue(attribute.getBaseValue() * 0.92);
	}
}
