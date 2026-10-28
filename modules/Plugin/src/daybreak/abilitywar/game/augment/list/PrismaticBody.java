package daybreak.abilitywar.game.augment.list;

import daybreak.abilitywar.game.AbstractGame.Participant;
import daybreak.abilitywar.game.augment.AbstractAugment;
import daybreak.abilitywar.game.augment.AugmentManifest;
import daybreak.abilitywar.game.augment.AugmentContext;
import daybreak.abilitywar.game.augment.AugmentRarity;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;

@AugmentManifest(key = "prismatic_body", name = "프리즘 육체", rarity = AugmentRarity.PRISMATIC, description = "최대 체력이 6 증가하고 재생 I 효과를 얻습니다.")
public class PrismaticBody extends AbstractAugment {
	@Override protected void onApply(@NotNull AugmentContext context, @NotNull Participant participant) {
		context.addMaxHealth(participant, 6.0);
		participant.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, Integer.MAX_VALUE, 0, true, false));
	}
}
