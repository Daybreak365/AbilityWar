package daybreak.abilitywar.game.augment.list;

import daybreak.abilitywar.game.AbstractGame.Participant;
import daybreak.abilitywar.game.augment.AbstractAugment;
import daybreak.abilitywar.game.augment.AugmentContext;
import daybreak.abilitywar.game.augment.AugmentRarity;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.jetbrains.annotations.NotNull;

public class AbilityOverdrive extends AbstractAugment {
	public AbilityOverdrive() { super("ability_overdrive", "능력 과부하", AugmentRarity.PRISMATIC, "능력이 있다면 최대 체력과 이동 속도가 증가합니다."); }
	@Override public boolean isAvailable(@NotNull AugmentContext context, @NotNull Participant participant) { return super.isAvailable(context, participant) && participant.hasAbility(); }
	@Override protected void onApply(@NotNull AugmentContext context, @NotNull Participant participant) {
		context.addMaxHealth(participant, 4.0);
		final AttributeInstance attribute = participant.getPlayer().getAttribute(Attribute.GENERIC_MOVEMENT_SPEED);
		if (attribute != null) attribute.setBaseValue(attribute.getBaseValue() * 1.10);
	}
}
