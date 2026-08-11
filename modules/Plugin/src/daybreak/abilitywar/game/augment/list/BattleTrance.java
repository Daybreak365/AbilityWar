package daybreak.abilitywar.game.augment.list;

import daybreak.abilitywar.game.AbstractGame.Participant;
import daybreak.abilitywar.game.augment.AbstractAugment;
import daybreak.abilitywar.game.augment.AugmentManifest;
import daybreak.abilitywar.game.augment.AugmentContext;
import daybreak.abilitywar.game.augment.AugmentRarity;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;

@AugmentManifest(key = "battle_trance", name = "전투 몰입", rarity = AugmentRarity.GOLD, description = "상시 힘 I 효과를 얻습니다.")
public class BattleTrance extends AbstractAugment {
	@Override protected void onApply(@NotNull AugmentContext context, @NotNull Participant participant) {
		participant.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.INCREASE_DAMAGE, Integer.MAX_VALUE, 0, true, false));
	}
}
